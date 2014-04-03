package net.sf.taverna.t2.activities.interaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.parser.node.ASTprocess;

public final class InteractionActivity extends
		AbstractAsynchronousActivity<InteractionActivityConfigurationBean>
		implements AsynchronousActivity<InteractionActivityConfigurationBean> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(InteractionActivity.class);

	InteractionActivityConfigurationBean configBean;

	private Template presentationTemplate;

	private final Map<String, Integer> inputDepths = new HashMap<String, Integer>();
	private final Map<String, Integer> outputDepths = new HashMap<String, Integer>();

	public InteractionActivity() {
		this.configBean = new InteractionActivityConfigurationBean();
	}

	@Override
	public void configure(final InteractionActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		this.inputDepths.clear();
		this.outputDepths.clear();

		InteractionVelocity.checkVelocity();

		if (this.configBean.getInteractionActivityType().equals(
				InteractionActivityType.VelocityTemplate)) {
			this.presentationTemplate = Velocity.getTemplate(configBean
					.getPresentationOrigin());
			final RequireChecker requireChecker = new RequireChecker();
			requireChecker.visit(
					(ASTprocess) this.presentationTemplate.getData(),
					this.inputDepths);

			final ProduceChecker produceChecker = new ProduceChecker();
			produceChecker.visit(
					(ASTprocess) this.presentationTemplate.getData(),
					this.outputDepths);
			
			final NotifyChecker notifyChecker = new NotifyChecker();
			notifyChecker.visit(
					(ASTprocess) this.presentationTemplate.getData(),
					this.configBean);
			this.configurePortsFromTemplate();
		}
		this.configurePorts(this.configBean);

	}

	protected void configurePortsFromTemplate() {
		final List<ActivityInputPortDefinitionBean> inputs = new ArrayList<ActivityInputPortDefinitionBean>();

		for (final String inputName : this.inputDepths.keySet()) {
			final ActivityInputPortDefinitionBean inputBean = new ActivityInputPortDefinitionBean();
			inputBean.setName(inputName);
			inputBean.setDepth(this.inputDepths.get(inputName));
			inputBean.setAllowsLiteralValues(true);
			inputBean.setHandledReferenceSchemes(null);
			inputBean.setTranslatedElementType(String.class);
			inputs.add(inputBean);
		}
		this.configBean.setInputPortDefinitions(inputs);

		final List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<ActivityOutputPortDefinitionBean>();
		for (final String outputName : this.outputDepths.keySet()) {
			final ActivityOutputPortDefinitionBean outputBean = new ActivityOutputPortDefinitionBean();
			outputBean.setName(outputName);
			outputBean.setDepth(this.outputDepths.get(outputName));
			outputBean.setGranularDepth(this.outputDepths.get(outputName));
			outputs.add(outputBean);
		}
		this.configBean.setOutputPortDefinitions(outputs);
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		final InteractionRequestor requestor = new InteractionCallbackRequestor(
				this, callback, inputs);
		callback.requestRun(new InteractionActivityRunnable(requestor,
				this.presentationTemplate));
	}

	@Override
	public InteractionActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	public ActivityInputPort getInputPort(final String name) {
		for (final ActivityInputPort port : this.getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

}
