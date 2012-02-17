package net.sf.taverna.t2.activities.interaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
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
	
	private static String TEMPLATE_SUFFIX = ".vm";
	
	private InteractionActivityConfigurationBean configBean;
	
	private static boolean velocityInitialized = false;
	
	private static Abdera ABDERA = Abdera.getInstance();

	private static File tempDir;
	
	private Template template;
	
	private Map<String, Integer> inputDepths = new HashMap<String, Integer> ();
	private Map<String, Integer> outputDepths = new HashMap<String, Integer> ();
	
	private static QName inputDataQName = new QName("http://www.taverna.org.uk/interaction", "inputData", "interaction");
	private static QName resultDataQName = new QName("http://www.taverna.org.uk/interaction", "resultData", "interaction");
	private static QName resultStatusQName = new QName("http://www.taverna.org.uk/interaction", "resultStatus", "interaction");

	@Override
	public void configure(InteractionActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;
		
		if (!velocityInitialized) {
			initializeVelocity();
		}
		
		template = Velocity.getTemplate(configBean.getTemplateName() + TEMPLATE_SUFFIX);
		inputDepths.clear();
		RequireChecker requireChecker = new RequireChecker();
		requireChecker.visit((ASTprocess) template.getData(), inputDepths);
		
		outputDepths.clear();
		ProduceChecker produceChecker = new ProduceChecker();
		produceChecker.visit((ASTprocess) template.getData(), outputDepths);

		configurePorts();
	}

	private void initializeVelocity() {
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
        Velocity.setProperty("userdirective", RequireDirective.class.getName() + "," + ProduceDirective.class.getName());
        Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init();
        velocityInitialized = true;

	}

	protected void configurePorts() {
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

				// Only do if working with template
				VelocityContext velocityContext = new VelocityContext();
				for (String inputName : inputs.keySet()) {
					Object input = inputData.get(inputName);
					velocityContext.put(inputName, input);
				}
				velocityContext.put("feed", InteractionPreference.getInstance().getFeedUrl());
				velocityContext.put("entryId", id);

                File tempFile = null;
                try {
					tempFile = File.createTempFile("interaction", ".html", getTempDir());
					FileWriter fileWriter = new FileWriter(tempFile);
					template.merge(velocityContext, fileWriter);
					fileWriter.close();
				} catch (IOException e) {
					logger.error(e);
					callback.fail("Unable to write HTML file", e);
					return;
				}
				// End of template specific part
				
				synchronized(ABDERA) {
					Entry entry = ABDERA.newEntry();

					entry.setId(id);
					Date timestamp = new Date();
					entry.setPublished(timestamp);
					entry.setUpdated(timestamp);

					entry.addAuthor("Taverna");
					entry.setTitle("Interaction");
					
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
					inputDataElement.setText(sw.toString());
					
						String lastFilePart = tempFile.getName();
						String webFile = InteractionPreference.getInstance().getLocationUrl() + "/" + lastFilePart;
						entry.addLink(webFile, "presentation");
						entry.setContentAsXhtml("<p><a href=\"" + webFile + "\">Open: " + webFile + "</a></p>");



					AbderaClient client = new AbderaClient(ABDERA);
					RequestOptions rOptions = client.getDefaultRequestOptions();
		            rOptions.setSlug(id);
					ClientResponse resp = client.post(
					InteractionPreference.getInstance().getFeedUrl(), entry, rOptions);
						client.teardown();
						FeedListener.getInstance().registerInteraction(entry, callback);
						}
			}
		});
	}

	protected static String generateId(AsynchronousActivityCallback callback) {
		String workflowRunId = callback.getContext().getEntities(WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
		return (workflowRunId + ":" + callback.getParentProcessIdentifier());
	}

	protected static File getTempDir() {
		if (tempDir == null) {
			tempDir = new File(InteractionPreference.getInstance().getPresentationDirectory());
		}
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
