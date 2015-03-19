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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.TreeMap;

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

		SortedMap<Integer, String> currentDataRow = new TreeMap<Integer, String>();

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
								currentDataRow = new TreeMap<Integer, String>();
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
