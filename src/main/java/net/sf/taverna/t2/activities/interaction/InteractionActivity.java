package net.sf.taverna.t2.activities.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.interaction.velocity.NotifyChecker;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceChecker;
import net.sf.taverna.t2.activities.interaction.velocity.RequireChecker;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTprocess;

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

	public InteractionActivity(final CredentialManager credentialManager,
			final InteractionRecorder interactionRecorder,
			final InteractionUtils interactionUtils,
			final InteractionJetty interactionJetty,
			final InteractionPreference interactionPreference,
			final ResponseFeedListener responseFeedListener) {
		this.credentialManager = credentialManager;
		this.interactionRecorder = interactionRecorder;
		this.interactionUtils = interactionUtils;
		this.interactionJetty = interactionJetty;
		this.interactionPreference = interactionPreference;
		this.responseFeedListener = responseFeedListener;
		this.json = null;
	}

	@Override
	public void configure(final JsonNode json)
			throws ActivityConfigurationException {
		
		this.json = json;

		InteractionVelocity.checkVelocity();

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
				this.responseFeedListener));
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
