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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workflowmodel.impl.EditsImpl;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker}.
 *
 * @author David Withers
 */
public class SpreadsheetImportHealthCheckerTest {

	private SpreadsheetImportHealthChecker healthChecker;

	private SpreadsheetImportActivity activity;
	private ArrayList ancestors;

	@Before
	public void setUp() throws Exception {
		EditsImpl ei = new EditsImpl();
		healthChecker = new SpreadsheetImportHealthChecker();
		activity = new SpreadsheetImportActivity();
		activity.setEdits(new EditsImpl());
		ancestors = new ArrayList();
		ancestors.add(ei.createProcessor("fred"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker#canHandle(java.lang.Object)}.
	 */
	@Test
	public void testCanHandle() {
		assertTrue(healthChecker.canVisit(activity));
		assertFalse(healthChecker.canVisit(null));
		assertFalse(healthChecker.canVisit(""));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker#checkHealth(net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportActivity)}.
	 * @throws Exception
	 */
	@Test
	public void testCheckHealth() throws Exception {
		assertEquals(Status.SEVERE, healthChecker.visit(activity, ancestors).getStatus());
		ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		configuration.put("columnRange", configuration.objectNode().put("start", 0).put("end", 1));
		configuration.put("rowRange", configuration.objectNode().put("start", 0).put("end", -1));
		configuration.put("emptyCellValue", "");
		configuration.put("allRows", true);
		configuration.put("excludeFirstRow", false);
		configuration.put("ignoreBlankRows", false);
		configuration.put("emptyCellPolicy", "EMPTY_STRING");
		configuration.put("outputFormat", "PORT_PER_COLUMN");
		configuration.put("csvDelimiter", ",");
		activity.configure(configuration);
		assertEquals(Status.OK, healthChecker.visit(activity, ancestors).getStatus());
	}

}
