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

    private JsonNode activityConfig;
    private DockerContainerConfigurationImpl containerConfiguration;

    public DockerActivity(DockerContainerConfigurationImpl containerConfiguration) {
        this.containerConfiguration = containerConfiguration;
    }

    @Override
    public void configure(JsonNode activityConfig) throws ActivityConfigurationException {
      this.activityConfig = activityConfig;
    }

    @Override
    public JsonNode getConfiguration() {
        return activityConfig;
    }

    @Override
    public void executeAsynch(Map<String, T2Reference> map, final AsynchronousActivityCallback callback) {
        callback.requestRun(new Runnable() {
            @Override
            public void run() {
                Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
                T2Reference responseBodyRef = null;

                InvocationContext context = callback.getContext();
                ReferenceService referenceService = context.getReferenceService();

                DockerHttpResponse response = RESTUtil.createContainer(containerConfiguration);
                if(response != null && response.getStatusCode() == DockerHttpResponse.HTTP_201_CODE){
                    responseBodyRef = referenceService.register(response.getBody(), 0, true, context);
                } else {
                    ErrorDocument errorDocument = referenceService.getErrorDocumentService().registerError(response.getBody(),0,context);
                    responseBodyRef = referenceService.register(errorDocument, 0, true, context);
                }

                outputs.put("response_body", responseBodyRef);
                T2Reference statusRef = referenceService.register(response.getStatusCode(), 0, true, context);
                outputs.put("response_code", statusRef);
                //TODO add any more useful parameters to the output

                callback.receiveResult(outputs, new int[0]);

            }
        });
    }
}
