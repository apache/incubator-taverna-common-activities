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

import java.util.Map;

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
	public static String getPortName(String columnLabel, Map<String, String> columnNameMapping) {
		String portName;
		if (columnNameMapping != null && columnNameMapping.containsKey(columnLabel)) {
			portName = columnNameMapping.get(columnLabel);
		} else {
			portName = columnLabel;
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
	public static String getPortName(int columnIndex, Map<String, String> columnNameMapping) {
		return getPortName(getColumnLabel(columnIndex), columnNameMapping);
	}

}
