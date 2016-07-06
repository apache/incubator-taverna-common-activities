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

    public static final String ACTION = "action";

    public static final String INSPECT = "inspect";

    public static final String CREATE_CONTAINER = "create-container";

    public static final String START_CONTAINER = "start-container";

    public static final String STOP_CONTAINER = "stop-container";

    public static final String LIST_CONTAINERS = "list-containers";

    public static final String OUT_CONTAINER_ID = "container-id";

    public static final String OUT_IMAGE_ID = "container-id";

    public static final String OUT_IMAGE_AUTHOR = "image-author";

    public static final String OUT_IMAGE_CONTAINER = "image-container";

    public static final String IN_IMAGE_NAME = "image-name";

    public static final String RESPONSE_BODY_KEY = "response_body";

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
                String action = getRenderedParam(referenceService,context, map.get(ACTION));

                JsonNodeFactory factory = new ObjectMapper().getNodeFactory();
                ObjectNode outJson = factory.objectNode();

                RemoteClient remoteClient = new RemoteClient(containerConfiguration);
                    if (CREATE_CONTAINER.equalsIgnoreCase(action)) {

                        CreateContainerResponse response = remoteClient.createContainer();
                        System.out.println("+++++" + response.toString());
                        outJson.put(OUT_CONTAINER_ID, response.getId());

                    } else if (INSPECT.equalsIgnoreCase(action)) {

                        String imageName = getRenderedParam(referenceService, context, map.get(IN_IMAGE_NAME));
                        InspectImageResponse response = remoteClient.inspect(imageName);
                        outJson.put(OUT_IMAGE_ID, response.getId());
                        outJson.put(OUT_IMAGE_AUTHOR, response.getAuthor());
                        outJson.put(OUT_IMAGE_CONTAINER, response.getContainer());

                    } else if (LIST_CONTAINERS.equalsIgnoreCase(action)) {

                        // TODO

                    } else if (START_CONTAINER.equalsIgnoreCase(action)) {
                        // TODO
                    } else if (STOP_CONTAINER.equalsIgnoreCase(action)) {
                        // TODO
                    }
                    //TODO add any more supporting actions
                    responseBodyRef = referenceService.register(outJson.toString(), 0, true, context);

                outputs.put(RESPONSE_BODY_KEY, responseBodyRef);
                callback.receiveResult(outputs, new int[0]);
            }
        });
    }


    private String getRenderedParam(ReferenceService referenceService, InvocationContext context, T2Reference key){
        return (String) referenceService.renderIdentifier(key, String.class, context);

    }
}
