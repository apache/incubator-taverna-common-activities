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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.ODFSpreadsheetReader}.
 *
 * @author David Withers
 */
public class ODFSpreadsheetReaderTest {

	private SpreadsheetReader spreadsheetReader;

	@Before
	public void setUp() throws Exception {
		spreadsheetReader = new ODFSpreadsheetReader();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.OdfSpreadsheetReader#read(java.io.InputStream, net.sf.taverna.t2.activities.spreadsheet.Range, net.sf.taverna.t2.activities.spreadsheet.Range, net.sf.taverna.t2.activities.spreadsheet.SpreadsheetRowProcessor)}.
	 */
	@Test
	public void testRead() throws Exception {
		String[] testFiles2 = new String[] { "/test-spreadsheet.ods" };
		for (int i = 0; i < testFiles2.length; i++) {
			final List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5));
			spreadsheetReader.read(getClass().getResourceAsStream(testFiles2[i]), new Range(0, 5), new Range(0, 4), false,
					new SpreadsheetRowProcessor() {

						public void processRow(int rowIndex, Map<Integer, String> row) {
							assertTrue(rows.remove((Integer) rowIndex));
							List<Integer> columns = new ArrayList<Integer>(Arrays.asList(0, 1, 2,
									3, 4));
							for (Entry<Integer, String> cell : row.entrySet()) {
								assertTrue(columns.remove(cell.getKey()));
								if (rowIndex == 0) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("true", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("2009-06-15", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 3 || rowIndex == 4) {
									if (cell.getKey().equals(4)) {
										assertNull(cell.getValue());
									} else {
										assertEquals("X", cell.getValue());
									}
								} else {
									assertNull(cell.getValue());
								}
							}
							assertTrue(columns.isEmpty());
						}
					});
			assertTrue(rows.isEmpty());

		}

	}

	@Test(expected=SpreadsheetReadException.class)
	public void testReadException() throws Exception {
		spreadsheetReader.read(new ByteArrayInputStream(new byte[0]), new Range(0,1), new Range(0,1), false, new SpreadsheetRowProcessor() {
			public void processRow(int rowIndex, Map<Integer, String> rowData) {				
			}
			
		});
	}	
	
	@Test
	public void testReadAllRows() throws Exception {
		String[] testFiles2 = new String[] { "/test-spreadsheet.ods" };
		for (int i = 0; i < testFiles2.length; i++) {
			final List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7,
					8, 9, 10, 11, 12, 13, 14));
			spreadsheetReader.read(getClass().getResourceAsStream(testFiles2[i]), new Range(0, -1), new Range(0, 4), false,
					new SpreadsheetRowProcessor() {

						public void processRow(int rowIndex, Map<Integer, String> row) {
							assertTrue(rows.remove((Integer) rowIndex));
							List<Integer> columns = new ArrayList<Integer>(Arrays.asList(0, 1, 2,
									3, 4));
							for (Entry<Integer, String> cell : row.entrySet()) {
								assertTrue(columns.remove(cell.getKey()));
								if (rowIndex == 0) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("true", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("2009-06-15", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 3 || rowIndex == 4) {
									if (cell.getKey().equals(4)) {
										assertNull(cell.getValue());
									} else {
										assertEquals("X", cell.getValue());
									}
								} else if (rowIndex == 5 || rowIndex == 6 || rowIndex == 7
										|| rowIndex == 8) {
									assertNull(cell.getValue());
								} else if (rowIndex == 9 || rowIndex == 10 || rowIndex == 11
										|| rowIndex == 12 || rowIndex == 13 || rowIndex == 14) {
									if (cell.getKey().equals(4)) {
										assertNull(cell.getValue());
									} else {
										assertEquals("y", cell.getValue());
									}
								}
							}
							assertTrue(columns.isEmpty());
						}
					});
			assertTrue(rows.isEmpty());
		}
	}

	@Test
	public void testIgnoreBlankRows() throws Exception {
		String[] testFiles2 = new String[] { "/test-spreadsheet.ods" };
		for (int i = 0; i < testFiles2.length; i++) {
			final List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 14));
			spreadsheetReader.read(getClass().getResourceAsStream(testFiles2[i]), new Range(0, -1), new Range(0, 4), true,
					new SpreadsheetRowProcessor() {

						public void processRow(int rowIndex, Map<Integer, String> row) {
							assertTrue(rows.remove((Integer) rowIndex));
							List<Integer> columns = new ArrayList<Integer>(Arrays.asList(0, 1, 2,
									3, 4));
							for (Entry<Integer, String> cell : row.entrySet()) {
								assertTrue(columns.remove(cell.getKey()));
								if (rowIndex == 0) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5.0", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("true", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("2009-06-15", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2.0", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 3 || rowIndex == 4) {
									if (cell.getKey().equals(4)) {
										assertNull(cell.getValue());
									} else {
										assertEquals("X", cell.getValue());
									}
								} else if (rowIndex == 9 || rowIndex == 10 || rowIndex == 11
										|| rowIndex == 12 || rowIndex == 13 || rowIndex == 14) {
									if (cell.getKey().equals(4)) {
										assertNull(cell.getValue());
									} else {
										assertEquals("y", cell.getValue());
									}
								}
							}
							assertTrue(columns.isEmpty());
						}
					});
			assertTrue(rows.isEmpty());
		}
	}

}
