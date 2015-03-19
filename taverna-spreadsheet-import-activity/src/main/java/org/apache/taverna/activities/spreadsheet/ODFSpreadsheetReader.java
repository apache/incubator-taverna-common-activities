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

import java.io.InputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.w3c.dom.NodeList;

/**
 * Reads Open Document Format (ODF) spreadsheet files.
 * 
 * @author David Withers
 */
public class ODFSpreadsheetReader implements SpreadsheetReader {

	private static Logger logger = Logger.getLogger(ODFSpreadsheetReader.class);

	public void read(InputStream inputStream, Range rowRange, Range columnRange, boolean ignoreBlankRows, SpreadsheetRowProcessor rowProcessor)
			throws SpreadsheetReadException {
		NodeList rowList = null;
		try {
			// Load the ODF document
			OdfDocument odfDoc = OdfDocument.loadDocument(inputStream);
			logger.debug("Reading document of type : " + odfDoc.getMediaType());
			// Get the content as DOM tree
			OdfFileDom odfContent = odfDoc.getContentDom();
			// Initialize XPath
			XPath xpath = odfDoc.getXPath();
			// Get the rows of the first table
			String rowsPath = ("//table:table[1]/table:table-row");
			rowList = (NodeList) xpath.evaluate(rowsPath, odfContent, XPathConstants.NODESET);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new SpreadsheetReadException("The spreadsheet file could not be read", e);
		}

		if (rowRange.getEnd() < 0) {
			rowRange.setEnd(calculateRowCount(rowList) - 1);
		}

		SortedMap<Integer, String> currentDataRow = new TreeMap<Integer, String>();
		int rowRep = 0;
		for (int rowIndex = rowRange.getStart(); rowIndex <= rowRange.getEnd(); rowIndex++) {
			boolean blankRow = true;
			OdfTableRow row = (OdfTableRow) rowList.item(rowIndex);
			int columnRep = 0;
			for (int columnIndex = columnRange.getStart(); columnIndex <= columnRange.getEnd(); columnIndex++) {
				String value = null;
				OdfTableCell cell = null;
				if (row != null) {
					cell = (OdfTableCell) row.getCellAt(columnIndex);
					if (cell != null) {
						String type = cell.getOfficeValueTypeAttribute();
						if ("float".equals(type)) {
							value = cell.getOfficeValueAttribute().toString();
						} else if ("percentage".equals(type)) {
							value = cell.getOfficeValueAttribute().toString();
						} else if ("currency".equals(type)) {
							value = cell.getOfficeValueAttribute().toString();
						} else if ("date".equals(type)) {
							value = cell.getOfficeDateValueAttribute();
						} else if ("time".equals(type)) {
							value = cell.getOfficeTimeValueAttribute();
						} else if ("boolean".equals(type)) {
							value = cell.getOfficeBooleanValueAttribute().toString();
						} else if ("string".equals(type)) {
							value = cell.getOfficeStringValueAttribute();
							if (value == null) {
								value = cell.getTextContent();
							}
						} else {
							value = cell.getTextContent();
						}
					}
				}
				value = "".equals(value) ? null : value;
				if (value != null) {
					blankRow = false;
				}
				// if the cell is within the column range add it to the row values
				if (columnRange.contains(columnIndex + columnRep)) {
					currentDataRow.put(columnIndex + columnRep, value);
				}
				// check if this cell is repeated
				int repeatedCells = cell == null ? 0 : cell
						.getTableNumberColumnsRepeatedAttribute() - 1;
				while (repeatedCells > 0 && columnIndex + columnRep < columnRange.getEnd()) {
					columnRep++;
					if (columnRange.contains(columnIndex + columnRep)) {
						currentDataRow
								.put(columnIndex + columnRep, value);
					}
					repeatedCells--;
				}
				// if it's the last cell in the range process the row
				if (columnIndex == columnRange.getEnd()) {
					if (rowRange.contains(rowIndex + rowRep)) {
						if (!ignoreBlankRows || !blankRow) {
							rowProcessor.processRow(rowIndex + rowRep, currentDataRow);
						}
					}
					// check if this row is repeated
					int repeatedRows = row == null ? 0
							: row.getTableNumberRowsRepeatedAttribute() - 1;
					while (repeatedRows > 0 && rowIndex + rowRep < rowRange.getEnd()) {
						rowRep++;
						if (rowRange.contains(rowIndex + rowRep)) {
							if (!ignoreBlankRows || !blankRow) {
								rowProcessor.processRow(rowIndex + rowRep, currentDataRow);
							}
						}
						repeatedRows--;
					}
					currentDataRow = new TreeMap<Integer, String>();
				}

			}
		}

	}

	/**
	 * Calculates the number of rows in a table, ignoring blank rows at the end of the table.
	 * 
	 * @param rowList
	 *            the list of rows in a table
	 * @return the number of rows in a table
	 */
	private int calculateRowCount(NodeList rowList) {
		int rowCount = 0;
		int blankRows = 0;
		for (int i = 0; i < rowList.getLength(); i++) {
			OdfTableRow row = (OdfTableRow) rowList.item(i);
			int repeatedRows = row.getTableNumberRowsRepeatedAttribute();
			if (isBlankRow(row)) {
				blankRows += repeatedRows;
			} else {
				rowCount += repeatedRows + blankRows;
				blankRows = 0;
			}
		}
		return rowCount;
	}

	/**
	 * Returns <code>true</code> if a row is blank (non of the cells contain data).
	 * 
	 * @param row
	 * @return <code>true</code> if a row is blank
	 */
	private boolean isBlankRow(OdfTableRow row) {
		OdfTableCell cell = (OdfTableCell) row.getCellAt(0);
		for (int i = 1; cell != null; i++) {
			String cellContent = cell.getTextContent();
			if (cellContent != null && !cellContent.equals("")) {
				return false;
			}
			cell = (OdfTableCell) row.getCellAt(i);
		}
		return true;
	}

}