/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import static java.util.UUID.randomUUID;
import static net.sf.taverna.t2.activities.interaction.InteractionRecorder.addResource;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.copyFixedFile;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.getUsedRunId;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.objectToJson;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.publishFile;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getCountQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getIdQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getPathIdQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getProgressQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getRunIdQName;
import static net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty.startJettyIfNecessary;
import static net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty.startListenersIfNecessary;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getInputDataUrlString;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getInteractionUrlString;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getOutputDataUrlString;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getPresentationUrlString;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getPublicationUrlString;
import static net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity.getInteractionTemplate;
import static org.apache.abdera.i18n.text.Sanitizer.sanitize;
import static org.apache.commons.lang.StringEscapeUtils.escapeJavaScript;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMElement;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

public final class InteractionActivityRunnable implements Runnable {
	private static final Logger logger = Logger
			.getLogger(InteractionActivityRunnable.class);

	private static final Abdera ABDERA = Abdera.getInstance();

	private final Template presentationTemplate;
	private final InteractionRequestor requestor;
	private final InteractionPreference prefs;

	public InteractionActivityRunnable(InteractionRequestor requestor,
			Template presentationTemplate) {
		this.requestor = requestor;
		this.presentationTemplate = presentationTemplate;
		this.prefs = InteractionPreference.getInstance();
	}

	@Override
	public void run() {
		/*
		 * InvocationContext context = callback.getContext();
		 */
		String runId = getUsedRunId(requestor.getRunId());
		String id = sanitize(randomUUID().toString(), "", true,
				Normalizer.Form.D);
		Map<String, Object> inputData = requestor.getInputData();

		if (prefs.getUseJetty()) {
			startJettyIfNecessary();
		}
		startListenersIfNecessary();
		try {
			copyFixedFile("pmrpc.js");
			copyFixedFile("interaction.css");
		} catch (IOException e1) {
			logger.error(e1);
			requestor.fail("Unable to copy necessary fixed file");
			return;
		}
		synchronized (ABDERA) {
			Entry interactionNotificationMessage = createBasicInteractionMessage(
					id, runId);

			for (String key : inputData.keySet()) {
				Object value = inputData.get(key);
				if (!(value instanceof byte[])) {
					continue;
				}
				String realReplacementUrl = getPublicationUrlString(true, id,
						key);
				ByteArrayInputStream bais = new ByteArrayInputStream(
						(byte[]) value);
				try {
					publishFile(realReplacementUrl, bais, runId, id);
					bais.close();
					inputData.put(key, getPublicationUrlString(false, id, key));
				} catch (final IOException e) {
					logger.error(e);
					requestor
							.fail("Unable to publish to " + realReplacementUrl);
					return;
				}
			}

			final String inputDataString = createInputDataJson(inputData);
			if (inputDataString == null) {
				return;
			}
			final String inputDataUrl = getInputDataUrlString(false, id);
			try {
				publishFile(getInputDataUrlString(true, id), inputDataString,
						runId, id);
			} catch (final IOException e) {
				logger.error(e);
				requestor.fail("Unable to publish to " + inputDataUrl);
				return;
			}

			String outputDataUrl = null;

			if (requestor.getInteractionType() != InteractionType.Notification) {
				outputDataUrl = getOutputDataUrlString(false, id);
			}
			String interactionUrlString = generateHtml(inputDataUrl,
					outputDataUrl, inputData, runId, id);

			try {
				postInteractionMessage(id, interactionNotificationMessage,
						interactionUrlString, runId);
			} catch (IOException e) {
				logger.error(e);
				requestor.fail("Unable to post message");
				return;
			}
			if (requestor.getInteractionType() != InteractionType.Notification) {
				ResponseFeedListener.getInstance().registerInteraction(
						interactionNotificationMessage, requestor);
			} else {
				requestor.carryOn();
			}
		}
	}

	private String createInputDataJson(Map<String, Object> inputData) {
		try {
			return objectToJson(inputData);
		} catch (final IOException e) {
			logger.error(e);
			requestor.fail("Unable to generate JSON");
			return null;
		}
	}

	private Entry createBasicInteractionMessage(String id, String runId) {
		final Entry interactionNotificationMessage = ABDERA.newEntry();

		interactionNotificationMessage.setId(id);
		final Date timestamp = new Date();
		interactionNotificationMessage.setPublished(timestamp);
		interactionNotificationMessage.setUpdated(timestamp);

		interactionNotificationMessage.addAuthor("Taverna");
		interactionNotificationMessage.setTitle("Interaction from Taverna for "
				+ requestor.generateId());

		interactionNotificationMessage.addExtension(getRunIdQName()).setText(
				escapeJavaScript(runId));

		interactionNotificationMessage.addExtension(getPathIdQName()).setText(
				escapeJavaScript(requestor.getPath()));

		interactionNotificationMessage.addExtension(getCountQName()).setText(
				escapeJavaScript(requestor.getInvocationCount().toString()));

		if (requestor.getInteractionType() == InteractionType.Notification) {
			interactionNotificationMessage.addExtension(getProgressQName());
		}
		interactionNotificationMessage.addExtension(getIdQName()).setText(id);

		return interactionNotificationMessage;
	}

	private void postInteractionMessage(String id, Entry entry,
			String interactionUrlString, String runId) throws IOException {
		entry.addLink(escapeXml(interactionUrlString), "presentation");
		entry.setContentAsXhtml("<p><a href=\""
				+ escapeXml(interactionUrlString) + "\">Open: "
				+ escapeXml(interactionUrlString) + "</a></p>");

		URL feedUrl = new URL(prefs.getFeedUrlString(true));
		final String entryContent = ((FOMElement) entry).toFormattedString();

		final HttpURLConnection httpCon = (HttpURLConnection) feedUrl
				.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestProperty("Content-Type",
				"application/atom+xml;type=entry;charset=UTF-8");
		httpCon.setRequestProperty("Content-Length", "" + entryContent.length());
		httpCon.setRequestProperty("Slug", id);
		httpCon.setRequestMethod("POST");
		httpCon.setConnectTimeout(5000);

		final OutputStream outputStream = httpCon.getOutputStream();
		IOUtils.write(entryContent, outputStream, "UTF-8");
		outputStream.close();

		int response = httpCon.getResponseCode();
		if ((response < 0) || (response >= 400)) {
			logger.error("Received response code" + response);
			throw new IOException("Received response code " + response);
		}
		if (response == HttpURLConnection.HTTP_CREATED) {
			addResource(runId, id, httpCon.getHeaderField("Location"));
		}
	}

	private String generateHtml(String inputDataUrl, String outputDataUrl,
			Map<String, Object> inputData, String runId, String id) {
		VelocityContext velocityContext = new VelocityContext();

		for (String inputName : inputData.keySet()) {
			velocityContext.put(inputName, inputData.get(inputName));
		}

		velocityContext.put("feed", prefs.getFeedUrlString(false));
		velocityContext.put("runId", runId);
		velocityContext.put("entryId", id);
		velocityContext.put("pmrpcUrl", prefs.getLocationUrl(false)
				+ "/pmrpc.js");
		velocityContext.put("inputDataUrl", inputDataUrl);
		velocityContext.put("outputDataUrl", outputDataUrl);
		String interactionUrl = getInteractionUrlString(false, id);
		String realInteractionUrl = getInteractionUrlString(true, id);

		velocityContext.put("interactionUrl", interactionUrl);

		String presentationUrl = "";
		final String authorizeUrl = "";
		try {
			switch (requestor.getPresentationType()) {
			case VelocityTemplate:
				presentationUrl = getPresentationUrlString(false, id);
				publishFile(getPresentationUrlString(true, id),
						processTemplate(presentationTemplate, velocityContext),
						runId, id);
				break;
			case LocallyPresentedHtml:
				presentationUrl = requestor.getPresentationOrigin();
				break;
			}

			velocityContext.put("presentationUrl", presentationUrl);

			publishFile(realInteractionUrl,
					processTemplate(getInteractionTemplate(), velocityContext),
					runId, id);

			if (!authorizeUrl.isEmpty()) {
				return authorizeUrl;
			}
			return interactionUrl;
		} catch (final IOException e) {
			logger.error(e);
			requestor.fail("Unable to generate HTML");
			return null;
		}
	}

	private String processTemplate(Template template, VelocityContext context)
			throws IOException {
		StringWriter resultWriter = new StringWriter();
		template.merge(context, resultWriter);
		resultWriter.close();
		return resultWriter.toString();
	}
}