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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceChecker;
import net.sf.taverna.t2.activities.interaction.velocity.RequireChecker;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.security.oauth.OAuthUtils;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.abdera.Abdera;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.parser.node.ASTprocess;

public class InteractionActivity extends
		AbstractAsynchronousActivity<InteractionActivityConfigurationBean>
		implements AsynchronousActivity<InteractionActivityConfigurationBean> {

	private static Logger logger = Logger.getLogger(InteractionActivity.class);

	InteractionActivityConfigurationBean configBean;

	static Abdera ABDERA = Abdera.getInstance();

	private Template presentationTemplate;

	private Map<String, Integer> inputDepths = new HashMap<String, Integer>();
	private Map<String, Integer> outputDepths = new HashMap<String, Integer>();

	
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
		InteractionRequestor requestor = new InteractionCallbackRequestor(this, callback, inputs);
		callback.requestRun(new InteractionActivityRunnable(requestor, presentationTemplate));
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



}
