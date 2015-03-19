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

/**
 * Interface for reading a spreadsheet from an input stream.
 * 
 * @author David Withers
 */
public interface SpreadsheetReader {

	/**
	 * Reads an InputStream and passes spreadsheet cell data values, row by row, to the
	 * rowProcessor.
	 * 
	 * @param inputStream
	 *            the stream to read
	 * @param rowProcessor
	 *            the rowProcessor to write rows of data values to
	 * @param rowRange
	 *            the rows to read
	 * @param columnRange
	 *            the columns to read
	 * @param ignoreBlankRows
	 *            whether to ignore blank rows
	 * @throws SpreadsheetReadException
	 *             if there's an error reading the stream or the stream is not a valid spreadsheet
	 */
	public void read(InputStream inputStream, Range rowRange, Range columnRange, boolean ignoreBlankRows,
			SpreadsheetRowProcessor rowProcessor) throws SpreadsheetReadException;

}