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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
			e.printStackTrace();
			throw new SpreadsheetReadException("The spreadsheet file could not be read", e);
		}

		if (rowRange.getEnd() < 0) {
			rowRange.setEnd(calculateRowCount(rowList) - 1);
		}

		Map<Integer, String> currentDataRow = new HashMap<Integer, String>();
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
					currentDataRow = new HashMap<Integer, String>();
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