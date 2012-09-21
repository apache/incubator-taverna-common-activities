/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * @author alanrw
 *
 */
public class InteractionCallbackRequestor implements InteractionRequestor {

	private AsynchronousActivityCallback callback;
	
	private  Map<String, T2Reference> inputs;

	private final InteractionActivity activity;
	
	public InteractionCallbackRequestor(InteractionActivity activity, AsynchronousActivityCallback callback, Map<String, T2Reference> inputs) {
		this.activity = activity;
		this.callback = callback;
		this.inputs = inputs;		
	}

	@Override
	public String getRunId() {
		return callback.getContext().getEntities(
				WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
	}

	@Override
	public Map<String, Object> getInputData() {
		Map<String, Object> inputData = new HashMap<String, Object>();
		
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context
		.getReferenceService();
		for (String inputName : inputs.keySet()) {
			Object input = referenceService
					.renderIdentifier(inputs.get(inputName),
							getInputPort(inputName)
									.getTranslatedElementClass(),
							callback.getContext());
			inputData.put(inputName, input);
		}
		return inputData;
	}

	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : activity.getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	@Override
	public void fail(String string) {
		callback.fail(string);
	}

	@Override
	public void carryOn() {
		callback.receiveResult(new HashMap<String, T2Reference>(), new int[0]);
	}

	@Override
	public String generateId() {
			String workflowRunId = callback.getContext().getEntities(
					WorkflowRunIdEntity.class).get(0).getWorkflowRunId();
			return (workflowRunId + ":" + callback.getParentProcessIdentifier());
	}

	@Override
	public InteractionType getInteractionType() {
		if (activity.getConfiguration().isProgressNotification()) {
			return InteractionType.Notification;
		}
		return InteractionType.DataRequest;
	}

	@Override
	public InteractionActivityType getPresentationType() {
		return activity.getConfiguration().getInteractionActivityType();
	}

	@Override
	public String getPresentationOrigin() {
		return activity.getConfiguration().getPresentationOrigin();
	}

	@Override
	public void receiveResult(Map<String, Object> resultMap) {
		Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
		
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context
		.getReferenceService();

		for (Object key : resultMap.keySet()) {
			String keyString = (String) key;
			Object value = resultMap.get(key);
			Integer depth = findPortDepth(keyString);
			if (depth == null) {
				callback.fail("Data sent for unknown port : " + keyString);
			}
			outputs.put(keyString, referenceService.register(value, depth, true, context));
		}
		callback.receiveResult(outputs, new int[0]);
	}
	
	private Integer findPortDepth(String portName) {
		Set<OutputPort> ports = activity.getOutputPorts();
		for (OutputPort op : ports) {
			if (op.getName().equals(portName)) {
				return op.getDepth();
			}
		}
		return null;
	}
}
