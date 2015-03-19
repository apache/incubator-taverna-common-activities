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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.taverna.activities.interaction.atom.AtomUtils;
import org.apache.taverna.activities.interaction.jetty.InteractionJetty;
import org.apache.taverna.activities.interaction.preference.InteractionPreference;
import org.apache.taverna.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.i18n.text.Sanitizer;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

public final class InteractionActivityRunnable implements Runnable {

	private static final Logger logger = Logger
			.getLogger(InteractionActivityRunnable.class);

	private static final Abdera ABDERA = Abdera.getInstance();

	private final Template presentationTemplate;

	private final InteractionRequestor requestor;

	private CredentialManager credentialManager;

	private InteractionRecorder interactionRecorder;
	
	private InteractionUtils interactionUtils;

	private InteractionJetty interactionJetty;

	private InteractionPreference interactionPreference;

	private ResponseFeedListener responseFeedListener;

	private InteractionVelocity interactionVelocity;

	public InteractionActivityRunnable(final InteractionRequestor requestor,
			final Template presentationTemplate,
			final CredentialManager credentialManager,
			final InteractionRecorder interactionRecorder,
			final InteractionUtils interactionUtils,
			final InteractionJetty interactionJetty,
			final InteractionPreference interactionPreference,
			final ResponseFeedListener responseFeedListener,
			final InteractionVelocity interactionVelocity) {
		this.requestor = requestor;
		this.presentationTemplate = presentationTemplate;
		this.credentialManager = credentialManager;
		this.interactionRecorder = interactionRecorder;
		this.interactionUtils = interactionUtils;
		this.interactionJetty = interactionJetty;
		this.interactionPreference = interactionPreference;
		this.responseFeedListener = responseFeedListener;
		this.interactionVelocity = interactionVelocity;
	}

	@Override
	public void run() {
		/*
		 * InvocationContext context = callback.getContext();
		 */
		final String runId = InteractionUtils.getUsedRunId(this.requestor
				.getRunId());

		final String id = Sanitizer.sanitize(UUID.randomUUID().toString(), "",
				true, Normalizer.Form.D);

		final Map<String, Object> inputData = this.requestor.getInputData();

		if (interactionPreference.getUseJetty()) {
			interactionJetty.startJettyIfNecessary(credentialManager);
		}
		interactionJetty.startListenersIfNecessary();
		try {
			interactionUtils.copyFixedFile("pmrpc.js");
			interactionUtils.copyFixedFile("interaction.css");
		} catch (final IOException e1) {
			logger.error(e1);
			this.requestor.fail("Unable to copy necessary fixed file");
			return;
		}
		synchronized (ABDERA) {
			final Entry interactionNotificationMessage = this
					.createBasicInteractionMessage(id, runId);

			for (final String key : inputData.keySet()) {
				final Object value = inputData.get(key);
				if (value instanceof byte[]) {
					final String replacementUrl = interactionPreference
							.getPublicationUrlString(id, key);
					final ByteArrayInputStream bais = new ByteArrayInputStream(
							(byte[]) value);
					try {
						interactionUtils.publishFile(replacementUrl, bais,
								runId, id);
						bais.close();
						inputData.put(key, replacementUrl);
					} catch (final IOException e) {
						logger.error(e);
						this.requestor.fail("Unable to publish to " + replacementUrl);
						return;
					}
				}
			}

			final String inputDataString = this.createInputDataJson(inputData);
			if (inputDataString == null) {
				return;
			}
			final String inputDataUrl = interactionPreference
					.getInputDataUrlString(id);
			try {
				interactionUtils.publishFile(inputDataUrl, inputDataString,
						runId, id);
			} catch (final IOException e) {
				logger.error(e);
				this.requestor.fail("Unable to publish to " + inputDataUrl);
				return;
			}

			String outputDataUrl = null;

			if (!this.requestor.getInteractionType().equals(
					InteractionType.Notification)) {
				outputDataUrl = interactionPreference
						.getOutputDataUrlString(id);
			}
			final String interactionUrlString = this.generateHtml(inputDataUrl,
					outputDataUrl, inputData, runId, id);

			try {
				this.postInteractionMessage(id, interactionNotificationMessage,
						interactionUrlString, runId);
			} catch (IOException e) {
				logger.error(e);
				this.requestor.fail("Unable to post message");
				return;
			}
			if (!this.requestor.getInteractionType().equals(
					InteractionType.Notification)) {
				responseFeedListener.registerInteraction(
						interactionNotificationMessage, this.requestor);
			} else {
				this.requestor.carryOn();

			}
		}
	}

	private String createInputDataJson(final Map<String, Object> inputData) {
		try {
			return InteractionUtils.objectToJson(inputData);
		} catch (final IOException e) {
			logger.error(e);
			this.requestor.fail("Unable to generate JSON");
		}
		return null;
	}

	private Entry createBasicInteractionMessage(final String id,
			final String runId) {
		final Entry interactionNotificationMessage = ABDERA.newEntry();

		interactionNotificationMessage.setId(id);
		final Date timestamp = new Date();
		interactionNotificationMessage.setPublished(timestamp);
		interactionNotificationMessage.setUpdated(timestamp);

		interactionNotificationMessage.addAuthor("Taverna");
		interactionNotificationMessage.setTitle("Interaction from Taverna for "
				+ this.requestor.generateId());

		final Element runIdElement = interactionNotificationMessage
				.addExtension(AtomUtils.getRunIdQName());
		runIdElement.setText(StringEscapeUtils.escapeJavaScript(runId));
		
		final Element pathIdElement = interactionNotificationMessage.addExtension(AtomUtils.getPathIdQName());
		pathIdElement.setText(StringEscapeUtils.escapeJavaScript(this.requestor.getPath()));
		
		final Element countElement = interactionNotificationMessage.addExtension(AtomUtils.getCountQName());
		countElement.setText(StringEscapeUtils.escapeJavaScript(this.requestor.getInvocationCount().toString()));
		
		if (this.requestor.getInteractionType().equals(
				InteractionType.Notification)) {
			interactionNotificationMessage.addExtension(AtomUtils
					.getProgressQName());
		}
		final Element idElement = interactionNotificationMessage
				.addExtension(AtomUtils.getIdQName());
		idElement.setText(id);

		return interactionNotificationMessage;
	}

	private void postInteractionMessage(final String id, final Entry entry,
			final String interactionUrlString, final String runId) throws IOException {

		entry.addLink(StringEscapeUtils.escapeXml(interactionUrlString),
				"presentation");
		entry.setContentAsXhtml("<p><a href=\""
				+ StringEscapeUtils.escapeXml(interactionUrlString)
				+ "\">Open: "
				+ StringEscapeUtils.escapeXml(interactionUrlString)
				+ "</a></p>");

		URL feedUrl;

			feedUrl = new URL(interactionPreference
					.getFeedUrlString());
			final String entryContent = ((FOMElement) entry)
					.toFormattedString();
			final HttpURLConnection httpCon = (HttpURLConnection) feedUrl
					.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type",
					"application/atom+xml;type=entry;charset=UTF-8");
			httpCon.setRequestProperty("Content-Length",
					"" + entryContent.length());
			httpCon.setRequestProperty("Slug", id);
			httpCon.setRequestMethod("POST");
			httpCon.setConnectTimeout(5000);
			final OutputStream outputStream = httpCon.getOutputStream();
			IOUtils.write(entryContent, outputStream, "UTF-8");
			outputStream.close();
			final int response = httpCon.getResponseCode();
			if ((response < 0) || (response >= 400)) {
				logger.error("Received response code" + response);
				throw (new IOException ("Received response code " + response));
			}
			if (response == HttpURLConnection.HTTP_CREATED) {
				interactionRecorder.addResource(runId, id,
						httpCon.getHeaderField("Location"));
			}
	}

	String generateHtml(final String inputDataUrl, final String outputDataUrl,
			final Map<String, Object> inputData, final String runId,
			final String id) {

		final VelocityContext velocityContext = new VelocityContext();

		for (final String inputName : inputData.keySet()) {
			final Object input = inputData.get(inputName);
			velocityContext.put(inputName, input);
		}

		velocityContext.put("feed", interactionPreference
				.getFeedUrlString());
		velocityContext.put("runId", runId);
		velocityContext.put("entryId", id);
		final String pmrpcUrl = interactionPreference
				.getLocationUrl() + "/pmrpc.js";
		velocityContext.put("pmrpcUrl", pmrpcUrl);
		velocityContext.put("inputDataUrl", inputDataUrl);
		velocityContext.put("outputDataUrl", outputDataUrl);
		final String interactionUrl = interactionPreference
				.getInteractionUrlString(id);

		velocityContext.put("interactionUrl", interactionUrl);

		String presentationUrl = "";
		final String authorizeUrl = "";
		try {
			if (this.requestor.getPresentationType().equals(
					InteractionActivityType.VelocityTemplate)) {

				presentationUrl = interactionPreference
						.getPresentationUrlString(id);

				final String presentationString = this.processTemplate(
						this.presentationTemplate, velocityContext);
				interactionUtils.publishFile(presentationUrl,
						presentationString, runId, id);

			} else if (this.requestor.getPresentationType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = this.requestor.getPresentationOrigin();
			}

			velocityContext.put("presentationUrl", presentationUrl);

			final String interactionString = this.processTemplate(
					interactionVelocity.getInteractionTemplate(),
					velocityContext);
			interactionUtils.publishFile(interactionUrl, interactionString,
					runId, id);

			if (!authorizeUrl.isEmpty()) {
				return authorizeUrl;
			}
			return interactionUrl;
		} catch (final IOException e) {
			logger.error(e);
			this.requestor.fail("Unable to generate HTML");
			return null;
		}
	}

	private String processTemplate(final Template template,
			final VelocityContext context) throws IOException {
		final StringWriter resultWriter = new StringWriter();
		template.merge(context, resultWriter);
		resultWriter.close();
		return resultWriter.toString();
	}

}