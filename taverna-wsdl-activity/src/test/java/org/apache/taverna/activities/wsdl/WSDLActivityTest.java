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

package org.apache.taverna.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.activities.testutils.ActivityInvoker;
import org.apache.taverna.activities.testutils.LocationConstants;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WSDLActivityTest implements LocationConstants {

	private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;

	private static WSDLActivity activity;
	private static ObjectNode configBean;
	private static ObjectNode operationConfigBean;
	private static String wsdlLocation = WSDL_TEST_BASE
			+ "eutils/eutils_lite.wsdl";

	//@BeforeClass
    @Ignore("Integration test")
	public static void setUp() throws Exception {
		activity = new WSDLActivity(null);
		configBean = JSON_NODE_FACTORY.objectNode();
		operationConfigBean = configBean.objectNode();
		configBean.put("operation", operationConfigBean);
		operationConfigBean.put("name", "run_eInfo");
		operationConfigBean.put("wsdl", wsdlLocation);
		activity.configure(configBean);
	}

	@Test
    @Ignore("Integration test")
	public void testConfigureWSDLActivityConfigurationBean() throws Exception {
		assertEquals("There should be 1 input ports", 1, activity
				.getInputPorts().size());
		assertEquals("There should be 2 output ports", 2, activity
				.getOutputPorts().size());

		assertEquals("parameters", activity.getInputPorts().iterator().next()
				.getName());

		List<String> expectedOutputNames = new ArrayList<String>();
		expectedOutputNames.add("parameters");
		expectedOutputNames.add("attachmentList");
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue("Unexpected output name:" + port.getName(),
					expectedOutputNames.contains(port.getName()));
			expectedOutputNames.remove(port.getName());
		}
		assertEquals(
				"Not all of the expected outputs were found, those remainng are:"
						+ expectedOutputNames.toArray(), 0, expectedOutputNames
						.size());
	}

	@Test
    @Ignore("Integration test")
	public void testGetConfiguration() throws Exception {
		assertSame(configBean, activity.getConfiguration());
	}

	@Test
	@Ignore("Service is broken")
	public void testExecuteAsynchMapOfStringEntityIdentifierAsynchronousActivityCallback()
			throws Exception {
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters", "<parameters><db>pubmed</db></parameters>");

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("parameters", String.class);

		Map<String, Object> outputMap = ActivityInvoker.invokeAsyncActivity(
				activity, inputMap, expectedOutputs);
		assertNotNull("there should be an output named parameters", outputMap
				.get("parameters"));
		String xml;
		if (outputMap.get("parameters") instanceof String) {
			xml = (String) outputMap.get("parameters");
		} else {
			byte[] bytes = (byte[]) outputMap.get("parameters");
			xml = new String(bytes);
		}

		assertTrue("the xml is not what was expected", xml
				.contains("<DbName>pubmed</DbName>"));
	}

    @Ignore("Integration test")
	@Test
	public void testGetTypeDescriptorForOutputPort() throws Exception {
		assertNotNull("The type for the port 'paremeters' could not be found",
				activity.getTypeDescriptorForOutputPort("parameters"));
		assertTrue(
				"The type for the port 'paremeters' shoule be complex",
				activity.getTypeDescriptorForOutputPort("parameters") instanceof ComplexTypeDescriptor);
		assertNull("There should be no type descriptor for 'fred' port",
				activity.getTypeDescriptorForOutputPort("fred"));
	}

    @Ignore("Integration test")
	@Test
	public void testGetTypeDescriptorForInputPort() throws Exception {
		assertNotNull("The type for the port 'parameters' could not be found",
				activity.getTypeDescriptorForInputPort("parameters"));
		assertTrue(
				"The type for the port 'parameters' shoule be complex",
				activity.getTypeDescriptorForInputPort("parameters") instanceof ComplexTypeDescriptor);
		assertNull("There should be no type descriptor for 'fred' port",
				activity.getTypeDescriptorForInputPort("fred"));
	}

}
