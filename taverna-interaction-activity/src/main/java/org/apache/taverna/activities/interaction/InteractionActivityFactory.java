/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.interaction;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.apache.taverna.activities.interaction.jetty.InteractionJetty;
import org.apache.taverna.activities.interaction.preference.InteractionPreference;
import org.apache.taverna.activities.interaction.velocity.InteractionVelocity;
import org.apache.taverna.security.credentialmanager.CredentialManager;
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

	private InteractionVelocity interactionVelocity;

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
				this.responseFeedListener,
				this.interactionVelocity);
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

	public void setInteractionVelocity(InteractionVelocity interactionVelocity) {
		this.interactionVelocity = interactionVelocity;
	}

}
