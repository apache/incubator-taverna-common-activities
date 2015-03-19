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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility functions for handling spreadsheet column labels and indexes.
 *
 * @author David Withers
 */
public class SpreadsheetUtils {

	/**
	 * Converts a column label to a (0 based) column index.
	 * <p>
	 * Label must match the format [A-Z]+ for result to be valid.
	 *
	 * @param column
	 *            the column label
	 * @return the (0 based) column index
	 */
	public static int getColumnIndex(String column) {
		int result = -1;
		char a = 'A' - 1;
		char[] chars = column.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int pos = (chars[i] - a);
			result += pos * Math.pow(26, chars.length - i - 1);
		}
		return result;
	}

	/**
	 * Converts a (0 based) column index to a column label.
	 *
	 * @param column
	 *            the (0 based) column index
	 * @return the column label
	 */
	public static String getColumnLabel(int column) {
		StringBuilder result = new StringBuilder();
		while (column >= 0) {
			result.insert(0, (char) ((char) (column % 26) + 'A'));
			column = (column / 26) - 1;
		}
		return result.toString();
	}

	/**
	 * Returns the port name for the column label.
	 *
	 * @param columnLabel
	 *            the column label
	 * @param columnNameMapping
	 * @return the port name for the column label
	 */
	public static String getPortName(String columnLabel, JsonNode jsonNode) {
		String portName = columnLabel;
		if (jsonNode != null && jsonNode.has("columnNames")) {
			for (JsonNode mappingNode : jsonNode.get("columnNames")) {
				if (columnLabel.equals(mappingNode.get("column").textValue())) {
					portName = mappingNode.get("port").textValue();
					break;
				}
			}
		}
		return portName;
	}

	/**
	 * Returns the port name for the column index.
	 *
	 * @param columnIndex
	 *            the column index
	 * @param columnNameMapping
	 * @return the port name for the column index
	 */
	public static String getPortName(int columnIndex, JsonNode jsonNode) {
		return getPortName(getColumnLabel(columnIndex), jsonNode);
	}

	/**
	 * @param jsonNode
	 * @return
	 */
	public static Range getRange(JsonNode jsonNode) {
		Range range = new Range();
		if (jsonNode != null) {
			if (jsonNode.has("start")) {
				range.setStart(jsonNode.get("start").intValue());
			}
			if (jsonNode.has("end")) {
				range.setEnd(jsonNode.get("end").intValue());
			}
			if (jsonNode.has("excludes")) {
				List<Range> excludes = new ArrayList<>();
				for (JsonNode rangeNode : jsonNode.get("excludes")) {
					excludes.add(getRange(rangeNode));
				}
				range.setExcludes(excludes);
			}
		}
		return range;
	}

}
