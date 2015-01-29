package net.sf.taverna.t2.activities.xpath;

import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.CORRECTLY_CONFIGURED;
import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.EMPTY_XPATH_EXPRESSION;
import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.GENERAL_CONFIG_PROBLEM;
import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.INVALID_XPATH_EXPRESSION;
import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.MISSING_NAMESPACE_MAPPINGS;
import static net.sf.taverna.t2.activities.xpath.XPathActivityHealthCheck.NO_EXAMPLE_DOCUMENT;
import static net.sf.taverna.t2.activities.xpath.XPathUtils.XPATH_EMPTY;
import static net.sf.taverna.t2.activities.xpath.XPathUtils.XPATH_INVALID;
import static net.sf.taverna.t2.activities.xpath.XPathUtils.isValid;
import static net.sf.taverna.t2.activities.xpath.XPathUtils.validateXPath;
import static net.sf.taverna.t2.visit.VisitReport.getWorstStatus;
import static net.sf.taverna.t2.visit.VisitReport.Status.OK;
import static net.sf.taverna.t2.visit.VisitReport.Status.SEVERE;
import static net.sf.taverna.t2.visit.VisitReport.Status.WARNING;
import static net.sf.taverna.t2.workflowmodel.health.HealthCheck.NO_PROBLEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

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
