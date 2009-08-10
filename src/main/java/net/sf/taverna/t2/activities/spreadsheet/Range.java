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

import java.util.ArrayList;
import java.util.List;

/**
 * A range of integer values.
 * 
 * @author David Withers
 */
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
