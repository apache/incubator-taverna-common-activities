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
