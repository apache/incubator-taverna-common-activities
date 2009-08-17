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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.csvreader.CsvReader;

/**
 * Reads CSV files.
 *
 * @author David Withers
 */
public class CSVSpreadsheetReader implements SpreadsheetReader {

	public void read(InputStream inputStream, Range rowRange,
			Range columnRange, boolean ignoreBlankRows,
			SpreadsheetRowProcessor rowProcessor)
			throws SpreadsheetReadException {
		CsvReader csvReader = new CsvReader(new InputStreamReader(inputStream));
		csvReader.setSkipEmptyRecords(false);

		Map<Integer, String> currentDataRow = new HashMap<Integer, String>();

		try {
			while(csvReader.readRecord()) {
				int rowIndex = (int) csvReader.getCurrentRecord();
				boolean blankRow = true;
				if (rowRange.contains(rowIndex)) {
					for (int columnIndex = columnRange.getStart(); columnIndex <= columnRange.getEnd(); columnIndex++) {
						if (columnRange.contains(columnIndex)) {
							String value = csvReader.get(columnIndex);
							value = "".equals(value) ? null : value;
							if (value != null) {
								blankRow = false;
							}
							currentDataRow.put(columnIndex, value);
							if (columnIndex == columnRange.getEnd()) {
								if (!ignoreBlankRows || !blankRow) {
									rowProcessor.processRow(rowIndex, currentDataRow);
								}
								currentDataRow = new HashMap<Integer, String>();
							}
						}
					}
				
					if (rowIndex == rowRange.getEnd()) {
						break;
					}
				}
			}
		} catch (IOException e) {
			throw new SpreadsheetReadException("Unable to read CSV file", e);
		}
	}

}
