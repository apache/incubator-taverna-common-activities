package net.sf.taverna.t2.activities.interaction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceChecker;
import net.sf.taverna.t2.activities.interaction.velocity.RequireChecker;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.i18n.text.Sanitizer;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class InteractionActivity extends
		AbstractAsynchronousActivity<InteractionActivityConfigurationBean>
		implements AsynchronousActivity<InteractionActivityConfigurationBean> {

	private static Logger logger = Logger.getLogger(InteractionActivity.class);

	private InteractionActivityConfigurationBean configBean;

	private static Abdera ABDERA = Abdera.getInstance();

	private Template presentationTemplate;

	private Map<String, Integer> inputDepths = new HashMap<String, Integer>();
	private Map<String, Integer> outputDepths = new HashMap<String, Integer>();

	private static QName inputDataQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "input-data",
			"interaction");
	private static QName resultDataQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "result-data",
			"interaction");
	private static QName resultStatusQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "result-status",
			"interaction");
	private static QName idQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "id", "interaction");
	private static QName runIdQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "run-id", "interaction");
	private static QName inReplyToQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "in-reply-to",
			"interaction");
	private static QName progressQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "progress",
			"interaction");
	
	private static Set<String> publishedUrls = Collections.synchronizedSet(new HashSet<String> ());
	
	public InteractionActivity() {
		configBean = new InteractionActivityConfigurationBean();
	}

	@Override
	public void configure(InteractionActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		inputDepths.clear();
		outputDepths.clear();

		InteractionVelocity.checkVelocity();

		if (this.configBean.getInteractionActivityType().equals(
				InteractionActivityType.VelocityTemplate)) {
			presentationTemplate = Velocity.getTemplate(configBean
					.getPresentationOrigin());
			RequireChecker requireChecker = new RequireChecker();
			requireChecker.visit((ASTprocess) presentationTemplate.getData(),
					inputDepths);

			ProduceChecker produceChecker = new ProduceChecker();
			produceChecker.visit((ASTprocess) presentationTemplate.getData(),
					outputDepths);
			configurePortsFromTemplate();
		} else {
			configurePorts(this.configBean);
		}

	}

	protected void configurePortsFromTemplate() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		for (String inputName : inputDepths.keySet()) {
			addInput(inputName, inputDepths.get(inputName), true, null,
					String.class);
		}

		for (String outputName : outputDepths.keySet()) {
			addOutput(outputName, outputDepths.get(outputName));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {

			public void run() {
				InvocationContext context = callback.getContext();
				String runId = callback.getContext().getEntities(
						WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
				String specifiedId = System.getProperty("taverna.runid");
				if (specifiedId != null) {
					runId = specifiedId;
				}
				ReferenceService referenceService = context
						.getReferenceService();

				String id = Sanitizer.sanitize(UUID.randomUUID().toString(),
						"", true, Normalizer.Form.D);

				Map<String, Object> inputData = new HashMap<String, Object>();
				for (String inputName : inputs.keySet()) {
					Object input = referenceService
							.renderIdentifier(inputs.get(inputName),
									getInputPort(inputName)
											.getTranslatedElementClass(),
									callback.getContext());
					inputData.put(inputName, input);
				}

				if (InteractionPreference.getInstance().getUseJetty()) {
					InteractionJetty.checkJetty();
				}
				try {
					copyJavaScript("pmrpc.js");
				} catch (IOException e1) {
					logger.error(e1);
					callback.fail("Unable to copy necessary Javascript");
				}
				synchronized (ABDERA) {
					Entry entry = ABDERA.newEntry();

					entry.setId(id);
					Date timestamp = new Date();
					entry.setPublished(timestamp);
					entry.setUpdated(timestamp);

					entry.addAuthor("Taverna");
					entry.setTitle("Interaction from Taverna for "
							+ generateId(callback));

					ObjectMapper mapper = new ObjectMapper();
					StringWriter sw = new StringWriter();
					try {
						mapper.writeValue(sw, inputData);
					} catch (JsonGenerationException e) {
						logger.error(e);
						callback.fail("Unable to generate JSON");
					} catch (JsonMappingException e) {
						logger.error(e);
						callback.fail("Unable to generate JSON");
					} catch (IOException e) {
						logger.error(e);
						callback.fail("Unable to generate JSON");
					}

					Element runIdElement = entry.addExtension(getRunIdQName());
					runIdElement.setText(StringEscapeUtils
							.escapeJavaScript(runId));
					if (configBean.isProgressNotification()) {
						entry.addExtension(getProgressQName());
					}
					Element inputDataElement = entry
							.addExtension(getInputDataQName());
					String inputDataString = sw.toString();
					inputDataString = StringEscapeUtils
							.escapeJavaScript(inputDataString);
					inputDataElement.setText(inputDataString);

					Element idElement = entry.addExtension(getIdQName());
					idElement.setText(id);

					AbderaClient client = new AbderaClient(ABDERA);
					RequestOptions rOptions = client.getDefaultRequestOptions();
					rOptions.setSlug(id);
					String slug = rOptions.getHeader("Slug");

					String webFile = generateHtml(inputData, inputDataString,
							runId, id);

					entry.addLink(webFile, "presentation");
					entry.setContentAsXhtml("<p><a href=\"" + webFile
							+ "\">Open: " + webFile + "</a></p>");

					ClientResponse resp = client.post(InteractionPreference
							.getInstance().getFeedUrl(), entry, rOptions);
					client.teardown();
					if (!configBean.isProgressNotification()) {
						FeedListener.getInstance().registerInteraction(entry,
							callback, getOutputPorts());
					} else {
						callback.receiveResult(new HashMap<String, T2Reference>(), new int[0]);
					}
				}
			}
		});
	}

	protected void copyJavaScript(String javascriptFileName) throws IOException {
		String targetUrl = InteractionPreference.getInstance().getLocationUrl()
				+ "/" + javascriptFileName;
		publishFile(targetUrl, InteractionActivity.class.getResourceAsStream("/" + javascriptFileName));
	}

	private String generateHtml(Map<String, Object> inputData,
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

		String presentationUrl = "";
		try {
			if (configBean.getInteractionActivityType().equals(
					InteractionActivityType.VelocityTemplate)) {

				presentationUrl = InteractionPreference.getInstance()
						.getLocationUrl()
						+ "/presentation" + id + ".html";
				String presentationString = processTemplate(
						presentationTemplate, velocityContext);
				publishFile(presentationUrl, presentationString);

			} else if (configBean.getInteractionActivityType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = configBean.getPresentationOrigin();
			}

			velocityContext.put("presentationUrl", presentationUrl);

				String interactionUrl = InteractionPreference.getInstance()
						.getLocationUrl()
						+ "/interaction" + id + ".html";
				String interactionString = processTemplate(InteractionVelocity
						.getInteractionTemplate(), velocityContext);
				publishFile(interactionUrl, interactionString);

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

	protected static String generateId(AsynchronousActivityCallback callback) {
		String workflowRunId = callback.getContext().getEntities(
				WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
		return (workflowRunId + ":" + callback.getParentProcessIdentifier());
	}

	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	public static QName getInputDataQName() {
		return inputDataQName;
	}

	public static QName getIdQName() {
		return idQName;
	}

	public static QName getInReplyToQName() {
		return inReplyToQName;
	}

	public static QName getResultDataQName() {
		return resultDataQName;
	}

	public static QName getResultStatusQName() {
		return resultStatusQName;
	}

	/**
	 * @return the runIdQName
	 */
	public static QName getRunIdQName() {
		return runIdQName;
	}

	/**
	 * @return the progressQName
	 */
	public static QName getProgressQName() {
		return progressQName;
	}

}
