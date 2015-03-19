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

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * A range of integer values.
 *
 * @author David Withers
 */
@ConfigurationBean(uri = SpreadsheetImportActivity.URI + "/Range")
public class Range {

	/**
	 * The (inclusive) start and end of this <code>Range</code>.
	 */
	private int start, end;

	/**
	 * <code>Range</code>s that are excluded from this <code>Range</code>.
	 */
	private List<Range> excludes = new ArrayList<Range>();

	/**
	 * Constructs a <code>Range</code>.
	 */
	public Range() {
	}

	/**
	 * Constructs a <code>Range</code> with the specified start and end values.
	 *
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 */
	public Range(int start, int end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Constructs a <code>Range</code> with the specified start and end values and a
	 * <code>Range</code> of excluded values.
	 *
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 * @param exclude
	 *            the range to exclude
	 */
	public Range(int start, int end, Range exclude) {
		this.start = start;
		this.end = end;
		if (exclude != null) {
			excludes.add(exclude);
		}
	}

	/**
	 * Constructs a <code>Range</code> with the specified start and end values and
	 * <code>Range</code>s of excluded values.
	 *
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 * @param excludes
	 *            the ranges to exclude
	 */
	public Range(int start, int end, List<Range> excludes) {
		this.start = start;
		this.end = end;
		if (excludes != null) {
			for (Range range : excludes) {
				this.excludes.add(range);
			}
		}
	}

	/**
	 * Constructs a <code>Range</code> that is a deep copy of the specified range.
	 *
	 * @param range
	 *            the <code>Range</code> to copy
	 */
	public Range(Range range) {
		this.start = range.start;
		this.end = range.end;
		if (range.excludes != null) {
			for (Range excludeRange : range.excludes) {
				excludes.add(new Range(excludeRange));
			}
		}
	}

	/**
	 * Returns <code>true</code> if <code>value</code> is included in this <code>Range</code>.
	 *
	 * @param value
	 * @return
	 */
	public boolean contains(int value) {
		if (value >= start && (value <= end || end < 0)) {
			for (Range exclude : excludes) {
				if (exclude.contains(value)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns an array of the values contained in this <code>Range</code>.
	 *
	 * @return an array of the values contained in this <code>Range</code>
	 */
	public int[] getRangeValues() {
		List<Integer> rangeList = new ArrayList<Integer>();
		for (int i = start; i <= end; i++) {
			boolean excluded = false;
			for (Range range : excludes) {
				if (range.contains(i)) {
					excluded = true;
					break;
				}
			}
			if (!excluded) {
				rangeList.add(i);
			}
		}
		int[] rangeArray = new int[rangeList.size()];
		for (int i = 0; i < rangeArray.length; i++) {
			rangeArray[i] = rangeList.get(i);
		}
		return rangeArray;
	}

	/**
	 * Returns the start of the <code>Range</code>.
	 *
	 * @return the start of the <code>Range</code>
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start of the <code>Range</code>.
	 *
	 * @param start
	 *            the new value for start of the <code>Range</code>
	 */
	@ConfigurationProperty(name = "start", label = "Start", description = "The start of the range")
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Returns the end of the <code>Range</code>.
	 *
	 * @return the end of the <code>Range</code>
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end of the range.
	 *
	 * @param end
	 *            the new value for end of the <code>Range</code>
	 */
	@ConfigurationProperty(name = "end", label = "End", description = "The end of the range")
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Adds a <code>Range</code> to be excluded from this <code>Range</code>.
	 *
	 * @param exclude
	 *            a <code>Range</code> to be excluded
	 */
	public void addExclude(Range exclude) {
		excludes.add(exclude);
	}

	/**
	 * Removes a <code>Range</code> from the exclusions for this range.
	 *
	 * @param exclude
	 *            a <code>Range</code> to be removed from the exclusions
	 */
	public void removeExclude(Range exclude) {
		excludes.remove(exclude);
	}

	/**
	 * Returns the exclusions for this range.
	 *
	 * @return the exclusions for this range
	 */
	public List<Range> getExcludes() {
		return excludes;
	}

	/**
	 * Sets the exclusions for this range.
	 *
	 * @param excludes
	 *            the exclusions for this range
	 */
	@ConfigurationProperty(name = "excludes", label = "Excludes Ranges", description = "The ranges the exclude from this range", required = false)
	public void setExcludes(List<Range> excludes) {
		this.excludes = excludes;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('[');
		result.append(start);
		result.append("..");
		result.append(end);
		result.append(']');
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + ((excludes == null) ? 0 : excludes.hashCode());
		result = prime * result + start;
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
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
		Range other = (Range) obj;
		if (end != other.end)
			return false;
		if (excludes == null) {
			if (other.excludes != null)
				return false;
		} else if (!excludes.equals(other.excludes))
			return false;
		if (start != other.start)
			return false;
		return true;
	}

}
