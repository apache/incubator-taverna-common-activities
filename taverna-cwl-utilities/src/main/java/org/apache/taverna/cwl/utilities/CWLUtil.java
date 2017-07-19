/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.taverna.cwl.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CWLUtil {

    public static final String ARRAY_SPLIT_BRACKETS = "\\[\\]";
    public static final String ARRAY_SIGNATURE_BRACKETS = "\\[\\]$";
    private static final String INPUTS = "inputs";
    private static final String OUTPUTS = "outputs";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String ARRAY = "array";
    private static final String DESCRIPTION = "description";
    private static final int DEPTH_0 = 0;
    private static final int DEPTH_1 = 1;
    private static final int DEPTH_2 = 2;

    private static final String FLOAT = "float";
    private static final String NULL = "null";
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";
    private static final String DOUBLE = "double";
    private static final String STRING = "string";
    private static final String LABEL = "label";
    private static final String FILE = "file";
    private static final String DIRECTORY = "directory";
    private static final String FORMAT = "format";
    private JsonNode nameSpace;
    private JsonNode cwlFile;

    public CWLUtil(JsonNode cwlFile) {
        this.cwlFile = cwlFile;
        processNameSpace();
    }

    public JsonNode getNameSpace() {
        return nameSpace;
    }

    /**
     * This method is processing the CWL NameSpace for later use such as
     * figuring out the Format of a input or output
     */
    public void processNameSpace() {

        if (cwlFile != null && cwlFile.has("$namespaces")) {
            nameSpace = cwlFile.path("$namespaces");
        }

    }

    public Map<String, Integer> processInputDepths() {
        return process(cwlFile.get(INPUTS));
    }

    public Map<String, Integer> processOutputDepths() {
        return process(cwlFile.get(OUTPUTS));
    }

    public Map<String, PortDetail> processInputDetails() {
        return processdetails(cwlFile.get(INPUTS));
    }

    public Map<String, PortDetail> processOutputDetails() {
        return processdetails(cwlFile.get(OUTPUTS));
    }

    /**
     * This method will go through CWL tool input or out puts and figure outs
     * their IDs and the respective depths
     *
     * @param inputs This is JsonNode object which contains the Inputs or outputs
     *               of the respective CWL tool
     * @return This the respective, ID and the depth of the input or output
     */
    public Map<String, Integer> process(JsonNode inputs) {

        Map<String, Integer> result = new HashMap<>();

        if (inputs == null)
            return result;

        if (inputs.getClass() == ArrayNode.class) {
            Iterator<JsonNode> iterator = inputs.iterator();

            while (iterator.hasNext()) {
                JsonNode input = iterator.next();
                String currentInputId = input.get(ID).asText();

                JsonNode typeConfigurations;
                try {

                    typeConfigurations = input.get(TYPE);
                    // if type :single argument
                    if (typeConfigurations.getClass() == TextNode.class) {
                        // inputs:
                        /// -id: input_1
                        //// type: int[]
                        if (isValidArrayType(typeConfigurations.asText()))
                            result.put(currentInputId, DEPTH_1);
                            // inputs:
                            /// -id: input_1
                            //// type: int or int?
                        else
                            result.put(currentInputId, DEPTH_0);
                        // type : defined as another map which contains type:
                    } else if (typeConfigurations.getClass() == ObjectNode.class) {
                        // inputs:
                        /// -id: input_1
                        //// type:
                        ///// type: array or int[]
                        String inputType = typeConfigurations.get(TYPE).asText();
                        if (inputType.equals(ARRAY) || isValidArrayType(inputType)) {
                            result.put(currentInputId, DEPTH_1);

                        }
                        // inputs:
                        // -id: input_1
                        // type:
                        // type: ["null",int]
                    } else if (typeConfigurations.getClass() == ArrayNode.class) {
                        if (isValidDataType(typeConfigurations)) {
                            result.put(currentInputId, DEPTH_0);
                        }

                    }

                } catch (ClassCastException e) {

                    System.out.println("Class cast exception !!!");
                }

            }
        } else if (inputs.getClass() == ObjectNode.class) {

            Iterator<Entry<String, JsonNode>> iterator = inputs.fields();

            while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                String currentInputId = entry.getKey();
                JsonNode typeConfigurations = entry.getValue();

                if (typeConfigurations.getClass() == TextNode.class) {
                    if (typeConfigurations.asText().startsWith("$")) {
                        System.out.println("Exception");
                    }
                    // inputs:
                    /// input_1: int[]
                    else if (isValidArrayType(typeConfigurations.asText()))
                        result.put(currentInputId, DEPTH_1);
                        // inputs:
                        /// input_1: int or int?
                    else
                        result.put(currentInputId, DEPTH_0);

                } else if (typeConfigurations.getClass() == ObjectNode.class) {

                    if (typeConfigurations.has(TYPE)) {
                        JsonNode inputType = typeConfigurations.get(TYPE);
                        // inputs:
                        /// input_1:
                        //// type: [int,"null"]
                        if (inputType.getClass() == ArrayNode.class) {
                            if (isValidDataType(inputType))
                                result.put(currentInputId, DEPTH_0);
                        } else {
                            // inputs:
                            /// input_1:
                            //// type: array or int[]
                            if (inputType.asText().equals(ARRAY) || isValidArrayType(inputType.asText()))
                                result.put(currentInputId, DEPTH_1);
                                // inputs:
                                /// input_1:
                                //// type: int or int?
                            else
                                result.put(currentInputId, DEPTH_0);
                        }
                    }
                }
            }

        }
        return result;
    }

    /**
     * This method is used for extracting details of the CWL tool inputs or
     * outputs. ex:Label, Format, Description
     *
     * @param inputs This is JsonNode object which contains the Inputs or outputs
     *               of the respective CWL tool
     * @return
     */
    private Map<String, PortDetail> processdetails(JsonNode inputs) {

        Map<String, PortDetail> result = new HashMap<>();

        if (inputs.getClass() == ArrayNode.class) {

            for (JsonNode input : inputs) {
                PortDetail detail = new PortDetail();
                String currentInputId = input.get(ID).asText();

                getParamDetails(result, input, detail, currentInputId);

            }
        } else if (inputs.getClass() == ObjectNode.class) {
            Iterator<Entry<String, JsonNode>> iterator = inputs.fields();
            while (iterator.hasNext()) {
                PortDetail detail = new PortDetail();
                Entry<String, JsonNode> entry = iterator.next();
                getParamDetails(result, entry.getValue(), detail, entry.getKey());
            }
        }
        return result;
    }

    private void getParamDetails(Map<String, PortDetail> result, JsonNode input, PortDetail detail,
                                 String currentInputId) {
        extractDescription(input, detail);

        extractFormat(input, detail);

        extractLabel(input, detail);

        result.put(currentInputId, detail);
    }

    /**
     * This method is used for extracting the Label of a CWL input or Output
     *
     * @param input  Single CWL input or output as a JsonNode
     * @param detail respective PortDetail Object to hold the extracted Label
     */
    public void extractLabel(JsonNode input, PortDetail detail) {
        if (input != null)
            if (input.has(LABEL)) {
                detail.setLabel(input.get(LABEL).asText());
            } else {
                detail.setLabel(null);
            }
    }

    /**
     * @param input  Single CWL input or output as a JsonNode
     * @param detail respective PortDetail Object to hold the extracted Label
     */
    public void extractDescription(JsonNode input, PortDetail detail) {
        if (input != null)
            if (input.has(DESCRIPTION)) {
                detail.setDescription(input.get(DESCRIPTION).asText());
            } else {
                detail.setDescription(null);
            }
    }

    /**
     * This method is used for extracting the Formats of a CWL input or Output
     * Single argument(Input or Output) can have multiple Formats.
     *
     * @param input  Single CWL input or output as a JsonNode
     * @param detail respective PortDetail Object to hold the extracted Label
     */
    public void extractFormat(JsonNode input, PortDetail detail) {
        if (input != null)
            if (input.has(FORMAT)) {

                JsonNode formatInfo = input.get(FORMAT);

                ArrayList<String> format = new ArrayList<>();
                detail.setFormat(format);

                if (formatInfo.getClass() == TextNode.class) {

                    figureOutFormats(formatInfo.asText(), detail);
                } else if (formatInfo.getClass() == ArrayNode.class) {
                    for (JsonNode eachFormat : formatInfo) {
                        figureOutFormats(eachFormat.asText(), detail);
                    }
                }

            }
    }

    /**
     * Re Format the CWL format using the NameSpace in CWL Tool if possible
     * otherwise it doesn't change the current nameSpace => edam:http://edam.org
     * format : edam :1245 => http://edamontology.org/1245
     *
     * @param formatInfoString Single Format
     * @param detail           respective PortDetail Object to hold the extracted Label
     */
    public void figureOutFormats(String formatInfoString, PortDetail detail) {
        if (formatInfoString.startsWith("$")) {

            detail.addFormat(formatInfoString);
        } else if (formatInfoString.contains(":")) {
            String format[] = formatInfoString.split(":");
            String namespaceKey = format[0];
            String urlAppednd = format[1];

            if (nameSpace.has(namespaceKey))
                detail.addFormat(nameSpace.get(namespaceKey).asText() + urlAppednd);
            else
                // can't figure out the format
                detail.addFormat(formatInfoString);

        } else {
            // can't figure out the format
            detail.addFormat(formatInfoString);
        }
    }

    /**
     * This method is used to check whether the input/output is valid CWL TYPE
     * when the type is represented as type: ["null","int"]
     *
     * @param typeConfigurations Type of the CWl input or output
     * @return
     */
    public boolean isValidDataType(JsonNode typeConfigurations) {
        if (typeConfigurations == null)
            return false;
        for (JsonNode type : typeConfigurations) {
            if (!(type.asText().equals(FLOAT) || type.asText().equals(NULL) || type.asText().equals(BOOLEAN)
                    || type.asText().equals(INT) || type.asText().equals(STRING) || type.asText().equals(DOUBLE)
                    || type.asText().equals(FILE) || type.asText().equals(DIRECTORY)))
                return false;
        }
        return true;
    }

    /**
     * This method is for figure out whether the parameter is an array or not.
     * As from CWL document v1.0, array can be defined as "TYPE[]". For Example
     * : int[] This method will look for "[]" sequence of characters in the end
     * of the type and is provided type is a valid CWL TYPE or not
     *
     * @param type type of the CWL parameter
     * @return
     */
    public boolean isValidArrayType(String type) {
        if (type == null)
            return false;
        Pattern pattern = Pattern.compile(ARRAY_SIGNATURE_BRACKETS);
        Matcher matcher = pattern.matcher(type);
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode node = mapper.createArrayNode();
        node.add(type.split(ARRAY_SPLIT_BRACKETS)[0]);
        if (matcher.find() && isValidDataType(node))
            return true;
        else
            return false;
    }
}
