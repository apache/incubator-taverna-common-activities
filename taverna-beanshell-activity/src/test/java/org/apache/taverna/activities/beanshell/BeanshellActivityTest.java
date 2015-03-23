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

package org.apache.taverna.activities.beanshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.activities.testutils.ActivityInvoker;
import org.apache.taverna.workflowmodel.AbstractPort;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.impl.EditsImpl;
import org.apache.taverna.workflowmodel.processor.activity.impl.ActivityInputPortImpl;
import org.apache.taverna.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Beanshell Activity Tests
 * @author Stuart Owen
 *
 */
public class BeanshellActivityTest {

	private ObjectNode configuration;

	@Before
	public void setup() throws Exception {
		configuration = JsonNodeFactory.instance.objectNode();
		configuration.put("classLoaderSharing", "workflow");
	}

	/**
	 * Tests a simple script (String output = input + "_returned") to ensure the script is invoked correctly.
	 * @throws Exception
	 */
	@Test
	public void simpleScript() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		Edits edits = new EditsImpl();
		edits.getAddActivityInputPortEdit(activity, new ActivityInputPortImpl("input", 0, false, null, String.class)).doEdit();
		edits.getAddActivityOutputPortEdit(activity, new ActivityOutputPortImpl("output", 0, 0)).doEdit();

		configuration.put("script", "String output = input + \"_returned\";");

		activity.configure(configuration);
		assertEquals("There should be 1 input port",1,activity.getInputPorts().size());
		assertEquals("There should be 1 output port",1,activity.getOutputPorts().size());

		assertEquals("The input should be called input", "input",((AbstractPort)activity.getInputPorts().toArray()[0]).getName());
		assertEquals("The output should be called output", "output",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());

		Map<String,Object> inputs = new HashMap<String, Object>();
		inputs.put("input", "aString");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("output", String.class);

		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertTrue("there should be an output named output",outputs.containsKey("output"));
		assertEquals("output should have the value aString_returned","aString_returned",outputs.get("output"));
	}
}
