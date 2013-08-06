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

	@Override
	public boolean canVisit(final Object o) {
		return o instanceof InteractionActivity;
	}

	@Override
	public boolean isTimeConsuming() {
		return true;
	}

	@Override
	public VisitReport visit(final InteractionActivity activity,
			final List<Object> ancestry) {
		final InteractionActivityConfigurationBean config = activity
				.getConfiguration();

		if (config.getInteractionActivityType().equals(
				InteractionActivityType.LocallyPresentedHtml)) {
			return RemoteHealthChecker.contactEndpoint(activity,
					config.getPresentationOrigin());
		}

		return null;
	}

}
