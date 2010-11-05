package net.sf.taverna.t2.activities.rest;

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
public class RESTActivityHealthChecker implements HealthChecker<RESTActivity>
{
  public boolean canVisit(Object subject) {
    return (subject instanceof RESTActivity);
  }
  
  
  public VisitReport visit(RESTActivity activity, List<Object> ancestors)
  {
    // collection of validation reports that this health checker will create
    List<VisitReport> reports = new ArrayList<VisitReport>();
    
    
    RESTActivityConfigurationBean configBean = activity.getConfiguration();
    if (configBean.isValid()) {
      reports.add(new VisitReport(RESTActivityHealthCheck.getInstance(), activity, 
                                  "REST Activity is configured correctly", 
                                  RESTActivityHealthCheck.CORRECTLY_CONFIGURED, Status.OK));
    }
    else {
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
  public boolean isTimeConsuming() {
    return false;
  }

}
