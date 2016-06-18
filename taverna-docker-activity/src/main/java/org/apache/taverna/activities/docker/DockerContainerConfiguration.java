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

public class DockerContainerConfiguration {

    /**
     * Hold the hostname of the docker container.
     */
    private String containerHost;

    /**
     * Remote REST API port exposed by Docker
     */
    private int remoteAPIPort = 443;

    /**
     * JSON payload to invoke create container REST API.
     */
    private JsonNode createContainerPayload;

    /**
     * Complete HTTP URL for create container
     */
    private final String createContainerURL;

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

    public DockerContainerConfiguration(String containerHost, int remoteAPIPort, String protocol, JsonNode createContainerPayload) {
        this.containerHost = containerHost;
        this.remoteAPIPort = remoteAPIPort;
        this.protocol = protocol;
        this.createContainerPayload = createContainerPayload;
        this.createContainerURL = protocol + "://" + containerHost +  ":" + remoteAPIPort + CREATE_CONTAINER_RESOURCE_PATH ;
    }

    public String getContainerHost() {
        return containerHost;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getRemoteAPIPort() {
        return remoteAPIPort;
    }

    public JsonNode getCreateContainerPayload() {
        return createContainerPayload;
    }

    public String getCreateContainerURL() {
        return createContainerURL;
    }
}
