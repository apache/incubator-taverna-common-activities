package net.sf.taverna.t2.activities.interaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.i18n.text.Sanitizer;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceChecker;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceDirective;
import net.sf.taverna.t2.activities.interaction.velocity.RequireChecker;
import net.sf.taverna.t2.activities.interaction.velocity.RequireDirective;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class InteractionActivity extends
		AbstractAsynchronousActivity<InteractionActivityConfigurationBean>
		implements AsynchronousActivity<InteractionActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(InteractionActivity.class);

	
	private InteractionActivityConfigurationBean configBean;
	
	private static Abdera ABDERA = Abdera.getInstance();
	
	private Template template;
	
	private Map<String, Integer> inputDepths = new HashMap<String, Integer> ();
	private Map<String, Integer> outputDepths = new HashMap<String, Integer> ();
	
	private static QName inputDataQName = new QName("http://ns.taverna.org.uk/2012/interaction", "inputData", "interaction");
	private static QName resultDataQName = new QName("http://ns.taverna.org.uk/2012/interaction", "resultData", "interaction");
	private static QName resultStatusQName = new QName("http://ns.taverna.org.uk/2012/interaction", "resultStatus", "interaction");
	
	
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
			template = Velocity.getTemplate(configBean.getPresentationOrigin());
			RequireChecker requireChecker = new RequireChecker();
			requireChecker.visit((ASTprocess) template.getData(), inputDepths);

			ProduceChecker produceChecker = new ProduceChecker();
			produceChecker.visit((ASTprocess) template.getData(), outputDepths);
			configurePortsFromTemplate();
		}
		else {
			configurePorts(this.configBean);
		}

	}

	protected void configurePortsFromTemplate() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		
		for (String inputName : inputDepths.keySet()) {
			addInput(inputName, inputDepths.get(inputName), true, null, String.class);
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
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				
				String id = generateId(callback);
				
				Map<String, Object> inputData = new HashMap<String, Object>();
				for (String inputName : inputs.keySet()) {
					Object input = referenceService.renderIdentifier(inputs
                            .get(inputName), getInputPort(inputName)
                            .getTranslatedElementClass(), callback
                            .getContext());
					inputData.put(inputName, input);
				}

				InteractionJetty.checkJetty();
				synchronized(ABDERA) {
					Entry entry = ABDERA.newEntry();

					entry.setId(id);
					Date timestamp = new Date();
					entry.setPublished(timestamp);
					entry.setUpdated(timestamp);

					entry.addAuthor("Taverna");
					entry.setTitle("Interaction from Taverna for " + id);
					
					ObjectMapper mapper = new ObjectMapper();
					StringWriter sw = new StringWriter();
					try {
						mapper.writeValue (sw, inputData);
					} catch (JsonGenerationException e) {
						logger.error(e);
					} catch (JsonMappingException e) {
						logger.error(e);
					} catch (IOException e) {
						logger.error(e);
					}
					
					Element inputDataElement = entry.addExtension(getInputDataQName());
					String inputDataString = sw.toString();
					inputDataElement.setText(inputDataString);
					
					AbderaClient client = new AbderaClient(ABDERA);
					RequestOptions rOptions = client.getDefaultRequestOptions();
		            rOptions.setSlug(id);
		            String slug = rOptions.getHeader("Slug");

		            String webFile = generateHtml(inputData, inputDataString, id, slug);

						entry.addLink(webFile, "presentation");
							entry.setContentAsXhtml("<p><a href=\"" + webFile + "\">Open: " + webFile + "</a></p>");

					ClientResponse resp = client.post(
					InteractionPreference.getInstance().getFeedUrl(), entry, rOptions);
						client.teardown();
						FeedListener.getInstance().registerInteraction(entry, callback);
						}
			}
		});
	}
	
	private String generateHtml(Map<String, Object> inputData, String inputDataString, String id, String slug) {
		
		String slugForFile = Sanitizer.sanitize(slug, "", true, Normalizer.Form.D);
		
		VelocityContext velocityContext = new VelocityContext();
		for (String inputName : inputData.keySet()) {
			Object input = inputData.get(inputName);
			velocityContext.put(inputName, input);
		}
		velocityContext.put("feed", InteractionPreference.getInstance().getFeedUrl());
		velocityContext.put("entryId", id);
		String pmrpcUrl = InteractionPreference.getInstance().getLocationUrl() + "/" + "pmrpc.js";
		velocityContext.put("pmrpcUrl", pmrpcUrl);
		velocityContext.put("inputData", inputDataString);
		
		String presentationUrl = "";
        try {
			if (configBean.getInteractionActivityType().equals(
					InteractionActivityType.VelocityTemplate)) {
				// Write presentation frame file
				File presentationFile = new File(getTempDir(), "presentation"
						+ slugForFile + ".html");
				presentationFile.createNewFile();
				FileWriter presentationFileWriter = new FileWriter(
						presentationFile);
				template.merge(velocityContext, presentationFileWriter);
				presentationFileWriter.close();
				presentationUrl = InteractionPreference.getInstance()
						.getLocationUrl()
						+ "/" + presentationFile.getName();
			} else if (configBean.getInteractionActivityType().equals(
					InteractionActivityType.LocallyPresentedHtml)) {
				presentationUrl = configBean.getPresentationOrigin();
			}

		velocityContext.put("slug", slug);
		
		// Write communication frame file		
		File communicationFile = new File(getTempDir(), "communication" + slugForFile + ".html");
		communicationFile.createNewFile();
		FileWriter communicationFileWriter = new FileWriter(communicationFile);
		InteractionVelocity.getCommunicationTemplate().merge(velocityContext, communicationFileWriter);
		communicationFileWriter.close();
		
		String communicationUrl = InteractionPreference.getInstance().getLocationUrl() + "/" + communicationFile.getName();

		velocityContext.put("presentationUrl", presentationUrl);
		velocityContext.put("communicationUrl", communicationUrl);
		
		if (!configBean.getInteractionActivityType().equals(InteractionActivityType.RemotelyPresentededHtml)) {
		// Write main html file
		File mainFile = new File(getTempDir(), "interaction" + slugForFile + ".html");
		mainFile.createNewFile();
		FileWriter mainFileWriter = new FileWriter(mainFile);
		InteractionVelocity.getInteractionTemplate().merge(velocityContext, mainFileWriter);
		mainFileWriter.close();
		
		return InteractionPreference.getInstance().getLocationUrl() + "/" + mainFile.getName();
		} else {
			return (configBean.getPresentationOrigin() + "?communicationFrame=" + communicationUrl);
		}
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	protected static String generateId(AsynchronousActivityCallback callback) {
		String workflowRunId = callback.getContext().getEntities(WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
		return (workflowRunId + ":" + callback.getParentProcessIdentifier());
	}

	public static File getTempDir() {
			File tempDir = new File(InteractionPreference.getInstance()
					.getPresentationDirectory());
		return tempDir;
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

	public static QName getResultDataQName() {
		return resultDataQName;
	}

	public static QName getResultStatusQName() {
		return resultStatusQName;
	}



}
