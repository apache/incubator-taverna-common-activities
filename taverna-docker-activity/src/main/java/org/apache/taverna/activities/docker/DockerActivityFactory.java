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
import org.apache.taverna.workflowmodel.processor.activity.*;

import java.net.URI;
import java.util.Set;

public class DockerActivityFactory implements ActivityFactory {

    private DockerContainerConfiguration containerConfiguration;

    @Override
    public Activity<?> createActivity() {
        return new DockerActivity(containerConfiguration);
    }

    @Override
    public URI getActivityType() {
        return null;
    }

    @Override
    public JsonNode getActivityConfigurationSchema() {
        return null;
    }

    @Override
    public Set<ActivityInputPort> getInputPorts(JsonNode jsonNode) throws ActivityConfigurationException {
        return null;
    }

    @Override
    public Set<ActivityOutputPort> getOutputPorts(JsonNode jsonNode) throws ActivityConfigurationException {
        return null;
    }


    public void setDockerConfigurationManagerManager(DockerContainerConfiguration dockerContainerConfiguration) {
        this.containerConfiguration = containerConfiguration;
    }
}
