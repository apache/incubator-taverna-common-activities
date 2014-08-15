package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.InteractionActivityType.LocallyPresentedHtml;
import static net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker.contactEndpoint;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * Checks if remote-defined HTML interaction pages are actually available.
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
	public VisitReport visit(InteractionActivity activity, List<Object> ancestry) {
		InteractionActivityConfigurationBean config = activity
				.getConfiguration();

		if (config.getInteractionActivityType().equals(LocallyPresentedHtml)) {
			return contactEndpoint(activity, config.getPresentationOrigin());
		}

		return null;
	}
}
