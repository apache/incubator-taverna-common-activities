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
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import net.sf.taverna.t2.activities.interaction.atom.AtomUtils;
import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.rest.RESTActivityCredentialsProvider;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.i18n.text.Sanitizer;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMElement;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.abdera.protocol.error.ProtocolException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public final class InteractionActivityRunnable implements Runnable {
	
	private static Logger logger = Logger.getLogger(InteractionActivityRunnable.class);
	
	private static Abdera ABDERA = Abdera.getInstance();

	private static Set<String> publishedUrls = Collections.synchronizedSet(new HashSet<String> ());

	private final Template presentationTemplate;


	private final InteractionRequestor requestor;

	public InteractionActivityRunnable(InteractionRequestor requestor, Template presentationTemplate) {
		this.requestor = requestor;
		this.presentationTemplate = presentationTemplate;
	}

	public void run() {
/*		InvocationContext context = callback.getContext();
				*/
		String runId = requestor.getRunId();
		String specifiedId = System.getProperty("taverna.runid");
		if (specifiedId != null) {
			runId = specifiedId;
		}

		String id = Sanitizer.sanitize(UUID.randomUUID().toString(),
				"", true, Normalizer.Form.D);
		
		Map<String, Object> inputData = new HashMap<String, Object>();
		inputData = requestor.getInputData();

		if (InteractionPreference.getInstance().getUseJetty()) {
			InteractionJetty.checkJetty();
		}
		try {
			copyJavaScript("pmrpc.js");
		} catch (IOException e1) {
			logger.error(e1);
			requestor.fail("Unable to copy necessary Javascript");
		}
		synchronized (ABDERA) {
			Entry interactionNotificationMessage = createBasicInteractionMessage(id, runId);
			
			for (String key : inputData.keySet()) {
				Object value = inputData.get(key);
				if (value instanceof byte[]) {
					String replacementUrl = InteractionPreference.getInstance()
					.getLocationUrl()
					+ "/interaction" + id + "-" + key;
					ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) value);
					try {
						publishFile(replacementUrl, bais);
						bais.close();
						inputData.put(key, replacementUrl);
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}

			String inputDataString = addInputDataToMessage(interactionNotificationMessage, inputData);

			String interactionUrlString = generateHtml(inputData, inputDataString,
					runId, id);

			postInteractionMessage(id, interactionNotificationMessage, interactionUrlString);
			if (!requestor.getInteractionType().equals(InteractionType.Notification)) {
					FeedListener.getInstance().registerInteraction(interactionNotificationMessage,requestor);
			} else {
				requestor.carryOn();
				
			}
		}
	}

	private String addInputDataToMessage(Entry interactionNotificationMessage,
			Map<String, Object> inputData) {
		Element inputDataElement = interactionNotificationMessage
				.addExtension(AtomUtils.getInputDataQName());
		ObjectMapper mapper = new ObjectMapper();
		StringWriter sw = new StringWriter();
		try {
			mapper.writeValue(sw, inputData);
		} catch (JsonGenerationException e) {
			logger.error(e);
			requestor.fail("Unable to generate JSON");
		} catch (JsonMappingException e) {
			logger.error(e);
			requestor.fail("Unable to generate JSON");
		} catch (IOException e) {
			logger.error(e);
			requestor.fail("Unable to generate JSON");
		}
		String inputDataString = sw.toString();
		inputDataString = StringEscapeUtils
				.escapeJavaScript(inputDataString);
		inputDataElement.setText(inputDataString);
		return inputDataString;
	}

	private Entry createBasicInteractionMessage(String id, String runId) {
		Entry interactionNotificationMessage = ABDERA.newEntry();

		interactionNotificationMessage.setId(id);
		Date timestamp = new Date();
		interactionNotificationMessage.setPublished(timestamp);
		interactionNotificationMessage.setUpdated(timestamp);

		interactionNotificationMessage.addAuthor("Taverna");
		interactionNotificationMessage.setTitle("Interaction from Taverna for "
				+ requestor.generateId());


		Element runIdElement = interactionNotificationMessage.addExtension(AtomUtils.getRunIdQName());
		runIdElement.setText(StringEscapeUtils
				.escapeJavaScript(runId));
		if (requestor.getInteractionType().equals(InteractionType.Notification)) {
			interactionNotificationMessage.addExtension(AtomUtils.getProgressQName());
		}
		Element idElement = interactionNotificationMessage.addExtension(AtomUtils.getIdQName());
		idElement.setText(id);

		return interactionNotificationMessage;
	}
	
	private void postInteractionMessage(String id, Entry entry,
			String interactionUrlString) {

		entry.addLink(StringEscapeUtils.escapeXml(interactionUrlString), "presentation");
		entry.setContentAsXhtml("<p><a href=\"" + StringEscapeUtils.escapeXml(interactionUrlString)
				+ "\">Open: " + StringEscapeUtils.escapeXml(interactionUrlString) + "</a></p>");
		
		URL feedUrl;

			try {
				feedUrl = new URL(InteractionPreference.getInstance().getFeedUrl());
				String entryContent = ((FOMElement) entry).toFormattedString();
				HttpURLConnection httpCon = (HttpURLConnection) feedUrl.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestProperty("Content-Type", "application/atom+xml;type=entry");
		           httpCon.setRequestProperty("Content-Length", "" + entryContent.length());
		           httpCon.setRequestProperty("Slug", id);
				httpCon.setRequestMethod("POST");
				OutputStream outputStream = httpCon.getOutputStream();
				IOUtils.write(entryContent, outputStream);
				outputStream.close();
				int respCode = httpCon.getResponseCode();
			} catch (MalformedURLException e2) {
				logger.error(e2);
			} catch (IOException e) {
				logger.error(e);
			}
	}
	
	String generateHtml(Map<String, Object> inputData,
			String inputDataString, String runId, String id) {

		VelocityContext velocityContext = new VelocityContext();
		for (String inputName : inputData.keySet()) {
			Object input = inputData.get(inputName);
			velocityContext.put(inputName, input);
		}
		velocityContext.put("feed", InteractionPreference.getInstance()
				.getFeedUrl());
		velocityContext.put("runId", runId);
		velocityContext.put("entryId", id);
		String pmrpcUrl = InteractionPreference.getInstance().getLocationUrl()
				+ "/pmrpc.js";
		velocityContext.put("pmrpcUrl", pmrpcUrl);
		velocityContext.put("inputData", inputDataString);
		String interactionUrl = InteractionPreference.getInstance()
		.getLocationUrl()
		+ "/interaction" + id + ".html";

		velocityContext.put("interactionUrl", interactionUrl);
		
		String presentationUrl = "";
		String authorizeUrl = "";
		try {
			if (requestor.getPresentationType().equals(
					InteractionActivityType.VelocityTemplate)) {

				presentationUrl = InteractionPreference.getInstance()
						.getLocationUrl()
						+ "/presentation" + id + ".html";
				
				String presentationString = processTemplate(
						presentationTemplate, velocityContext);
				publishFile(presentationUrl, presentationString);

			} else if (requestor.getPresentationType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = requestor.getPresentationOrigin();
			}

			velocityContext.put("presentationUrl", presentationUrl);

				String interactionString = processTemplate(InteractionVelocity
						.getInteractionTemplate(), velocityContext);
				publishFile(interactionUrl, interactionString);

				if (!authorizeUrl.isEmpty()) {
					return authorizeUrl;
				}
				return interactionUrl;
		} catch (IOException e) {
			logger.error(e);
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

	private void publishFile(String urlString, String contents)
			throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				contents.getBytes());
		publishFile(urlString, byteArrayInputStream);
	}

	private void publishFile(String urlString, InputStream is)
			throws IOException {
		if (publishedUrls.contains(urlString)) {
			return;
		}
		publishedUrls.add(urlString);
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		OutputStream outputStream = httpCon.getOutputStream();
		IOUtils.copy(is, outputStream);
		is.close();
		outputStream.close();
		int respCode = httpCon.getResponseCode();
	}
	
	protected void copyJavaScript(String javascriptFileName) throws IOException {
		String targetUrl = InteractionPreference.getInstance().getLocationUrl()
				+ "/" + javascriptFileName;
		publishFile(targetUrl, InteractionActivity.class.getResourceAsStream("/" + javascriptFileName));
	}

	

}