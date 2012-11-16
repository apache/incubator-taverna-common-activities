package net.sf.taverna.t2.activities.interaction;

import java.io.Serializable;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * Example activity configuration bean.
 *
 */
public class InteractionActivityConfigurationBean extends
		ActivityPortsDefinitionBean implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1827843116765609367L;

	private String presentationOrigin;

	private InteractionActivityType interactionActivityType;

	private boolean progressNotification;

	public InteractionActivityConfigurationBean() {
		super();
		this.interactionActivityType = InteractionActivityType.LocallyPresentedHtml;
	}

	public InteractionActivityType getInteractionActivityType() {
		return this.interactionActivityType;
	}

	public void setInteractionActivityType(
			final InteractionActivityType interactionActivityType) {
		this.interactionActivityType = interactionActivityType;
	}

	public String getPresentationOrigin() {
		return this.presentationOrigin;
	}

	public void setPresentationOrigin(final String presentationOrigin) {
		this.presentationOrigin = presentationOrigin;
	}

	/**
	 * @return the progressNotification
	 */
	public boolean isProgressNotification() {
		return this.progressNotification;
	}

	/**
	 * @param progressNotification the progressNotification to set
	 */
	public void setProgressNotification(final boolean progressNotification) {
		this.progressNotification = progressNotification;
	}

}
