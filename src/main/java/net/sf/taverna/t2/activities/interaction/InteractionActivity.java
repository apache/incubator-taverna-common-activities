package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.InteractionActivityType.VelocityTemplate;
import static net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity.checkVelocity;
import static net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity.getInteractionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.activities.interaction.velocity.NotifyChecker;
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

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.parser.node.ASTprocess;

public final class InteractionActivity extends
		AbstractAsynchronousActivity<InteractionActivityConfigurationBean>
		implements AsynchronousActivity<InteractionActivityConfigurationBean> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(InteractionActivity.class);

	InteractionActivityConfigurationBean configBean;

	private Template presentationTemplate;

	private final Map<String, Integer> inputDepths = new HashMap<>();
	private final Map<String, Integer> outputDepths = new HashMap<>();

	public InteractionActivity() {
		this.configBean = new InteractionActivityConfigurationBean();
	}

	@Override
	public void configure(InteractionActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		/*
		 * Store for getConfiguration(), but you could also make
		 * getConfiguration() return a new bean from other sources
		 */
		this.configBean = configBean;

		inputDepths.clear();
		outputDepths.clear();

		checkVelocity();

		if (configBean.getInteractionActivityType().equals(VelocityTemplate)) {
			try {
				presentationTemplate = getInteractionTemplate(configBean);
				extractTemplateInfo((ASTprocess) presentationTemplate
						.getData());
				configurePortsFromTemplate();
			} catch (VelocityException e) {
				throw new ActivityConfigurationException(
						"Unable to find/parse template", e);
			}
		}
		configurePorts(configBean);
	}

	private void extractTemplateInfo(ASTprocess parsedTemplate)
			throws VelocityException {
		new RequireChecker().visit(parsedTemplate, inputDepths);
		new ProduceChecker().visit(parsedTemplate, outputDepths);
		new NotifyChecker().visit(parsedTemplate, configBean);
	}

	protected void configurePortsFromTemplate() {
		List<ActivityInputPortDefinitionBean> inputs = new ArrayList<>();

		for (Entry<String, Integer> inputDepth : inputDepths.entrySet()) {
			ActivityInputPortDefinitionBean inputBean = new ActivityInputPortDefinitionBean();
			inputBean.setName(inputDepth.getKey());
			inputBean.setDepth(inputDepth.getValue());
			inputBean.setAllowsLiteralValues(true);
			inputBean.setHandledReferenceSchemes(null);
			inputBean.setTranslatedElementType(String.class);
			inputs.add(inputBean);
		}
		configBean.setInputPortDefinitions(inputs);

		List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<>();
		for (Entry<String, Integer> outputDepth : outputDepths.entrySet()) {
			ActivityOutputPortDefinitionBean outputBean = new ActivityOutputPortDefinitionBean();
			outputBean.setName(outputDepth.getKey());
			outputBean.setDepth(outputDepth.getValue());
			outputBean.setGranularDepth(outputDepth.getValue());
			outputs.add(outputBean);
		}
		configBean.setOutputPortDefinitions(outputs);
	}

	@Override
	public void executeAsynch(Map<String, T2Reference> inputs,
			AsynchronousActivityCallback callback) {
		/*
		 * Don't execute service directly now, request to be run ask to be run
		 * from thread pool and return asynchronously
		 */
		InteractionRequestor requestor = new InteractionCallbackRequestor(this,
				callback, inputs);
		callback.requestRun(new InteractionActivityRunnable(requestor,
				presentationTemplate));
	}

	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		return configBean;
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
