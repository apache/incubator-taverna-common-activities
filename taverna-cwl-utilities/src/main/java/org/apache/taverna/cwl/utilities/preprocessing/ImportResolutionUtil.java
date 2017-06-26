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
package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class ImportResolutionUtil extends LinkedResolutionUtil {

    final public static String IMPORT = "$import";
    final private static Logger logger = Logger.getLogger(ImportResolutionUtil.class);
    private JsonNode cwlToolDescription;
    private Path path;

    public ImportResolutionUtil(JsonNode cwlToolDescription, Path path) {
        super(cwlToolDescription, path);
        this.cwlToolDescription = cwlToolDescription;
        this.path = path;
    }

    @Override
    public void process() throws URISyntaxException {
        processNode(cwlToolDescription);
    }


    public void processNode(JsonNode node) throws URISyntaxException {

        replace(node);
        if (node.has(IMPORT)) {
            processNode(node);
        } else {
            List<JsonNode> parents = node.findParents(IMPORT);
            parents.forEach(x -> {
                replace(x);
                if (x.has(IMPORT)) {
                    try {
                        processNode(x);
                    } catch (URISyntaxException e) {
                        logger.error("URI is not valid", e);
                    }
                }
            });
        }
    }

    public void replace(JsonNode node) {
        JsonNode importedNode = null;
        try {
            ImportNode importNode = new ImportViaHTTP();
            importedNode = importNode.importNode(super.process(node.get(IMPORT)));
        } catch (URISyntaxException e) {
            logger.error("URI is not valid", e);
        }
        ((ObjectNode) node).remove(IMPORT);
        importedNode.fields().forEachRemaining(y -> {
            ((ObjectNode) node).put(y.getKey(), y.getValue());
        });
    }


}
