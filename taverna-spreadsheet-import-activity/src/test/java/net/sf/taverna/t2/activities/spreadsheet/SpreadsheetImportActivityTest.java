/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.EditException;
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
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportActivityTest}.
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
