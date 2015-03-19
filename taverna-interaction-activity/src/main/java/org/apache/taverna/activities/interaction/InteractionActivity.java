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

import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.activities.interaction.jetty.InteractionJetty;
import org.apache.taverna.activities.interaction.preference.InteractionPreference;
import org.apache.taverna.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;

import com.fasterxml.jackson.databind.JsonNode;

public final class InteractionActivity extends
		AbstractAsynchronousActivity<JsonNode>
		implements AsynchronousActivity<JsonNode> {
	
	public static final String URI = "http://ns.taverna.org.uk/2010/activity/interaction";

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(InteractionActivity.class);

	private Template presentationTemplate;

	private final Map<String, Integer> inputDepths = new HashMap<String, Integer>();
	private final Map<String, Integer> outputDepths = new HashMap<String, Integer>();

	private CredentialManager credentialManager;

	private InteractionRecorder interactionRecorder;

	private InteractionUtils interactionUtils;

	private InteractionJetty interactionJetty;

	private InteractionPreference interactionPreference;

	private ResponseFeedListener responseFeedListener;

	private JsonNode json;

	private InteractionVelocity interactionVelocity;

	public InteractionActivity(final CredentialManager credentialManager,
			final InteractionRecorder interactionRecorder,
			final InteractionUtils interactionUtils,
			final InteractionJetty interactionJetty,
			final InteractionPreference interactionPreference,
			final ResponseFeedListener responseFeedListener,
			final InteractionVelocity interactionVelocity) {
		this.credentialManager = credentialManager;
		this.interactionRecorder = interactionRecorder;
		this.interactionUtils = interactionUtils;
		this.interactionJetty = interactionJetty;
		this.interactionPreference = interactionPreference;
		this.responseFeedListener = responseFeedListener;
		this.interactionVelocity = interactionVelocity;
		this.json = null;
	}

	@Override
	public void configure(final JsonNode json)
			throws ActivityConfigurationException {
		
		this.json = json;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		final InteractionRequestor requestor = new InteractionCallbackRequestor(
				this, callback, inputs);
		callback.requestRun(new InteractionActivityRunnable(requestor,
				this.presentationTemplate,
				this.credentialManager,
				this.interactionRecorder,
				this.interactionUtils,
				this.interactionJetty,
				this.interactionPreference,
				this.responseFeedListener,
				this.interactionVelocity));
	}

	@Override
	public JsonNode getConfiguration() {
		return this.json;
	}

	public ActivityInputPort getInputPort(final String name) {
		for (final ActivityInputPort port : this.getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	InteractionActivityType getInteractionActivityType() {
		JsonNode subNode = json.get("interactivityActivityType");
		if (subNode == null) {
			return InteractionActivityType.LocallyPresentedHtml;
		}
		String textValue = subNode.textValue();
		if (textValue == null) {
			return InteractionActivityType.LocallyPresentedHtml;
		}
		if ("VelocityTemplate".equals(textValue)) {
			return InteractionActivityType.VelocityTemplate;
		}
		return InteractionActivityType.LocallyPresentedHtml;
	}
	

	 String getPresentationOrigin() {
		JsonNode subNode = json.get("presentationOrigin");
		if (subNode == null) {
			return null;
		}
		String textValue = subNode.textValue();
		if (textValue == null) {
			return null;			
		}
		return textValue;
	}

	public boolean isProgressNotification() {
		JsonNode subNode = json.get("progressNotification");
		if (subNode == null) {
			return false;
		}
		return subNode.booleanValue();
	}

}
