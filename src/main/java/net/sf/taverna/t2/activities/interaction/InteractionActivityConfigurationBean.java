package net.sf.taverna.t2.activities.interaction;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
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

	public InteractionActivityConfigurationBean(JsonNode json) throws ActivityConfigurationException {
		this.setPresentationOrigin(json);
		this.setInteractionActivityType(json);
		this.setProgressNotification(json);
	}


	public InteractionActivityType getInteractionActivityType() {
		return this.interactionActivityType;
	}

	public void setInteractionActivityType(
			final InteractionActivityType interactionActivityType) {
		this.interactionActivityType = interactionActivityType;
	}
	
	private void setInteractionActivityType(JsonNode json) {
		JsonNode subNode = json.get("interactivityActivityType");
		this.setInteractionActivityType(InteractionActivityType.LocallyPresentedHtml);
		if (subNode == null) {
			return;
		}
		String textValue = subNode.textValue();
		if (textValue == null) {
			return;
		}
		if ("VelocityTemplate".equals(textValue)) {
			this.setInteractionActivityType(InteractionActivityType.VelocityTemplate);
		}
	}



	public String getPresentationOrigin() {
		return this.presentationOrigin;
	}

	public void setPresentationOrigin(final String presentationOrigin) {
		this.presentationOrigin = presentationOrigin;
	}
	
	private void setPresentationOrigin(JsonNode json) throws ActivityConfigurationException {
		JsonNode subNode = json.get("presentationOrigin");
		if (subNode == null) {
			throw new ActivityConfigurationException("presentationOrigin must be specified");
		}
		String textValue = subNode.textValue();
		if (textValue == null) {
			throw new ActivityConfigurationException("presentationOrigin must be specified");			
		}
		this.setPresentationOrigin(textValue);
	}


	/**
	 * @return the progressNotification
	 */
	public boolean isProgressNotification() {
		return this.progressNotification;
	}

	/**
	 * @param progressNotification
	 *            the progressNotification to set
	 */
	public void setProgressNotification(final boolean progressNotification) {
		this.progressNotification = progressNotification;
	}
	
	private void setProgressNotification(JsonNode json) {
		JsonNode subNode = json.get("progressNotification");
		this.setProgressNotification(false);
		if (subNode == null) {
			return;
		}
		this.setProgressNotification(subNode.booleanValue());
	}



}
