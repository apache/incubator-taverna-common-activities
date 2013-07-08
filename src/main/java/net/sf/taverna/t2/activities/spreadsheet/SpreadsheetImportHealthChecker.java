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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;


/**
 * Health checker for SpreadsheetImport activities.
 *
 * @author David Withers
 */
public class SpreadsheetImportHealthChecker implements HealthChecker<SpreadsheetImportActivity> {

	public boolean canVisit(Object subject) {
		return (subject != null && subject instanceof SpreadsheetImportActivity);
	}

	public VisitReport visit(SpreadsheetImportActivity activity, List<Object> ancestors) {
		Processor p = (Processor) VisitReport.findAncestor(ancestors, Processor.class);
		if (p == null) {
			return null;
		}
		JsonNode configuration = activity.getConfiguration();
		if (configuration == null) {
			return new VisitReport(HealthCheck.getInstance(), p, "Spreadsheet import has not been configured", HealthCheck.NO_CONFIGURATION, Status.SEVERE);
		}
		return new VisitReport(HealthCheck.getInstance(), p, "Spreadsheet OK", HealthCheck.NO_PROBLEM, Status.OK);
	}

	public boolean isTimeConsuming() {
		return false;
	}

}
