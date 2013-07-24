package net.sf.taverna.t2.activities.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A {@link HealthChecker} for a {@link XPathActivity}.
 *
 * @author Sergejs Aleksejevs
 */
public class XPathActivityHealthChecker implements HealthChecker<XPathActivity>
{
  public boolean canVisit(Object subject) {
    return (subject instanceof XPathActivity);
  }


  public VisitReport visit(XPathActivity activity, List<Object> ancestors)
  {
    // collection of validation reports that this health checker will create
    List<VisitReport> reports = new ArrayList<VisitReport>();


    JsonNode configBean = activity.getConfiguration();
    if (XPathUtils.isValid(configBean)) {
      reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                  "XPath Activity is configured correctly",
                                  XPathActivityHealthCheck.CORRECTLY_CONFIGURED, Status.OK));
    }
    else {
      int xpathStatus = XPathUtils.validateXPath(configBean.get("xpathExpression").textValue());
      if (xpathStatus == XPathUtils.XPATH_EMPTY) {
        reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                    "XPath Activity - XPath expression is missing",
                                    XPathActivityHealthCheck.EMPTY_XPATH_EXPRESSION, Status.SEVERE));
      }
      else if (xpathStatus == XPathUtils.XPATH_INVALID) {
        reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                    "XPath Activity - XPath expression is invalid",
                                    XPathActivityHealthCheck.INVALID_XPATH_EXPRESSION, Status.SEVERE));
      }
      else {
        reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                    "XPath Activity - bad configuration",
                                    XPathActivityHealthCheck.GENERAL_CONFIG_PROBLEM, Status.SEVERE));
      }
    }


    // warn if there is no example XML document
    if (!configBean.has("exampleXmlDocument") || configBean.get("exampleXmlDocument").textValue().trim().length() == 0) {
      reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                  "XPath activity - no example XML document",
                                  XPathActivityHealthCheck.NO_EXAMPLE_DOCUMENT, Status.WARNING));
    }


    // warn if there are no namespace mappings
    if (hasMissingNamespaceMappings(configBean)) {
      reports.add(new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                  "XPath activity - has missing namespace mappings",
                                  XPathActivityHealthCheck.MISSING_NAMESPACE_MAPPINGS, Status.SEVERE));
    }


    // collect all reports together
    Status worstStatus = VisitReport.getWorstStatus(reports);
    VisitReport report = new VisitReport(XPathActivityHealthCheck.getInstance(), activity,
                                         "XPath Activity Report", HealthCheck.NO_PROBLEM, worstStatus, reports);

    return report;
  }



  /**
   * Health check for the XPath activity only involves
   * verifying details in the configuration bean -
   * that is quick.
   */
  public boolean isTimeConsuming() {
    return false;
  }



  private boolean hasMissingNamespaceMappings(JsonNode json)
  {
    List<String> missingNamespaces = new ArrayList<String>();

    for (String xpathLeg : json.get("xpathExpression").textValue().split("/")) {
      String[] legFragments = xpathLeg.split(":");
      if (legFragments.length == 2) {
        // two fragments - the first is the prefix; check if it's in the mappings table
        String fragment = legFragments[0];
        if (fragment.startsWith("@")) {
        	if (fragment.length() == 1) {
        		continue;
        	}
        	fragment = fragment.substring(1);
        }
		Map<String, String> xpathNamespaceMap = null;
		if (json.has("xpathNamespaceMap")) {
			xpathNamespaceMap = new HashMap<>();
			for (JsonNode namespaceMapping : json.get("xpathNamespaceMap")) {
				xpathNamespaceMap.put(namespaceMapping.get("prefix").textValue(),
						namespaceMapping.get("uri").textValue());
			}
		}
		if (xpathNamespaceMap == null || xpathNamespaceMap.isEmpty() ||
            !xpathNamespaceMap.containsKey(fragment))
        {
          missingNamespaces.add(fragment);
        }
      }
    }

    return (missingNamespaces.size() > 0);
  }

}
