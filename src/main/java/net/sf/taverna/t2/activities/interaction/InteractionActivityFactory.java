/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author alanrw
 *
 */
public class InteractionActivityFactory implements ActivityFactory {
	
	private CredentialManager credentialManager;
	
	private InteractionRecorder interactionRecorder;
	
	private InteractionUtils interactionUtils;

	private InteractionJetty interactionJetty;

	private InteractionPreference interactionPreference;

	private ResponseFeedListener responseFeedListener;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory#createActivity()
	 */
	@Override
	public InteractionActivity createActivity() {
		return new InteractionActivity(this.credentialManager,
				this.interactionRecorder,
				this.interactionUtils,
				this.interactionJetty,
				this.interactionPreference,
				this.responseFeedListener);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory#getActivityType()
	 */
	@Override
	public URI getActivityType() {
		return URI.create(InteractionActivity.URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory#getActivityConfigurationSchema()
	 */
	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readTree(getClass().getResource("/schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory#getInputPorts(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory#getOutputPorts(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the credentialManager
	 */
	public CredentialManager getCredentialManager() {
		return credentialManager;
	}

	/**
	 * @param credentialManager the credentialManager to set
	 */
	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

	public void setInteractionRecorder(InteractionRecorder interactionRecorder) {
		this.interactionRecorder = interactionRecorder;
	}

	public void setInteractionUtils(InteractionUtils interactionUtils) {
		this.interactionUtils = interactionUtils;
	}

	public void setInteractionJetty(InteractionJetty interactionJetty) {
		this.interactionJetty = interactionJetty;
	}

	public void setInteractionPreference(InteractionPreference interactionPreference) {
		this.interactionPreference = interactionPreference;
	}

	public void setResponseFeedListener(ResponseFeedListener responseFeedListener) {
		this.responseFeedListener = responseFeedListener;
	}

}
