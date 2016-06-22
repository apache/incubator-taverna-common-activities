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
package org.apache.taverna.activities.docker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DockerContainerConfigurationImpl extends AbstractConfigurable {

    /**
     * Key for Remote host
     */
    public static final String CONTAINER_REMOTE_HOST = "key-cnt-host";

    /**
     * Key for transport protocol
     */
    public static final String PROTOCOL = "key-cnt-protocol";

    /**
     * Key for Remote port
     */
    public static final String CONTAINER_REMOTE_PORT = "key-cnt-port";

    /**
     * Key for create container payload. Here we accept entire JSON payload as the value of this key in hash map.
     */
    public static final String CONTAINER_CREATE_PAYLOAD = "key-cnt-create-payload";

    /**
     * Docker remote REST resource path for creating a container
     */
    public static final String CREATE_CONTAINER_RESOURCE_PATH = "/containers/create";

    /**
     * Identifier for Http over SSL protocol
     */
    public static final String HTTP_OVER_SSL = "https";

    /**
     * Transport protocol
     */
    private String protocol = "http";



    public DockerContainerConfigurationImpl(ConfigurationManager configurationManager){
        super(configurationManager);

    }

    public String getContainerHost() {
        return getInternalPropertyMap().get(CONTAINER_REMOTE_HOST);
    }

    public String getProtocol() {
        return getInternalPropertyMap().get(PROTOCOL);
    }

    public int getRemoteAPIPort() {
        return Integer.parseInt(getInternalPropertyMap().get(CONTAINER_REMOTE_PORT));
    }

    public JsonNode getCreateContainerPayload() throws IOException {
      return new ObjectMapper().readTree(getInternalPropertyMap().get(CONTAINER_CREATE_PAYLOAD));
    }

    public String getCreateContainerURL() {
       return getProtocol() + "://" + getContainerHost() +  ":" + getRemoteAPIPort() + CREATE_CONTAINER_RESOURCE_PATH;
    }

    @Override
    public Map<String, String> getDefaultPropertyMap() {
        Map<String,String> defaultMap = new HashMap<String,String>();
        return defaultMap;
    }

    @Override
    public String getUUID() {
        return "6BR3F5C1-DK8D-4893-8D9B-2F46FA1DDB87";
    }

    @Override
    public String getDisplayName() {
        return "Docker Config";
    }

    @Override
    public String getFilePrefix() {
        return "Docker";
    }

    @Override
    public String getCategory() {
        return null;
    }


}
