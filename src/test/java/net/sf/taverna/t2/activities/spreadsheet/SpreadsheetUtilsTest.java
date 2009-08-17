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

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

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
		assertEquals("beta", SpreadsheetUtils.getPortName("B", Collections.singletonMap("B", "beta")));
		assertEquals("T", SpreadsheetUtils.getPortName("T", Collections.singletonMap("B", "beta")));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetUtils#getPortName(int, java.util.Map)}.
	 */
	@Test
	public void testGetPortNameIntMapOfStringString() {
		assertEquals("A", SpreadsheetUtils.getPortName(0, null));
		assertEquals("AA", SpreadsheetUtils.getPortName(26, null));
		assertEquals("delta", SpreadsheetUtils.getPortName(3, Collections.singletonMap("D", "delta")));
		assertEquals("AB", SpreadsheetUtils.getPortName(27, Collections.singletonMap("D", "beta")));
	}

}
