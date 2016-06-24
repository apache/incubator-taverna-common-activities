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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import org.apache.log4j.Logger;
import org.apache.taverna.invocation.InvocationContext;
import org.apache.taverna.reference.ErrorDocument;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Docker activity class responsible of handling tasks that are related to creating/invoking an external docker container.
 */
public class DockerActivity extends AbstractAsynchronousActivity<JsonNode> {

    private JsonNode json;

    private DockerContainerConfiguration containerConfiguration;

    private static final String ACTION = "action";

    private static final String INSPECT = "inspect";

    private static final String CREATE_CONTAINER = "create-container";

    private static final String START_CONTAINER = "start-container";

    private static final String STOP_CONTAINER = "stop-container";

    private static final String LIST_CONTAINERS = "list-containers";

    private static final String OUT_CONTAINER_ID = "container-id";

    private static final String OUT_IMAGE_ID = "container-id";

    private static final String OUT_IMAGE_AUTHOR = "image-author";

    private static final String OUT_IMAGE_CONTAINER = "image-container";

    private static final String IN_IMAGE_NAME = "image-name";

    private static Logger LOG = Logger.getLogger(DockerActivity.class);


    public DockerActivity(DockerContainerConfiguration containerConfiguration) {
        this.containerConfiguration = containerConfiguration;
    }

    @Override
    public void configure(JsonNode json) throws ActivityConfigurationException {
      this.json = json;
    }

    @Override
    public JsonNode getConfiguration() {
        return json;
    }

    @Override
    public void executeAsynch(final Map<String, T2Reference> map, final AsynchronousActivityCallback callback) {
        callback.requestRun(new Runnable() {
            @Override
            public void run() {

                Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
                T2Reference responseBodyRef = null;
                InvocationContext context = callback.getContext();
                ReferenceService referenceService = context.getReferenceService();
                String action = map.get(ACTION).getLocalPart();

                JsonNodeFactory factory = new ObjectMapper().getNodeFactory();
                ObjectNode outJson = factory.objectNode();

                RemoteClient remoteClient = new RemoteClient(containerConfiguration);
                try {
                    if (CREATE_CONTAINER.equalsIgnoreCase(action)) {

                        CreateContainerResponse response = remoteClient.createContainer();
                        outJson.put(OUT_CONTAINER_ID, response.getId());

                    } else if (INSPECT.equalsIgnoreCase(action)) {

                        String imageName = map.get(IN_IMAGE_NAME).getLocalPart();
                        InspectImageResponse response = remoteClient.inspect(imageName);
                        outJson.put(OUT_IMAGE_ID, response.getId());
                        outJson.put(OUT_IMAGE_AUTHOR, response.getAuthor());
                        outJson.put(OUT_IMAGE_CONTAINER, response.getContainer());

                    } else if (START_CONTAINER.equalsIgnoreCase(action)) {
                        // TODO
                    } else if (STOP_CONTAINER.equalsIgnoreCase(action)) {
                        // TODO
                    } else if (LIST_CONTAINERS.equalsIgnoreCase(action)) {
                        // TODO
                    }
                    //TODO add any more supporting actions
                    responseBodyRef = referenceService.register(outJson.toString(), 0, true, context);
                } catch (Exception e){
                    String log = "Error occurred while executing remote docker commands " + e.getMessage();
                    LOG.error(log ,e);
                    responseBodyRef = referenceService.register("{\"error\",\"" + log + "\"}", 0, true, context);
                }
                outputs.put("response_body", responseBodyRef);
                callback.receiveResult(outputs, new int[0]);
            }
        });
    }
}
