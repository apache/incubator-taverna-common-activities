/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.taverna.invocation.InvocationContext;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.reference.WorkflowRunIdEntity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;

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
		if (this.activity.isProgressNotification()) {
			return InteractionType.Notification;
		}
		return InteractionType.DataRequest;
	}

	@Override
	public InteractionActivityType getPresentationType() {
		return this.activity.getInteractionActivityType();
	}

	@Override
	public String getPresentationOrigin() {
		return this.activity.getPresentationOrigin();
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
		final Set<ActivityOutputPort> ports = this.activity.getOutputPorts();
		for (final ActivityOutputPort op : ports) {
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
			if (!result.isEmpty()) {
				result += ":";
			}
			result += parts[i];
		}
		return result;
	}

	@Override
	public String getPath() {
		return this.path;
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
