/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public final class InteractionActivityRunnable implements Runnable {

	private static final Logger logger = Logger.getLogger(InteractionActivityRunnable.class);

	private static final Abdera ABDERA = Abdera.getInstance();

	private static final Set<String> publishedUrls = Collections.synchronizedSet(new HashSet<String> ());

	private final Template presentationTemplate;


	private final InteractionRequestor requestor;

	public InteractionActivityRunnable(final InteractionRequestor requestor, final Template presentationTemplate) {
		this.requestor = requestor;
		this.presentationTemplate = presentationTemplate;
	}

	public void run() {
/*		InvocationContext context = callback.getContext();
				*/
		String runId = this.requestor.getRunId();
		final String specifiedId = System.getProperty("taverna.runid");
		if (specifiedId != null) {
			runId = specifiedId;
		}

		final String id = Sanitizer.sanitize(UUID.randomUUID().toString(),
				"", true, Normalizer.Form.D);

		final Map<String, Object> inputData = this.requestor.getInputData();

		if (InteractionPreference.getInstance().getUseJetty()) {
			InteractionJetty.checkJetty();
		}
		try {
			copyJavaScript("pmrpc.js");
		} catch (final IOException e1) {
			logger.error(e1);
			this.requestor.fail("Unable to copy necessary Javascript");
		}
		synchronized (ABDERA) {
			final Entry interactionNotificationMessage = createBasicInteractionMessage(id, runId);

			for (final String key : inputData.keySet()) {
				final Object value = inputData.get(key);
				if (value instanceof byte[]) {
					final String replacementUrl = InteractionPreference.getInstance()
					.getLocationUrl()
					+ "/interaction" + id + "-" + key;
					final ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) value);
					try {
						publishFile(replacementUrl, bais);
						bais.close();
						inputData.put(key, replacementUrl);
					} catch (final IOException e) {
						logger.error(e);
					}
				}
			}

			final String inputDataString = addInputDataToMessage(interactionNotificationMessage, inputData);

			final String interactionUrlString = generateHtml(inputData, inputDataString,
					runId, id);

			postInteractionMessage(id, interactionNotificationMessage, interactionUrlString);
			if (!this.requestor.getInteractionType().equals(InteractionType.Notification)) {
					FeedListener.getInstance().registerInteraction(interactionNotificationMessage,this.requestor);
			} else {
				this.requestor.carryOn();

			}
		}
	}

	private String addInputDataToMessage(final Entry interactionNotificationMessage,
			final Map<String, Object> inputData) {
		final Element inputDataElement = interactionNotificationMessage
				.addExtension(AtomUtils.getInputDataQName());
		final ObjectMapper mapper = new ObjectMapper();
		final StringWriter sw = new StringWriter();
		try {
			mapper.writeValue(sw, inputData);
		} catch (final JsonGenerationException e) {
			logger.error(e);
			this.requestor.fail("Unable to generate JSON");
		} catch (final JsonMappingException e) {
			logger.error(e);
			this.requestor.fail("Unable to generate JSON");
		} catch (final IOException e) {
			logger.error(e);
			this.requestor.fail("Unable to generate JSON");
		}
		final String inputDataString = StringEscapeUtils
				.escapeJavaScript(sw.toString());
		inputDataElement.setText(inputDataString);
		return inputDataString;
	}

	private Entry createBasicInteractionMessage(final String id, final String runId) {
		final Entry interactionNotificationMessage = ABDERA.newEntry();

		interactionNotificationMessage.setId(id);
		final Date timestamp = new Date();
		interactionNotificationMessage.setPublished(timestamp);
		interactionNotificationMessage.setUpdated(timestamp);

		interactionNotificationMessage.addAuthor("Taverna");
		interactionNotificationMessage.setTitle("Interaction from Taverna for "
				+ this.requestor.generateId());


		final Element runIdElement = interactionNotificationMessage.addExtension(AtomUtils.getRunIdQName());
		runIdElement.setText(StringEscapeUtils
				.escapeJavaScript(runId));
		if (this.requestor.getInteractionType().equals(InteractionType.Notification)) {
			interactionNotificationMessage.addExtension(AtomUtils.getProgressQName());
		}
		final Element idElement = interactionNotificationMessage.addExtension(AtomUtils.getIdQName());
		idElement.setText(id);

		return interactionNotificationMessage;
	}

	private void postInteractionMessage(final String id, final Entry entry,
			final String interactionUrlString) {

		entry.addLink(StringEscapeUtils.escapeXml(interactionUrlString), "presentation");
		entry.setContentAsXhtml("<p><a href=\"" + StringEscapeUtils.escapeXml(interactionUrlString)
				+ "\">Open: " + StringEscapeUtils.escapeXml(interactionUrlString) + "</a></p>");

		URL feedUrl;

			try {
				feedUrl = new URL(InteractionPreference.getInstance().getFeedUrl());
				final String entryContent = ((FOMElement) entry).toFormattedString();
				final HttpURLConnection httpCon = (HttpURLConnection) feedUrl.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestProperty("Content-Type", "application/atom+xml;type=entry");
		           httpCon.setRequestProperty("Content-Length", "" + entryContent.length());
		           httpCon.setRequestProperty("Slug", id);
				httpCon.setRequestMethod("POST");
				final OutputStream outputStream = httpCon.getOutputStream();
				IOUtils.write(entryContent, outputStream);
				outputStream.close();
				httpCon.getResponseCode();
			} catch (final MalformedURLException e2) {
				logger.error(e2);
			} catch (final IOException e) {
				logger.error(e);
			}
	}

	String generateHtml(final Map<String, Object> inputData,
			final String inputDataString, final String runId, final String id) {

		final VelocityContext velocityContext = new VelocityContext();
		for (final String inputName : inputData.keySet()) {
			final Object input = inputData.get(inputName);
			velocityContext.put(inputName, input);
		}
		velocityContext.put("feed", InteractionPreference.getInstance()
				.getFeedUrl());
		velocityContext.put("runId", runId);
		velocityContext.put("entryId", id);
		final String pmrpcUrl = InteractionPreference.getInstance().getLocationUrl()
				+ "/pmrpc.js";
		velocityContext.put("pmrpcUrl", pmrpcUrl);
		velocityContext.put("inputData", inputDataString);
		final String interactionUrl = InteractionPreference.getInstance()
		.getLocationUrl()
		+ "/interaction" + id + ".html";

		velocityContext.put("interactionUrl", interactionUrl);

		String presentationUrl = "";
		final String authorizeUrl = "";
		try {
			if (this.requestor.getPresentationType().equals(
					InteractionActivityType.VelocityTemplate)) {

				presentationUrl = InteractionPreference.getInstance()
						.getLocationUrl()
						+ "/presentation" + id + ".html";

				final String presentationString = processTemplate(
						this.presentationTemplate, velocityContext);
				publishFile(presentationUrl, presentationString);

			} else if (this.requestor.getPresentationType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = this.requestor.getPresentationOrigin();
			}

			velocityContext.put("presentationUrl", presentationUrl);

				final String interactionString = processTemplate(InteractionVelocity
						.getInteractionTemplate(), velocityContext);
				publishFile(interactionUrl, interactionString);

				if (!authorizeUrl.isEmpty()) {
					return authorizeUrl;
				}
				return interactionUrl;
		} catch (final IOException e) {
			logger.error(e);
			return null;
		}
	}

	private String processTemplate(final Template template, final VelocityContext context)
			throws IOException {
		final StringWriter resultWriter = new StringWriter();
		template.merge(context, resultWriter);
		resultWriter.close();
		return resultWriter.toString();
	}

	private void publishFile(final String urlString, final String contents)
			throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				contents.getBytes());
		publishFile(urlString, byteArrayInputStream);
	}

	private void publishFile(final String urlString, final InputStream is)
			throws IOException {
		if (publishedUrls.contains(urlString)) {
			return;
		}
		publishedUrls.add(urlString);
		final URL url = new URL(urlString);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		final OutputStream outputStream = httpCon.getOutputStream();
		IOUtils.copy(is, outputStream);
		is.close();
		outputStream.close();
		httpCon.getResponseCode();
	}

	protected void copyJavaScript(final String javascriptFileName) throws IOException {
		final String targetUrl = InteractionPreference.getInstance().getLocationUrl()
				+ "/" + javascriptFileName;
		publishFile(targetUrl, InteractionActivity.class.getResourceAsStream("/" + javascriptFileName));
	}



}