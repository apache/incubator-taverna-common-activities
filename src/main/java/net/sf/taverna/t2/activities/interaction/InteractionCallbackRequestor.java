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
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * @author alanrw
 * 
 */
public class InteractionCallbackRequestor implements InteractionRequestor {

	private final AsynchronousActivityCallback callback;

	private final Map<String, T2Reference> inputs;

	private final InteractionActivity activity;

	private boolean answered = false;

	private String path;

	private Integer count;
	
	private static Map<String, Integer> invocationCount = new HashMap<String, Integer> ();

	public InteractionCallbackRequestor(final InteractionActivity activity,
			final AsynchronousActivityCallback callback,
			final Map<String, T2Reference> inputs) {
		this.activity = activity;
		this.callback = callback;
		this.inputs = inputs;
		this.path = calculatePath();
		this.count = calculateInvocationCount(path);
	}

	@Override
	public String getRunId() {
		return this.callback.getContext()
				.getEntities(WorkflowRunIdEntity.class).get(0)
				.getWorkflowRunId();
	}

	@Override
	public Map<String, Object> getInputData() {
		final Map<String, Object> inputData = new HashMap<String, Object>();

		final InvocationContext context = this.callback.getContext();
		final ReferenceService referenceService = context.getReferenceService();
		for (final String inputName : this.inputs.keySet()) {
			final Object input = referenceService.renderIdentifier(this.inputs
					.get(inputName), this.getInputPort(inputName)
					.getTranslatedElementClass(), this.callback.getContext());
			inputData.put(inputName, input);
		}
		return inputData;
	}

	public ActivityInputPort getInputPort(final String name) {
		for (final ActivityInputPort port : this.activity.getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	@Override
	public void fail(final String string) {
		if (this.answered) {
			return;
		}
		this.callback.fail(string);
		this.answered = true;
	}

	@Override
	public void carryOn() {
		if (this.answered) {
			return;
		}
		this.callback.receiveResult(new HashMap<String, T2Reference>(),
				new int[0]);
		this.answered = true;
	}

	@Override
	public String generateId() {
		final String workflowRunId = getRunId();
		final String parentProcessIdentifier = this.callback
				.getParentProcessIdentifier();
		return (workflowRunId + ":" + parentProcessIdentifier);
	}

	@Override
	public InteractionType getInteractionType() {
		if (this.activity.getConfiguration().isProgressNotification()) {
			return InteractionType.Notification;
		}
		return InteractionType.DataRequest;
	}

	@Override
	public InteractionActivityType getPresentationType() {
		return this.activity.getConfiguration().getInteractionActivityType();
	}

	@Override
	public String getPresentationOrigin() {
		return this.activity.getConfiguration().getPresentationOrigin();
	}

	@Override
	public void receiveResult(final Map<String, Object> resultMap) {
		if (this.answered) {
			return;
		}
		final Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();

		final InvocationContext context = this.callback.getContext();
		final ReferenceService referenceService = context.getReferenceService();

		for (final Object key : resultMap.keySet()) {
			final String keyString = (String) key;
			final Object value = resultMap.get(key);
			final Integer depth = this.findPortDepth(keyString);
			if (depth == null) {
				this.callback.fail("Data sent for unknown port : " + keyString);
			}
			outputs.put(keyString,
					referenceService.register(value, depth, true, context));
		}
		this.callback.receiveResult(outputs, new int[0]);
		this.answered = true;
	}

	private Integer findPortDepth(final String portName) {
		final Set<OutputPort> ports = this.activity.getOutputPorts();
		for (final OutputPort op : ports) {
			if (op.getName().equals(portName)) {
				return op.getDepth();
			}
		}
		return null;
	}

	private String calculatePath() {
		final String parentProcessIdentifier = this.callback
				.getParentProcessIdentifier();
		String result = "";
		String parts[] = parentProcessIdentifier.split(":");

		for (int i = 2; i < parts.length; i += 4) {
			result += ":" + parts[i];
		}
		return result;
	}

	@Override
	public String getPath() {
		return this.path;
	}
	
	private synchronized static Integer calculateInvocationCount(String path) {
		Integer currentCount = Integer.valueOf(0);
		if (invocationCount.containsKey(path)) {
			currentCount = currentCount + 1;
		}
		invocationCount.put(path, currentCount);
		return currentCount;
	}

	@Override
	public Integer getInvocationCount() {
		return count;
	}
}
