/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.activities.interaction.atom.AtomUtils;
import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;

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

	public InteractionActivityRunnable(final InteractionRequestor requestor,
			final Template presentationTemplate) {
		this.requestor = requestor;
		this.presentationTemplate = presentationTemplate;
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

		if (InteractionPreference.getInstance().getUseJetty()) {
			InteractionJetty.checkJetty();
		}
		try {
			InteractionUtils.copyJavaScript("pmrpc.js");
		} catch (final IOException e1) {
			logger.error(e1);
			this.requestor.fail("Unable to copy necessary Javascript");
		}
		synchronized (ABDERA) {
			final Entry interactionNotificationMessage = this
					.createBasicInteractionMessage(id, runId);

			for (final String key : inputData.keySet()) {
				final Object value = inputData.get(key);
				if (value instanceof byte[]) {
					final String replacementUrl = InteractionPreference
							.getPublicationUrlString(id, key);
					final ByteArrayInputStream bais = new ByteArrayInputStream(
							(byte[]) value);
					try {
						InteractionUtils.publishFile(replacementUrl, bais,
								runId, id);
						bais.close();
						inputData.put(key, replacementUrl);
					} catch (final IOException e) {
						logger.error(e);
					}
				}
			}

			final String inputDataString = this.createInputDataJson(inputData);
			final String inputDataUrl = InteractionPreference
					.getInputDataUrlString(id);
			try {
				InteractionUtils.publishFile(inputDataUrl, inputDataString,
						runId, id);
			} catch (final IOException e) {
				logger.error(e);
			}

			String outputDataUrl = null;

			if (!this.requestor.getInteractionType().equals(
					InteractionType.Notification)) {
				outputDataUrl = InteractionPreference
						.getOutputDataUrlString(id);
			}
			final String interactionUrlString = this.generateHtml(inputDataUrl,
					outputDataUrl, inputData, runId, id);

			this.postInteractionMessage(id, interactionNotificationMessage,
					interactionUrlString, runId);
			if (!this.requestor.getInteractionType().equals(
					InteractionType.Notification)) {
				FeedListener.getInstance().registerInteraction(
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
			final String interactionUrlString, final String runId) {

		entry.addLink(StringEscapeUtils.escapeXml(interactionUrlString),
				"presentation");
		entry.setContentAsXhtml("<p><a href=\""
				+ StringEscapeUtils.escapeXml(interactionUrlString)
				+ "\">Open: "
				+ StringEscapeUtils.escapeXml(interactionUrlString)
				+ "</a></p>");

		URL feedUrl;

		try {
			feedUrl = new URL(InteractionPreference.getInstance()
					.getFeedUrlString());
			final String entryContent = ((FOMElement) entry)
					.toFormattedString();
			final HttpURLConnection httpCon = (HttpURLConnection) feedUrl
					.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type",
					"application/atom+xml;type=entry");
			httpCon.setRequestProperty("Content-Length",
					"" + entryContent.length());
			httpCon.setRequestProperty("Slug", id);
			httpCon.setRequestMethod("POST");
			final OutputStream outputStream = httpCon.getOutputStream();
			IOUtils.write(entryContent, outputStream);
			outputStream.close();
			final int response = httpCon.getResponseCode();
			if (response >= 400) {
				logger.error("Received response code" + response);
			}
			if (response == HttpURLConnection.HTTP_CREATED) {
				InteractionRecorder.addResource(runId, id,
						httpCon.getHeaderField("Location"));
			}
		} catch (final MalformedURLException e2) {
			logger.error(e2);
		} catch (final IOException e) {
			logger.error(e);
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

		velocityContext.put("feed", InteractionPreference.getInstance()
				.getFeedUrlString());
		velocityContext.put("runId", runId);
		velocityContext.put("entryId", id);
		final String pmrpcUrl = InteractionPreference.getInstance()
				.getLocationUrl() + "/pmrpc.js";
		velocityContext.put("pmrpcUrl", pmrpcUrl);
		velocityContext.put("inputDataUrl", inputDataUrl);
		velocityContext.put("outputDataUrl", outputDataUrl);
		final String interactionUrl = InteractionPreference
				.getInteractionUrlString(id);

		velocityContext.put("interactionUrl", interactionUrl);

		String presentationUrl = "";
		final String authorizeUrl = "";
		try {
			if (this.requestor.getPresentationType().equals(
					InteractionActivityType.VelocityTemplate)) {

				presentationUrl = InteractionPreference
						.getPresentationUrlString(id);

				final String presentationString = this.processTemplate(
						this.presentationTemplate, velocityContext);
				InteractionUtils.publishFile(presentationUrl,
						presentationString, runId, id);

			} else if (this.requestor.getPresentationType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = this.requestor.getPresentationOrigin();
			}

			velocityContext.put("presentationUrl", presentationUrl);

			final String interactionString = this.processTemplate(
					InteractionVelocity.getInteractionTemplate(),
					velocityContext);
			InteractionUtils.publishFile(interactionUrl, interactionString,
					runId, id);

			if (!authorizeUrl.isEmpty()) {
				return authorizeUrl;
			}
			return interactionUrl;
		} catch (final IOException e) {
			logger.error(e);
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