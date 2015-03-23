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

package org.apache.taverna.activities.xpath;

import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.CORRECTLY_CONFIGURED;
import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.EMPTY_XPATH_EXPRESSION;
import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.GENERAL_CONFIG_PROBLEM;
import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.INVALID_XPATH_EXPRESSION;
import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.MISSING_NAMESPACE_MAPPINGS;
import static org.apache.taverna.activities.xpath.XPathActivityHealthCheck.NO_EXAMPLE_DOCUMENT;
import static org.apache.taverna.activities.xpath.XPathUtils.XPATH_EMPTY;
import static org.apache.taverna.activities.xpath.XPathUtils.XPATH_INVALID;
import static org.apache.taverna.activities.xpath.XPathUtils.isValid;
import static org.apache.taverna.activities.xpath.XPathUtils.validateXPath;
import static org.apache.taverna.visit.VisitReport.getWorstStatus;
import static org.apache.taverna.visit.VisitReport.Status.OK;
import static org.apache.taverna.visit.VisitReport.Status.SEVERE;
import static org.apache.taverna.visit.VisitReport.Status.WARNING;
import static org.apache.taverna.workflowmodel.health.HealthCheck.NO_PROBLEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.visit.VisitKind;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workflowmodel.health.HealthChecker;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A {@link HealthChecker} for a {@link XPathActivity}.
 * 
 * @author Sergejs Aleksejevs
 */
public class XPathActivityHealthChecker implements HealthChecker<XPathActivity> {
	@Override
	public boolean canVisit(Object subject) {
		return (subject instanceof XPathActivity);
	}

	@Override
	public VisitReport visit(XPathActivity activity, List<Object> ancestors) {
		VisitKind kind = XPathActivityHealthCheck.getInstance();
		/* collection of validation reports that this health checker will create */
		List<VisitReport> reports = new ArrayList<VisitReport>();

		JsonNode configBean = activity.getConfiguration();
		if (isValid(configBean)) {
			reports.add(new VisitReport(kind, activity,
					"XPath Activity is configured correctly",
					CORRECTLY_CONFIGURED, OK));
		} else {
			int xpathStatus = validateXPath(configBean.get("xpathExpression")
					.textValue());
			if (xpathStatus == XPATH_EMPTY) {
				reports.add(new VisitReport(kind, activity,
						"XPath Activity - XPath expression is missing",
						EMPTY_XPATH_EXPRESSION, SEVERE));
			} else if (xpathStatus == XPATH_INVALID) {
				reports.add(new VisitReport(kind, activity,
						"XPath Activity - XPath expression is invalid",
						INVALID_XPATH_EXPRESSION, SEVERE));
			} else {
				reports.add(new VisitReport(kind, activity,
						"XPath Activity - bad configuration",
						GENERAL_CONFIG_PROBLEM, SEVERE));
			}
		}

		// warn if there is no example XML document
		if (!configBean.has("exampleXmlDocument")
				|| configBean.get("exampleXmlDocument").textValue().trim()
						.length() == 0) {
			reports.add(new VisitReport(kind, activity,
					"XPath activity - no example XML document",
					NO_EXAMPLE_DOCUMENT, WARNING));
		}

		// warn if there are no namespace mappings
		if (hasMissingNamespaceMappings(configBean)) {
			reports.add(new VisitReport(kind, activity,
					"XPath activity - has missing namespace mappings",
					MISSING_NAMESPACE_MAPPINGS, SEVERE));
		}

		// collect all reports together
		Status worstStatus = getWorstStatus(reports);
		VisitReport report = new VisitReport(kind, activity,
				"XPath Activity Report", NO_PROBLEM, worstStatus, reports);

		return report;
	}

	/**
	 * Health check for the XPath activity only involves verifying details in
	 * the configuration bean - that is quick.
	 */
	@Override
	public boolean isTimeConsuming() {
		return false;
	}

	private boolean hasMissingNamespaceMappings(JsonNode json) {
		List<String> missingNamespaces = new ArrayList<String>();

		for (String xpathLeg : json.get("xpathExpression").textValue()
				.split("/")) {
			String[] legFragments = xpathLeg.split(":");
			if (legFragments.length == 2) {
				/*
				 * two fragments - the first is the prefix; check if it's in the
				 * mappings table
				 */
				String fragment = legFragments[0];
				if (fragment.startsWith("@")) {
					if (fragment.length() == 1)
						continue;
					fragment = fragment.substring(1);
				}
				Map<String, String> xpathNamespaceMap = null;
				if (json.has("xpathNamespaceMap")) {
					xpathNamespaceMap = new HashMap<>();
					for (JsonNode namespaceMapping : json
							.get("xpathNamespaceMap"))
						xpathNamespaceMap.put(namespaceMapping.get("prefix")
								.textValue(), namespaceMapping.get("uri")
								.textValue());
				}
				if (xpathNamespaceMap == null || xpathNamespaceMap.isEmpty()
						|| !xpathNamespaceMap.containsKey(fragment))
					missingNamespaces.add(fragment);
			}
		}

		return ! missingNamespaces.isEmpty();
	}

}
