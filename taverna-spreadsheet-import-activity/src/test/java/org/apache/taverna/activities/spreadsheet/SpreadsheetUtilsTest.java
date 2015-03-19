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

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils}.
 *
 * @author David Withers
 */
public class SpreadsheetUtilsTest {

	@Test
	public void testSpreadsheetUtils() {
		assertNotNull(new SpreadsheetUtils());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils#getColumnIndex(java.lang.String)}.
	 */
	@Test
	public void testGetColumnIndex() {
		assertEquals(0, SpreadsheetUtils.getColumnIndex("A"));
		assertEquals(4, SpreadsheetUtils.getColumnIndex("E"));
		assertEquals(25, SpreadsheetUtils.getColumnIndex("Z"));
		assertEquals(26, SpreadsheetUtils.getColumnIndex("AA"));
		assertEquals(457833, SpreadsheetUtils.getColumnIndex("ZAFZ"));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils#getColumnLabel(int)}.
	 */
	@Test
	public void testGetColumnLabel() {
		assertEquals("A", SpreadsheetUtils.getColumnLabel(0));
		assertEquals("E", SpreadsheetUtils.getColumnLabel(4));
		assertEquals("Z", SpreadsheetUtils.getColumnLabel(25));
		assertEquals("AA", SpreadsheetUtils.getColumnLabel(26));
		assertEquals("ZAFZ", SpreadsheetUtils.getColumnLabel(457833));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils#getPortName(java.lang.String, java.util.Map)}.
	 */
	@Test
	public void testGetPortNameStringMapOfStringString() {
		assertEquals("A", SpreadsheetUtils.getPortName("A", null));
		assertEquals("AABR", SpreadsheetUtils.getPortName("AABR", null));
		ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		ArrayNode columnNames = configuration.arrayNode();
		columnNames.addObject().put("column", "B").put("port", "beta");
		configuration.put("columnNames", columnNames);
		assertEquals("beta", SpreadsheetUtils.getPortName("B", configuration));
		assertEquals("T", SpreadsheetUtils.getPortName("T", configuration));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils#getPortName(int, java.util.Map)}.
	 */
	@Test
	public void testGetPortNameIntMapOfStringString() {
		assertEquals("A", SpreadsheetUtils.getPortName(0, null));
		assertEquals("AA", SpreadsheetUtils.getPortName(26, null));
		ObjectNode configuration = JsonNodeFactory.instance.objectNode();
		ArrayNode columnNames = configuration.arrayNode();
		columnNames.addObject().put("column", "D").put("port", "delta");
		configuration.put("columnNames", columnNames);
		assertEquals("delta", SpreadsheetUtils.getPortName(3, configuration));
		assertEquals("AB", SpreadsheetUtils.getPortName(27, configuration));
	}

}
