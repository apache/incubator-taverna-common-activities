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

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * Health checker for SpreadsheetImport activities.
 * 
 * @author David Withers
 */
public class SpreadsheetImportHealthChecker implements HealthChecker<SpreadsheetImportActivity> {

	public boolean canHandle(Object subject) {
		return (subject != null && subject instanceof SpreadsheetImportActivity);
	}

	public HealthReport checkHealth(SpreadsheetImportActivity activity) {
		HealthReport healthReport = new HealthReport("Spreadsheet Import Service", "OK", Status.OK);
		SpreadsheetImportConfiguration configuration = activity.getConfiguration();
		if (configuration == null) {
			healthReport.setMessage("Service has not been configured");
			healthReport.setStatus(Status.SEVERE);
		}
		return healthReport;
	}

}
