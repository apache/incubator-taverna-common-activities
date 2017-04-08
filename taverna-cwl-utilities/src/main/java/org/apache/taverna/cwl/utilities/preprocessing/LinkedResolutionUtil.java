/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LinkedResolutionUtil implements CwlPreprocessor {

    final private static Logger logger = Logger.getLogger(LinkedResolutionUtil.class);

    private JsonNode cwlToolDescription;
    private JsonNode nameSpace;
    private URI BASE;
    private Path path;


    private void setBASE(URI BASE) {
        this.BASE = BASE;
    }


    public LinkedResolutionUtil(JsonNode cwlToolDescription, Path path) {
        this.cwlToolDescription = cwlToolDescription;
        this.path = path;
        setup();
    }

    private void setup() {
        try {
            if (cwlToolDescription.has("$base"))
                setBASE(new URI(cwlToolDescription.get("$base").asText()));
            if (cwlToolDescription.has("$namespaces"))
                setNameSpace(cwlToolDescription.get("$namespaces"));
        } catch (URISyntaxException e) {
            logger.error("Setup function ", e);
        }
    }


    private void setNameSpace(JsonNode nameSpace) {
        this.nameSpace = nameSpace;
    }


    @Override
    public URI process(JsonNode val) throws URISyntaxException {

        String uriString = val.asText();
        URI uri = new URI(uriString);

        if (uri.isAbsolute()) {
            if (uriString.contains(":")) {

                String valSplit[] = uriString.split(":");
                String key = valSplit[0];

                if (nameSpace.has(key)) {

                    return new URI((nameSpace.get(key).asText() + valSplit[1]));
                }
            }

            return uri;
        }

        Path absolutePath = path.resolve(uriString);

        File file = new File(absolutePath.toString());

        if (file.isFile()) {
            return absolutePath.toUri();
        }


        if (BASE == null) {

            logger.warn("Base is null !");
            return null;
        }
        return BASE.resolve(uriString);
    }
    @Override
    public void process() throws URISyntaxException {

    }

}

