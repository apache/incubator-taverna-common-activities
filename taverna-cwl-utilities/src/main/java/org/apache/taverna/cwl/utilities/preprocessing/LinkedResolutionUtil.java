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
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * This class is Util class which is used for resolving Linked resolution
 * Rules are defined at: http://www.commonwl.org/draft-3/SchemaSalad.html#Link_resolution
 */
public class LinkedResolutionUtil implements CwlPreprocessor {

    final private static Logger logger = Logger.getLogger(LinkedResolutionUtil.class);

    private JsonNode cwlToolDescription;
    private JsonNode nameSpace;
    private URI BASE;
    private Path path;

    /**
     *
     * @param cwlToolDescription CWL tool description
     * @param path This must be the directory where CWL tool is located and resolving is done assuming required other files
     *             in the same directory
     */
    public LinkedResolutionUtil(JsonNode cwlToolDescription, Path path) {
        this.cwlToolDescription = cwlToolDescription;
        this.path = path;
        setup();
    }

    private void setBASE(URI BASE) {
        this.BASE = BASE;
    }

    /**
     * This method setup the initial resources for the process
     */
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

    /**
     * This method is resolving the uri based on the rules mentioned in the CWL Schema Salad
     * @param val This is a ValueNode (subclass of a JsonNode) which holds the link. This must be a single node
     * @return An URI object which contains absolute is returned
     * @throws URISyntaxException
     */
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

        Path absolutePath = path.resolve(uri.getPath());
        File file = new File(absolutePath.toString());

        if (file.isFile()) {
            final String fragment = uri.getFragment();
            return (fragment != null) ? absolutePath.toUri().resolve("#" + fragment) : absolutePath.toUri();
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

