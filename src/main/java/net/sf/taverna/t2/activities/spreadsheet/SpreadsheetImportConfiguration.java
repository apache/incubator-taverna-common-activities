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

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the SpreadsheetImport activity.
 * 
 * @author David Withers
 */
public class SpreadsheetImportConfiguration {

	private Range columnRange = new Range(0, 1);
	private Range rowRange = new Range(0, -1);
	private String emptyCellValue = "";
	private Map<String, String> columnNames = new HashMap<String, String>();
	private boolean allRows = true;
	private boolean excludeFirstRow = false;
	private boolean ignoreBlankRows = false;
	private SpreadsheetEmptyCellPolicy emptyCellPolicy = SpreadsheetEmptyCellPolicy.EMPTY_STRING;

	/**
	 * Constructs a new SpreadsheetImportConfiguration.
	 */
	public SpreadsheetImportConfiguration() {
	}

	/**
	 * Constructs a new SpreadsheetImportConfiguration that copies the values from the given
	 * configuration.
	 * 
	 * @param configuration
	 */
	public SpreadsheetImportConfiguration(SpreadsheetImportConfiguration configuration) {
		this.columnRange = configuration.columnRange;
		this.rowRange = configuration.rowRange;
		this.emptyCellValue = configuration.emptyCellValue;
		this.columnNames = new HashMap<String, String>(configuration.columnNames);
		this.allRows = configuration.allRows;
		this.excludeFirstRow = configuration.excludeFirstRow;
		this.emptyCellPolicy = configuration.emptyCellPolicy;
		this.ignoreBlankRows = configuration.ignoreBlankRows;
	}

	/**
	 * Returns the columnRange.
	 * 
	 * @return the value of columnRange
	 */
	public Range getColumnRange() {
		return columnRange;
	}

	/**
	 * Sets the columnRange.
	 * 
	 * @param columnRange
	 *            the new value for columnRange
	 */
	public void setColumnRange(Range columnRange) {
		this.columnRange = columnRange;
	}

	/**
	 * Returns the rowRange.
	 * 
	 * @return the value of rowRange
	 */
	public Range getRowRange() {
		return rowRange;
	}

	/**
	 * Sets the rowRange.
	 * 
	 * @param rowRange
	 *            the new value for rowRange
	 */
	public void setRowRange(Range rowRange) {
		this.rowRange = rowRange;
	}

	/**
	 * Returns the emptyCellValue. The default value is "".
	 * 
	 * @return the value of emptyCellValue
	 */
	public String getEmptyCellValue() {
		return emptyCellValue;
	}

	/**
	 * Sets the emptyCellValue.
	 * 
	 * @param emptyCellValue
	 *            the new value for emptyCellValue
	 */
	public void setEmptyCellValue(String emptyCellValue) {
		this.emptyCellValue = emptyCellValue;
	}

	/**
	 * Returns the columnNames. The default value is an empty map.
	 * 
	 * @return the value of columnNames
	 */
	public Map<String, String> getColumnNames() {
		return columnNames;
	}

	/**
	 * Sets the columnNames.
	 * 
	 * @param columnNames
	 *            the new value for columnNames
	 */
	public void setColumnNames(Map<String, String> columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * Returns the allRows property. The default value is <code>true</code>.
	 * 
	 * @return the value of allRows
	 */
	public boolean isAllRows() {
		return allRows;
	}

	/**
	 * Sets the allRows property.
	 * 
	 * @param allRows
	 *            the new value for allRows
	 */
	public void setAllRows(boolean allRows) {
		this.allRows = allRows;
	}

	/**
	 * Returns the excludeFirstRow property. The default value is <code>false</code>.
	 * 
	 * @return the value of excludeFirstRow
	 */
	public boolean isExcludeFirstRow() {
		return excludeFirstRow;
	}

	/**
	 * Sets the excludeFirstRow property.
	 * 
	 * @param excludeFirstRow
	 *            the new value for excludeFirstRow
	 */
	public void setExcludeFirstRow(boolean excludeFirstRow) {
		this.excludeFirstRow = excludeFirstRow;
	}

	/**
	 * Returns the ignoreBlankRows property. The default value is <code>false</code>.
	 *
	 * @return the value of ignoreBlankRows
	 */
	public boolean isIgnoreBlankRows() {
		return ignoreBlankRows;
	}

	/**
	 * Sets the ignoreBlankRows property.
	 *
	 * @param ignoreBlankRows the new value for ignoreBlankRows
	 */
	public void setIgnoreBlankRows(boolean ignoreBlankRows) {
		this.ignoreBlankRows = ignoreBlankRows;
	}

	/**
	 * Returns the emptyCellPolicy. The default value is
	 * <code>SpreadsheetEmptyCellPolicy.EMPTY_STRING</code>.
	 * 
	 * @return the value of emptyCellPolicy
	 */
	public SpreadsheetEmptyCellPolicy getEmptyCellPolicy() {
		return emptyCellPolicy;
	}

	/**
	 * Sets the emptyCellPolicy.
	 * 
	 * @param emptyCellPolicy
	 *            the new value for emptyCellPolicy
	 */
	public void setEmptyCellPolicy(SpreadsheetEmptyCellPolicy emptyCellPolicy) {
		this.emptyCellPolicy = emptyCellPolicy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allRows ? 1231 : 1237);
		result = prime * result + ((columnNames == null) ? 0 : columnNames.hashCode());
		result = prime * result + ((columnRange == null) ? 0 : columnRange.hashCode());
		result = prime * result + ((emptyCellPolicy == null) ? 0 : emptyCellPolicy.hashCode());
		result = prime * result + ((emptyCellValue == null) ? 0 : emptyCellValue.hashCode());
		result = prime * result + (excludeFirstRow ? 1231 : 1237);
		result = prime * result + (ignoreBlankRows ? 1231 : 1237);
		result = prime * result + ((rowRange == null) ? 0 : rowRange.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpreadsheetImportConfiguration other = (SpreadsheetImportConfiguration) obj;
		if (allRows != other.allRows)
			return false;
		if (columnNames == null) {
			if (other.columnNames != null)
				return false;
		} else if (!columnNames.equals(other.columnNames))
			return false;
		if (columnRange == null) {
			if (other.columnRange != null)
				return false;
		} else if (!columnRange.equals(other.columnRange))
			return false;
		if (emptyCellPolicy == null) {
			if (other.emptyCellPolicy != null)
				return false;
		} else if (!emptyCellPolicy.equals(other.emptyCellPolicy))
			return false;
		if (emptyCellValue == null) {
			if (other.emptyCellValue != null)
				return false;
		} else if (!emptyCellValue.equals(other.emptyCellValue))
			return false;
		if (excludeFirstRow != other.excludeFirstRow)
			return false;
		if (ignoreBlankRows != other.ignoreBlankRows)
			return false;
		if (rowRange == null) {
			if (other.rowRange != null)
				return false;
		} else if (!rowRange.equals(other.rowRange))
			return false;
		return true;
	}

}
