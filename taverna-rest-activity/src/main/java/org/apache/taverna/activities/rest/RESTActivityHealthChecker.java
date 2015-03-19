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

package org.apache.taverna.activities.rest;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * A {@link HealthChecker} for a {@link RESTActivity}.
 *
 * @author Sergejs Aleksejevs
 */
public class RESTActivityHealthChecker implements HealthChecker<RESTActivity> {
	@Override
	public boolean canVisit(Object subject) {
		return (subject instanceof RESTActivity);
	}

	@Override
	public VisitReport visit(RESTActivity activity, List<Object> ancestors) {
		// collection of validation reports that this health checker will create
		List<VisitReport> reports = new ArrayList<VisitReport>();

		RESTActivityConfigurationBean configBean = activity.getConfigurationBean();
		if (configBean.isValid()) {
			reports.add(new VisitReport(RESTActivityHealthCheck.getInstance(), activity,
					"REST Activity is configured correctly",
					RESTActivityHealthCheck.CORRECTLY_CONFIGURED, Status.OK));
		} else {
			reports.add(new VisitReport(RESTActivityHealthCheck.getInstance(), activity,
					"REST Activity - bad configuration",
					RESTActivityHealthCheck.GENERAL_CONFIG_PROBLEM, Status.SEVERE));
		}

		// (possibly other types of reports could be added later)

		// collection all reports together
		Status worstStatus = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(RESTActivityHealthCheck.getInstance(), activity,
				"REST Activity Report", HealthCheck.NO_PROBLEM, worstStatus, reports);

		return report;
	}

	/**
	 * Health check for the REST activity only involves
	 * verifying details in the configuration bean -
	 * that is quick.
	 */
	@Override
	public boolean isTimeConsuming() {
		return false;
	}

}
