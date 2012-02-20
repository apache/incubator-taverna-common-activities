package net.sf.taverna.t2.activities.interaction;

import java.io.Serializable;
import java.net.URI;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * Example activity configuration bean.
 * 
 */
public class InteractionActivityConfigurationBean extends ActivityPortsDefinitionBean implements Serializable {
	
	private String presentationOrigin;
	
	private InteractionActivityType interactionActivityType;
	
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


}
