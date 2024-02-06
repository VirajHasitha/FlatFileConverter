/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.example;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FlatFileConverter extends AbstractMediator {
    private static final Log log = LogFactory.getLog(FlatFileConverter.class);

    public boolean mediate(MessageContext mc) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) mc)
                .getAxis2MessageContext();
        log.info("Class Mediator Mediate Method");
        OMElement element = (OMElement) mc.getEnvelope().getBody().getFirstElement();
        String flatData = (String) element.getText();

        try {
            //Split based on the number of new lines
            String[] lines = flatData.split("\n");
            JSONArray jsonArray = new JSONArray();
            String[] flatDataArray = new String[lines.length];
            String[] outputDataArray = new String[lines.length];
            for (int i = 0; i < lines.length; i++) {
                flatDataArray[i] = lines[i];
                log.info("Line " + (i + 1) + " Data : " + lines[i]);
                String[] parts = flatDataArray[i].split("\\s{2,}");

                //for (String part : parts) {
                String riskId = parts[0].replace("-", "");
                String fein = parts[1].replace("-", "");
                String name = parts[2].replaceAll("\\s", "");
                String city = parts[3].replaceAll("\\s", "");
                int emodMr = Integer.parseInt(parts[4]);
                int year = Integer.parseInt(parts[5]);

                // Build the output string
                String output = String.format("RISKID: %s,\nFEIN:  %s,\nNAME:  %s,\nCITY:  %s,\nEMOD_MR: %d,\nYEAR:   %d",
                        riskId, fein, name, city, emodMr, year);

                outputDataArray[i] = output;
                log.info(outputDataArray[i]);

                // Convert payload to a JSON object
                JSONObject jsonObject = new JSONObject();
                String[] keyValuePairs = output.split(",\\s*");

                for (String pair : keyValuePairs) {
                    String[] entry = pair.split(":\\s*");
                    if (entry.length == 2) {
                        jsonObject.put(entry[0], entry[1]);
                    }
                }
                // Print the JSON object
                log.info("\n" + jsonObject.toString(4));

                jsonArray.put(jsonObject);
            }
            String jsonArrayString = jsonArray.toString();
            log.info(jsonArrayString);
            JsonUtil.getNewJsonPayload(axis2MessageContext, jsonArrayString, true, true);

        } catch (JSONException | AxisFault e) {
            e.printStackTrace();
        }
        return true;
    }

}
