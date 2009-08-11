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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker}.
 *
 * @author David Withers
 */
public class SpreadsheetImportHealthCheckerTest {

	private SpreadsheetImportHealthChecker healthChecker;
	
	private SpreadsheetImportActivity activity;
	
	@Before
	public void setUp() throws Exception {
		healthChecker = new SpreadsheetImportHealthChecker();
		activity = new SpreadsheetImportActivity();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker#canHandle(java.lang.Object)}.
	 */
	@Test
	public void testCanHandle() {
		assertTrue(healthChecker.canHandle(activity));
		assertFalse(healthChecker.canHandle(null));
		assertFalse(healthChecker.canHandle(""));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportHealthChecker#checkHealth(net.sf.taverna.t2.activities.spreadsheet.SpreadsheetImportActivity)}.
	 * @throws Exception 
	 */
	@Test
	public void testCheckHealth() throws Exception {
		assertEquals(Status.SEVERE, healthChecker.checkHealth(activity).getStatus());
		SpreadsheetImportConfiguration configuration = new SpreadsheetImportConfiguration();
		activity.configure(configuration);
		assertEquals(Status.OK, healthChecker.checkHealth(activity).getStatus());
	}

}
