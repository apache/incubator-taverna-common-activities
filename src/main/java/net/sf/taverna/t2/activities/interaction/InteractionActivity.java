package net.sf.taverna.t2.activities.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.activities.interaction.velocity.ProduceChecker;
import net.sf.taverna.t2.activities.interaction.velocity.RequireChecker;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.abdera.Abdera;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
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
		}
			configurePorts(this.configBean);

	}

	protected void configurePortsFromTemplate() {
		List<ActivityInputPortDefinitionBean> inputs = new ArrayList<ActivityInputPortDefinitionBean>();

		for (String inputName : inputDepths.keySet()) {
			ActivityInputPortDefinitionBean inputBean = new ActivityInputPortDefinitionBean();
			inputBean.setName(inputName);
			inputBean.setDepth(inputDepths.get(inputName));
			inputBean.setAllowsLiteralValues(true);
			inputBean.setHandledReferenceSchemes(null);
				inputBean.setTranslatedElementType(String.class);
			inputs.add(inputBean);
		}
		this.configBean.setInputPortDefinitions(inputs);

		List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<ActivityOutputPortDefinitionBean>();
		for (String outputName : outputDepths.keySet()) {
			ActivityOutputPortDefinitionBean outputBean = new ActivityOutputPortDefinitionBean();
			outputBean.setName(outputName);
			outputBean.setDepth(outputDepths.get(outputName));
			outputBean.setGranularDepth(outputDepths.get(outputName));
			outputs.add(outputBean);
		}
		this.configBean.setOutputPortDefinitions(outputs);
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
