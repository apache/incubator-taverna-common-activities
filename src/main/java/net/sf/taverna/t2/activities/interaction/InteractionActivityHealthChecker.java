package net.sf.taverna.t2.activities.interaction;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

/**
 * Example health checker
 * 
 */
public class InteractionActivityHealthChecker implements
		HealthChecker<InteractionActivity> {

	public boolean canVisit(Object o) {
		return o instanceof InteractionActivity;
	}

	public boolean isTimeConsuming() {
		return true;
	}

	public VisitReport visit(InteractionActivity activity, List<Object> ancestry) {
		InteractionActivityConfigurationBean config = activity.getConfiguration();
		
		if (config.getInteractionActivityType().equals(InteractionActivityType.LocallyPresentedHtml)) {
			return RemoteHealthChecker.contactEndpoint(activity, config.getPresentationOrigin());
		}

		return null;
	}

}
