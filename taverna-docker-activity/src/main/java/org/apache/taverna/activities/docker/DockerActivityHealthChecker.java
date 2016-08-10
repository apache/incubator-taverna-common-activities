package org.apache.taverna.activities.docker;

import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.workflowmodel.health.HealthCheck;
import org.apache.taverna.workflowmodel.health.HealthChecker;

import java.util.ArrayList;
import java.util.List;

public class DockerActivityHealthChecker implements HealthChecker<Object> {

    @Override
    public boolean canVisit(Object o) {
        return o instanceof DockerActivity;
    }

    @Override
    public VisitReport visit(Object o, List<Object> list) {
        DockerActivity activity = (DockerActivity) o;
        DockerContainerConfiguration contCfg = activity.getContainerConfiguration();
        String containerName = contCfg.getName();
        boolean createValid = ValidationUtil.validateCreateContainer(contCfg, containerName);

        List<VisitReport> reports = new ArrayList<VisitReport>();

        if (createValid) {
            reports.add(new VisitReport(HealthCheck.getInstance(), activity,
                    "Docker create container operation is healthy",
                    HealthCheck.NO_PROBLEM, VisitReport.Status.OK));
        } else {
            reports.add(new VisitReport(HealthCheck.getInstance(), activity,
                    "REST Activity - bad configuration",
                    HealthCheck.INVALID_CONFIGURATION, VisitReport.Status.SEVERE));
        }

        // collection all reports together
        VisitReport.Status worstStatus = VisitReport.getWorstStatus(reports);
        VisitReport report = new VisitReport(HealthCheck.getInstance(), activity,
                "REST Activity Report", HealthCheck.NO_PROBLEM, worstStatus, reports);

        return report;
    }

    @Override
    public boolean isTimeConsuming() {
        return false;
    }
}
