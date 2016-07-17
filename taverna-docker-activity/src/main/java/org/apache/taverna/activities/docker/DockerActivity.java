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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import org.apache.log4j.Logger;
import org.apache.taverna.invocation.InvocationContext;
import org.apache.taverna.reference.ErrorDocument;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;

import java.util.HashMap;
import java.util.List;
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

    public static final String DELETE_CONTAINER = "delete-container";

    public static final String STOP_CONTAINER = "stop-container";

    public static final String LIST_CONTAINERS = "list-containers";

    public static final String CONTAINER_ID = "container-id";

    public static final String CONTAINER_NAME = "container-name";

    public static final String OUT_IMAGE_ID = "image-id";

    public static final String OUT_IMAGE_AUTHOR = "image-author";

    public static final String OUT_IMAGE_CONTAINER = "image-container";

    public static final String IN_IMAGE_NAME = "image-name";

    public static final String IN_CONTAINER_START_CMD = "cnt-start-cmd";

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
                String action = getRenderedParam(referenceService, context, map.get(ACTION));

                JsonNodeFactory factory = new ObjectMapper().getNodeFactory();
                ContainerNode outJson = null;

                RemoteClient remoteClient = new RemoteClient(containerConfiguration);

                if (CREATE_CONTAINER.equalsIgnoreCase(action)) {

                    outJson = factory.objectNode();
                    CreateContainerResponse response = remoteClient.createContainer();
                    ((ObjectNode)outJson).put(CONTAINER_ID, response.getId());

                } else if (INSPECT.equalsIgnoreCase(action)) {

                    outJson = factory.objectNode();
                    String imageName = getRenderedParam(referenceService, context, map.get(IN_IMAGE_NAME));
                    InspectImageResponse response = remoteClient.inspect(imageName);
                    ((ObjectNode)outJson).put(OUT_IMAGE_ID, response.getId());
                    ((ObjectNode)outJson).put(OUT_IMAGE_AUTHOR, response.getAuthor());
                    ((ObjectNode)outJson).put(OUT_IMAGE_CONTAINER, response.getContainer());

                } else if (LIST_CONTAINERS.equalsIgnoreCase(action)) {

                    List<Container> containerList = remoteClient.listContainers();
                    outJson = factory.arrayNode();
                    for(Container container:containerList){
                        ((ArrayNode)outJson).add(createOutputJson(container,factory.objectNode()));
                    }

                } else if (START_CONTAINER.equalsIgnoreCase(action)) {

                    String name = getRenderedParam(referenceService, context, map.get(CONTAINER_NAME));
                    Container container = getContainerFromName(remoteClient, name);
                    if(!isStarted(container)) {
                        remoteClient.startContainer(container.getId());
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("started", container.getId());
                    } else {
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("already-started", container.getId());
                    }

                } else if (STOP_CONTAINER.equalsIgnoreCase(action)) {

                    String name = getRenderedParam(referenceService, context, map.get(CONTAINER_NAME));
                    Container container = getContainerFromName(remoteClient, name);
                    if(isStarted(container)) {
                        remoteClient.stopContainer(container.getId());
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("stopped", container.getId());
                    } else {
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("already-stopped", container.getId());
                    }

                } else if (DELETE_CONTAINER.equalsIgnoreCase(action)) {

                    String name = getRenderedParam(referenceService, context, map.get(CONTAINER_NAME));
                    Container container = getContainerFromName(remoteClient, name);
                    if(container != null) {
                        remoteClient.deleteContainer(container.getId());
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("deleted", container.getId());
                    } else {
                        outJson = factory.objectNode();
                        ((ObjectNode) outJson).put("container-not-found", name);
                    }

                } else {
                    // Creates empty node
                    outJson = factory.objectNode();
                }

                responseBodyRef = referenceService.register(outJson.toString(), 0, true, context);

                outputs.put(RESPONSE_BODY_KEY, responseBodyRef);
                callback.receiveResult(outputs, new int[0]);
            }
        });
    }


    private String getRenderedParam(ReferenceService referenceService, InvocationContext context, T2Reference key) {
        return (String) referenceService.renderIdentifier(key, String.class, context);

    }

    private JsonNode createOutputJson(Container container, ObjectNode out){
        out.put("id",container.getId());
        out.put("command",container.getCommand());
        out.put("created",container.getCreated());
        out.put("image",container.getImage());
        out.put("image-id",container.getImageId());
        out.put("status",container.getStatus());
        out.put("network-mode",container.getHostConfig().getNetworkMode());
    return out;
    }

    private boolean isStarted(Container container){
        return  container.getStatus() != null
                && container.getStatus().startsWith("Up");
    }

    private Container getContainerFromName(RemoteClient remoteClient, String containerName){
        List<Container> containerList = remoteClient.listContainers();
        for(Container container : containerList){
            if(container.getNames().length > 0){
                for(String name : container.getNames()){
                    if(name.endsWith(containerName)){
                        return container;
                    }
                }
            }
        }
    return null;
    }
}
