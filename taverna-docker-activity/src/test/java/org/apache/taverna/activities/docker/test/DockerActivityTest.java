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

package org.apache.taverna.activities.docker.test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.taverna.activities.docker.DockerActivity;
import org.apache.taverna.activities.docker.DockerContainerConfigurationImpl;
import org.apache.taverna.activities.docker.DockerRemoteConfig;
import org.apache.taverna.activities.testutils.ActivityInvoker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DockerActivityTest {

    private static final String IMAGE_NAME = "training/webapp";

    private static final String CONTAINER_NAME = "test-container";

	private ObjectNode activityConfiguration;

    private DockerContainerConfigurationImpl containerConfiguration;


	@Before
	public void setup() throws Exception {
        activityConfiguration = JsonNodeFactory.instance.objectNode();

        containerConfiguration = new DockerContainerConfigurationImpl(new TestConfigurationManager());
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfigurationImpl.NAME, CONTAINER_NAME);
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfigurationImpl.IMAGE,IMAGE_NAME);
        containerConfiguration.getInternalPropertyMap().put(DockerContainerConfigurationImpl.CMD,"env");

        DockerRemoteConfig remoteConfig = new DockerRemoteConfig();
        remoteConfig.setDockerHost("tcp://192.168.99.100:2376");
        remoteConfig.setApiVersion("1.21");
        remoteConfig.setDockerTlsVerify(true);

        // You need to copy your valid certificate file to resources directory in this test module as follows.
        remoteConfig.setDockerCertPath(new File("src/test/resources/cert").getAbsolutePath());
        remoteConfig.setRegistryUrl("https://registry-1.docker.io/v2");
        containerConfiguration.setDockerRemoteConfig(remoteConfig);

    }

	/**
	 * Tests a simple script (String output = input + "_returned") to ensure the script is invoked correctly.
	 * @throws Exception
	 */
	@Test
	public void testInspectImage() throws Exception {
		DockerActivity activity = new DockerActivity(containerConfiguration);
        activity.configure(activityConfiguration);

		Map<String,Object> inputs = new HashMap<String,Object>();
        inputs.put(DockerActivity.ACTION, DockerActivity.INSPECT);
        inputs.put(DockerActivity.IN_IMAGE_NAME, IMAGE_NAME);

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put(DockerActivity.RESPONSE_BODY_KEY, String.class);

		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
        System.out.println(outputs.get(DockerActivity.RESPONSE_BODY_KEY));
        Assert.assertNotNull(outputs.get(DockerActivity.RESPONSE_BODY_KEY));
	}
}
