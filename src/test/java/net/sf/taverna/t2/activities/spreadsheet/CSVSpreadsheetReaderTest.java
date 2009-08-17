package net.sf.taverna.t2.activities.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

public class CSVSpreadsheetReaderTest {

	private SpreadsheetReader spreadsheetReader;

	@Before
	public void setUp() throws Exception {
		spreadsheetReader = new CSVSpreadsheetReader();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.OdfSpreadsheetReader#read(java.io.InputStream, net.sf.taverna.t2.activities.spreadsheet.Range, net.sf.taverna.t2.activities.spreadsheet.Range, net.sf.taverna.t2.activities.spreadsheet.SpreadsheetRowProcessor)}.
	 */
	@Test
	public void testRead() throws Exception {
		String[] testFiles2 = new String[] { "/test-spreadsheet.csv" };
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
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("TRUE", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("15/06/09", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2", cell.getValue());
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
		spreadsheetReader.read(new InputStream() {
			public int read() throws IOException {
				throw new IOException();
			}			
		}, new Range(0,1), new Range(0,1), false, new SpreadsheetRowProcessor() {
			public void processRow(int rowIndex, Map<Integer, String> rowData) {				
			}			
		});
	}	
	
	@Test
	public void testReadAllRows() throws Exception {
		String[] testFiles2 = new String[] { "/test-spreadsheet.csv" };
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
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("TRUE", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("15/06/09", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2", cell.getValue());
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
		String[] testFiles2 = new String[] { "/test-spreadsheet.csv" };
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
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 1) {
									if (cell.getKey().equals(0)) {
										assertEquals("A", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("5", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertEquals("C", cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("1", cell.getValue());
									} else {
										assertNull(cell.getValue());
									}
								} else if (rowIndex == 2) {
									if (cell.getKey().equals(0)) {
										assertEquals("TRUE", cell.getValue());
									} else if (cell.getKey().equals(1)) {
										assertEquals("15/06/09", cell.getValue());
									} else if (cell.getKey().equals(2)) {
										assertNull(cell.getValue());
									} else if (cell.getKey().equals(3)) {
										assertEquals("2", cell.getValue());
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
