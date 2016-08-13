/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.impl.EditsImpl;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlActivityFactoryTest {
	private static Logger logger = Logger.getLogger(CwlActivityFactoryTest.class);
	CwlActivityFactory cwlActivityFactory;
	public static final String CWL_CONF = "cwl_conf";
	private JsonNode root;

	@Before
	public void setUp() throws Exception {
		cwlActivityFactory = new CwlActivityFactory();
		Edits edits = new EditsImpl();
		cwlActivityFactory.setEdits(edits);

		Yaml reader = new Yaml();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode cwlFile = null;
		try {
			cwlFile = mapper.valueToTree(
					reader.load(getClass().getResourceAsStream("/customtool1.cwl")));
		} catch (IllegalArgumentException e) {
			
			logger.error(e.getMessage());
		}
		
		root = mapper.createObjectNode();
		((ObjectNode) root).put(CWL_CONF, cwlFile);
	}

	@Test
	public void testSchemaJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			assertEquals(cwlActivityFactory.getActivityConfigurationSchema(),
					objectMapper.readTree(getClass().getResource("/schema.json")));
		} catch (IOException e) {
			logger.error(e);
		}

	}

	@Test 
	public void testgetInputPorts() {
		Set<ActivityInputPort> set = null;
		try {
			set = cwlActivityFactory.getInputPorts(root);
		} catch (ActivityConfigurationException e) {
			logger.error(e);

		}
		assertEquals(3, set.size());
		Map<String, Integer> expected = new HashMap<>();
		expected.put("input_2", 1);
		expected.put("input_3", 0);
		expected.put("input_1", 0);
		Iterator<ActivityInputPort> itr = set.iterator();
		while (itr.hasNext()) {
			ActivityInputPort input = itr.next();
			int expectedDepth = expected.get(input.getName());
			assertEquals(expectedDepth, input.getDepth());
		}
	}

	@Test 
	public void testgetOutputPorts() {
		Set<ActivityOutputPort> set = null;
		try {
			set = cwlActivityFactory.getOutputPorts(root);
		} catch (ActivityConfigurationException e) {
			logger.error(e);

		}
		assertEquals(2, set.size());
		Map<String, Integer> expected = new HashMap<>();
		expected.put("output_1", 0);
		expected.put("output_2", 0);
		Iterator<ActivityOutputPort> itr = set.iterator();
		while (itr.hasNext()) {
			ActivityOutputPort output = itr.next();
			int expectedDepth = expected.get(output.getName());
			assertEquals(expectedDepth, output.getDepth());
		}
	}
}
