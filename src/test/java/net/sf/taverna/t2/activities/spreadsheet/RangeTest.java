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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.Range}.
 * 
 * @author David Withers
 */
public class RangeTest {

	private Range range;

	@Before
	public void setUp() throws Exception {
		range = new Range(1, 5);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#Range(int, int)}.
	 */
	@Test
	public void testRangeIntInt() {
		Range range = new Range(3, 9);
		assertEquals(3, range.getStart());
		assertEquals(9, range.getEnd());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.spreadsheet.Range#Range(int, int, net.sf.taverna.t2.activities.spreadsheet.Range)}
	 * .
	 */
	@Test
	public void testRangeIntIntRange() {
		Range range = new Range(0, 12, new Range(3, 9));
		assertEquals(0, range.getStart());
		assertEquals(12, range.getEnd());
		assertTrue(range.contains(0));
		assertTrue(range.contains(2));
		assertFalse(range.contains(5));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.spreadsheet.Range#Range(int, int, java.util.List)}.
	 */
	@Test
	public void testRangeIntIntListOfRange() {
		Range range = new Range(-2, 12, Arrays.asList(new Range(3, 5), new Range(10, 11)));
		assertEquals(-2, range.getStart());
		assertEquals(12, range.getEnd());
		assertTrue(range.contains(-2));
		assertTrue(range.contains(-0));
		assertTrue(range.contains(6));
		assertTrue(range.contains(12));
		assertFalse(range.contains(4));
		assertFalse(range.contains(11));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.spreadsheet.Range#Range(net.sf.taverna.t2.activities.spreadsheet.Range)}
	 * .
	 */
	@Test
	public void testRangeRange() {
		Range rangeCopy = new Range(range);
		assertEquals(range.getStart(), rangeCopy.getStart());
		assertEquals(range.getEnd(), rangeCopy.getEnd());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#contains(int)}.
	 */
	@Test
	public void testContains() {
		assertTrue(range.contains(2));
		assertFalse(range.contains(7));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#getRangeValues()}.
	 */
	@Test
	public void testGetRangeValues() {
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, range.getRangeValues());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#getStart()}.
	 */
	@Test
	public void testGetStart() {
		assertEquals(1, range.getStart());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#setStart(int)}.
	 */
	@Test
	public void testSetStart() {
		range.setStart(2);
		assertEquals(2, range.getStart());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#getEnd()}.
	 */
	@Test
	public void testGetEnd() {
		assertEquals(5, range.getEnd());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#setEnd(int)}.
	 */
	@Test
	public void testSetEnd() {
		range.setEnd(7);
		assertEquals(7, range.getEnd());
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.spreadsheet.Range#addExclude(net.sf.taverna.t2.activities.spreadsheet.Range)}
	 * .
	 */
	@Test
	public void testAddExclude() {
		range.addExclude(new Range(4, 4));
		assertTrue(range.contains(2));
		assertTrue(range.contains(5));
		assertFalse(range.contains(4));
	}

	/**
	 * Test method for
	 * {@link net.sf.taverna.t2.activities.spreadsheet.Range#removeExclude(net.sf.taverna.t2.activities.spreadsheet.Range)}
	 * .
	 */
	@Test
	public void testRemoveExclude() {
		range.addExclude(new Range(4, 4));
		range.removeExclude(new Range(4, 4));
		assertTrue(range.contains(4));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.Range#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals("[1..5]", range.toString());
	}

}
