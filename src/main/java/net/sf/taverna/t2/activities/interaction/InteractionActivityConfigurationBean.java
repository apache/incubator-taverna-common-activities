package net.sf.taverna.t2.activities.interaction;

import java.io.Serializable;
import java.util.ArrayList;

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

	private ArrayList<String> publishedList;
	
	private boolean progressNotification;

	public InteractionActivityConfigurationBean() {
		super();
		interactionActivityType = InteractionActivityType.LocallyPresentedHtml;
	}

	public InteractionActivityType getInteractionActivityType() {
		return interactionActivityType;
	}

	public void setInteractionActivityType(
			InteractionActivityType interactionActivityType) {
		this.interactionActivityType = interactionActivityType;
	}

	public String getPresentationOrigin() {
		return presentationOrigin;
	}

	public void setPresentationOrigin(String presentationOrigin) {
		this.presentationOrigin = presentationOrigin;
	}

	private ArrayList<String> getPublishedList() {
		if (publishedList == null) {
			publishedList = new ArrayList<String>();
		}
		return publishedList;
	}

	public boolean isPublished(String portName) {
		return getPublishedList().contains(portName);
	}

	public void setPublished(String portName, boolean isPublished) {
		if (isPublished && !isPublished(portName)) {
			getPublishedList().add(portName);
		}
		if (!isPublished && isPublished(portName)) {
			getPublishedList().remove(portName);
		}
	}

	/**
	 * @return the progressNotification
	 */
	public boolean isProgressNotification() {
		return progressNotification;
	}

	/**
	 * @param progressNotification the progressNotification to set
	 */
	public void setProgressNotification(boolean progressNotification) {
		this.progressNotification = progressNotification;
	}

}
