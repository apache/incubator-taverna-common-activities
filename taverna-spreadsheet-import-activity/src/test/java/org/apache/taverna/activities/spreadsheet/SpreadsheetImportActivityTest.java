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

package org.apache.taverna.activities.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for {@link org.apache.taverna.activities.spreadsheet.SpreadsheetImportActivityTest}.
 *
 * @author David Withers
 */
public class SpreadsheetImportActivityTest {

	private SpreadsheetImportActivity activity;
	private SpreadsheetImportActivityFactory activityFactory;
	private Edits edits;
	private ObjectNode configuration;

	@Before
	public void setUp() throws Exception {
		activity = new SpreadsheetImportActivity();
		activityFactory = new SpreadsheetImportActivityFactory();
		edits = new EditsImpl();
		activityFactory.setEdits(edits);
		configuration = JsonNodeFactory.instance.objectNode();
		configuration.put("columnRange", configuration.objectNode().put("start", 0).put("end", 1));
		configuration.put("rowRange", configuration.objectNode().put("start", 0).put("end", -1));
		configuration.put("emptyCellValue", "");
		configuration.put("allRows", true);
		configuration.put("excludeFirstRow", false);
		configuration.put("ignoreBlankRows", false);
		configuration.put("emptyCellPolicy", "EMPTY_STRING");
		configuration.put("outputFormat", "PORT_PER_COLUMN");
		configuration.put("csvDelimiter", ",");
	}

	@Test
	public void testSpreadsheetImportActivity() {
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testConfigureSpreadsheetImportConfiguration() throws Exception {
		assertEquals(0, activity.getInputPorts().size());
		assertEquals(0, activity.getOutputPorts().size());
		configuration.put("columnRange", configuration.objectNode().put("start", 0).put("end", 10));
		ArrayNode columnNames = configuration.arrayNode();
		columnNames.addObject().put("column", "C").put("port", "test");
		configuration.put("columnNames", columnNames);
		activity.configure(configuration);
		for (ActivityInputPort activityInputPort : activityFactory.getInputPorts(configuration)) {
			edits.getAddActivityInputPortEdit(activity, activityInputPort).doEdit();
		}
		for (ActivityOutputPort activityOutputPort : activityFactory.getOutputPorts(configuration)) {
			edits.getAddActivityOutputPortEdit(activity, activityOutputPort).doEdit();
		}
		assertEquals(configuration, activity.getConfiguration());
		assertEquals(1, activity.getInputPorts().size());
		Set<ActivityOutputPort> outputPorts = activity.getOutputPorts();
		int[] rangeValues = SpreadsheetUtils.getRange(configuration.get("columnRange")).getRangeValues();
		assertEquals(rangeValues.length, outputPorts.size());
		for (int i = 0; i < rangeValues.length; i++) {
			String portName = SpreadsheetUtils.getPortName(rangeValues[i], configuration);
			Port port = null;
			for (Port outputPort : outputPorts) {
				if (outputPort.getName().equals(portName)) {
					port = outputPort;
					break;
				}
			}
			assertNotNull(port);
			outputPorts.remove(port);
		}
		assertEquals(0, outputPorts.size());

		configuration.put("outputFormat", SpreadsheetOutputFormat.SINGLE_PORT.name());
		activity.configure(configuration);
		assertEquals(1, activityFactory.getOutputPorts(configuration).size());
	}

	@Test
	public void testGetConfiguration() throws ActivityConfigurationException {
		assertNull(activity.getConfiguration());
		activity.configure(configuration);
		assertNotNull(activity.getConfiguration());
		assertEquals(configuration, activity.getConfiguration());
	}

	@Test
	public void testExecuteAsynchMapOfStringT2ReferenceAsynchronousActivityCallback() throws Exception {
		configuration.put("columnRange", configuration.objectNode().put("start", 0).put("end", 3));
		activity.configure(configuration);
		for (ActivityInputPort activityInputPort : activityFactory.getInputPorts(configuration)) {
			edits.getAddActivityInputPortEdit(activity, activityInputPort).doEdit();
		}
		for (ActivityOutputPort activityOutputPort : activityFactory.getOutputPorts(configuration)) {
			edits.getAddActivityOutputPortEdit(activity, activityOutputPort).doEdit();
		}
		Map<String, Class<?>> outputs = new HashMap<String, Class<?>>();
		outputs.put("A", String.class);
		outputs.put("B", String.class);
		outputs.put("C", String.class);
		outputs.put("D", String.class);
		Map<String, Object> results = ActivityInvoker.invokeAsyncActivity(activity, Collections.singletonMap("fileurl",
				(Object) "src/test/resources/test-spreadsheet.xls"), outputs);
		assertEquals(4, results.size());
		assertTrue(results.get("A") instanceof List<?>);
		assertEquals(15, ((List<?>) results.get("A")).size());
		results = ActivityInvoker.invokeAsyncActivity(activity, Collections.singletonMap("fileurl",
				(Object) "src/test/resources/test-spreadsheet.ods"), outputs);
		assertEquals(4, results.size());
		assertTrue(results.get("A") instanceof List<?>);
		assertEquals(15, ((List<?>) results.get("A")).size());
		results = ActivityInvoker.invokeAsyncActivity(activity, Collections.singletonMap("fileurl",
				(Object) "src/test/resources/test-spreadsheet.csv"), outputs);
		assertEquals(4, results.size());
		assertTrue(results.get("A") instanceof List<?>);
		assertEquals(15, ((List<?>) results.get("A")).size());

		// CSV output
		configuration.put("outputFormat", SpreadsheetOutputFormat.SINGLE_PORT.name());
		activity.configure(configuration);
		outputs = new HashMap<String, Class<?>>();
		outputs.put("output", String.class);
		results = ActivityInvoker.invokeAsyncActivity(activity, Collections.singletonMap("fileurl",
				(Object) "src/test/resources/test-spreadsheet.xls"), outputs);
		assertEquals(1, results.size());
		assertTrue(results.get("output") instanceof String);
		assertEquals(15, ((String) results.get("output")).split(System.getProperty("line.separator")).length);

		// TSV output
		configuration.put("csvDelimiter", "\t");
		activity.configure(configuration);
		results = ActivityInvoker.invokeAsyncActivity(activity, Collections.singletonMap("fileurl",
				(Object) "src/test/resources/test-spreadsheet.csv"), outputs);
		assertEquals(1, results.size());
		assertTrue(results.get("output") instanceof String);
		assertEquals(15, ((String) results.get("output")).split(System.getProperty("line.separator")).length);
	}

}
