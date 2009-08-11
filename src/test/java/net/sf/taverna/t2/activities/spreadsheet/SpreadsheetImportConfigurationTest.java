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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration}.
 *
 * @author David Withers
 */
public class SpreadsheetImportConfigurationTest {

	private SpreadsheetImportConfiguration configuration;
	
	@Before
	public void setUp() throws Exception {
		configuration = new SpreadsheetImportConfiguration();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#SpreadsheetImportConfiguration()}.
	 */
	@Test
	public void testSpreadsheetImportConfiguration() {
		assertNotNull(configuration);
		assertEquals(new Range(0, 1), configuration.getColumnRange());
		assertEquals(new Range(0, -1), configuration.getRowRange());
		assertEquals("", configuration.getEmptyCellValue());
		assertEquals(SpreadsheetEmptyCellPolicy.EMPTY_STRING, configuration.getEmptyCellPolicy());
		assertEquals(Collections.EMPTY_MAP, configuration.getColumnNames());
		assertEquals(true, configuration.isAllRows());
		assertEquals(false, configuration.isExcludeFirstRow());
		assertEquals(false, configuration.isIgnoreBlankRows());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#SpreadsheetImportConfiguration(net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration)}.
	 */
	@Test
	public void testSpreadsheetImportConfigurationSpreadsheetImportConfiguration() {
		configuration.setColumnRange(new Range(3, 22));
		configuration.setColumnRange(new Range(2, 53));
		configuration.setAllRows(false);
		configuration.setExcludeFirstRow(true);
		configuration.setIgnoreBlankRows(true);
		configuration.setEmptyCellPolicy(SpreadsheetEmptyCellPolicy.GENERATE_ERROR);
		configuration.setEmptyCellValue("NO VALUE");
		configuration.setColumnNames(Collections.singletonMap("D", "delta"));
		SpreadsheetImportConfiguration newConfiguration = new SpreadsheetImportConfiguration(configuration);
		assertEquals(configuration, newConfiguration);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#getEmptyCellValue()}.
	 */
	@Test
	public void testGetEmptyCellValue() {
		assertEquals("", configuration.getEmptyCellValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setEmptyCellValue(java.lang.String)}.
	 */
	@Test
	public void testSetEmptyCellValue() {
		configuration.setEmptyCellValue("XXXX");
		assertEquals("XXXX", configuration.getEmptyCellValue());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#getColumnRange()}.
	 */
	@Test
	public void testGetColumnRange() {
		assertEquals(new Range(0, 1), configuration.getColumnRange());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setColumnRange(net.sf.taverna.t2.activities.spreadsheet.Range)}.
	 */
	@Test
	public void testSetColumnRange() {
		configuration.setColumnRange(new Range(5, 89));
		assertEquals(new Range(5, 89), configuration.getColumnRange());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#getRowRange()}.
	 */
	@Test
	public void testGetRowRange() {
		assertEquals(new Range(0, -1), configuration.getRowRange());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setRowRange(net.sf.taverna.t2.activities.spreadsheet.Range)}.
	 */
	@Test
	public void testSetRowRange() {
		configuration.setRowRange(new Range(41, 67));
		assertEquals(new Range(41, 67), configuration.getRowRange());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#getColumnNames()}.
	 */
	@Test
	public void testGetColumnNames() {
		assertEquals(Collections.EMPTY_MAP, configuration.getColumnNames());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setColumnNames(java.util.Map)}.
	 */
	@Test
	public void testSetColumnNames() {
		configuration.setColumnNames(Collections.singletonMap("A", "alpha"));
		assertEquals(Collections.singletonMap("A", "alpha"), configuration.getColumnNames());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#isAllRows()}.
	 */
	@Test
	public void testIsAllRows() {
		assertEquals(true, configuration.isAllRows());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setAllRows(boolean)}.
	 */
	@Test
	public void testSetAllRows() {
		configuration.setAllRows(false);
		assertEquals(false, configuration.isAllRows());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#isExcludeFirstRow()}.
	 */
	@Test
	public void testIsExcludeFirstRow() {
		assertEquals(false, configuration.isExcludeFirstRow());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setExcludeFirstRow(boolean)}.
	 */
	@Test
	public void testSetExcludeFirstRow() {
		configuration.setExcludeFirstRow(true);
		assertEquals(true, configuration.isExcludeFirstRow());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#isIgnoreBlankRows()}.
	 */
	@Test
	public void testIsIgnoreBlankRows() {
		assertEquals(false, configuration.isIgnoreBlankRows());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setIgnoreBlankRows(boolean)}.
	 */
	@Test
	public void testSetIgnoreBlankRows() {
		configuration.setIgnoreBlankRows(true);
		assertEquals(true, configuration.isIgnoreBlankRows());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#getEmptyCellPolicy()}.
	 */
	@Test
	public void testGetEmptyCellPolicy() {
		assertEquals(SpreadsheetEmptyCellPolicy.EMPTY_STRING, configuration.getEmptyCellPolicy());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#setEmptyCellPolicy(net.sf.taverna.t2.activities.spreadsheet.SpreadsheetEmptyCellPolicy)}.
	 */
	@Test
	public void testSetEmptyCellPolicy() {
		configuration.setEmptyCellPolicy(SpreadsheetEmptyCellPolicy.GENERATE_ERROR);
		assertEquals(SpreadsheetEmptyCellPolicy.GENERATE_ERROR, configuration.getEmptyCellPolicy());
		configuration.setEmptyCellPolicy(SpreadsheetEmptyCellPolicy.USER_DEFINED);
		assertEquals(SpreadsheetEmptyCellPolicy.USER_DEFINED, configuration.getEmptyCellPolicy());
		configuration.setEmptyCellPolicy(SpreadsheetEmptyCellPolicy.EMPTY_STRING);
		assertEquals(SpreadsheetEmptyCellPolicy.EMPTY_STRING, configuration.getEmptyCellPolicy());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertTrue(configuration.equals(configuration));
		assertTrue(configuration.equals(new SpreadsheetImportConfiguration()));
		assertFalse(configuration.equals(null));
		configuration.setEmptyCellValue("NIL");
		assertFalse(configuration.equals(new SpreadsheetImportConfiguration()));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportConfiguration#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(configuration.hashCode(), configuration.hashCode());
		assertEquals(configuration.hashCode(), new SpreadsheetImportConfiguration().hashCode());
	}

}
