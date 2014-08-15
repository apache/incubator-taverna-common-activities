/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.InteractionType.DataRequest;
import static net.sf.taverna.t2.activities.interaction.InteractionType.Notification;

import java.util.HashMap;
import java.util.Map;

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

	private static Map<String, Integer> invocationCount = new HashMap<>();

	public InteractionCallbackRequestor(InteractionActivity activity,
			AsynchronousActivityCallback callback,
			Map<String, T2Reference> inputs) {
		this.activity = activity;
		this.callback = callback;
		this.inputs = inputs;
		this.path = calculatePath();
		this.count = calculateInvocationCount(path);
	}

	@Override
	public String getRunId() {
		return callback.getContext()
				.getEntities(WorkflowRunIdEntity.class).get(0)
				.getWorkflowRunId();
	}

	@Override
	public Map<String, Object> getInputData() {
		Map<String, Object> inputData = new HashMap<>();
		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();

		for (String inputName : inputs.keySet()) {
			Object input = referenceService.renderIdentifier(inputs
					.get(inputName), getInputPort(inputName)
					.getTranslatedElementClass(), callback.getContext());
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
		if (answered) {
			return;
		}
		callback.fail(string);
		answered = true;
	}

	@Override
	public void carryOn() {
		if (answered) {
			return;
		}
		callback.receiveResult(new HashMap<String, T2Reference>(),
				new int[0]);
		answered = true;
	}

	@Override
	public String generateId() {
		String workflowRunId = getRunId();
		String parentProcessIdentifier = callback.getParentProcessIdentifier();
		return (workflowRunId + ":" + parentProcessIdentifier);
	}

	@Override
	public InteractionType getInteractionType() {
		if (activity.getConfiguration().isProgressNotification()) {
			return Notification;
		}
		return DataRequest;
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
		if (answered) {
			return;
		}
		Map<String, T2Reference> outputs = new HashMap<>();

		InvocationContext context = callback.getContext();
		ReferenceService referenceService = context.getReferenceService();

		for (Object key : resultMap.keySet()) {
			String keyString = (String) key;
			Object value = resultMap.get(key);
			Integer depth = findPortDepth(keyString);
			if (depth == null) {
				callback.fail("Data sent for unknown port : " + keyString);
				continue;
			}
			outputs.put(keyString,
					referenceService.register(value, depth, true, context));
		}
		callback.receiveResult(outputs, new int[0]);
		answered = true;
	}

	private Integer findPortDepth(String portName) {
		for (OutputPort op : activity.getOutputPorts()) {
			if (op.getName().equals(portName)) {
				return op.getDepth();
			}
		}
		return null;
	}

	private String calculatePath() {
		String parentProcessIdentifier = callback.getParentProcessIdentifier();
		String result = "";
		String parts[] = parentProcessIdentifier.split(":");

		for (int i = 2; i < parts.length; i += 4) {
			if (!result.isEmpty()) {
				result += ":";
			}
			result += parts[i];
		}
		return result;
	}

	@Override
	public String getPath() {
		return path;
	}
	
	private synchronized static Integer calculateInvocationCount(String path) {
		Integer currentCount = invocationCount.get(path);
		if (currentCount == null) {
			currentCount = Integer.valueOf(0);
		} else {
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
