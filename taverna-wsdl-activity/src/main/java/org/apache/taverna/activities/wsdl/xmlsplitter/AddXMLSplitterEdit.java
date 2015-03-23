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

package org.apache.taverna.activities.wsdl.xmlsplitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.activities.wsdl.WSDLActivity;
import org.apache.taverna.workflowmodel.CompoundEdit;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Edit;
import org.apache.taverna.workflowmodel.EditException;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.EventForwardingOutputPort;
import org.apache.taverna.workflowmodel.EventHandlingInputPort;
import org.apache.taverna.workflowmodel.InputPort;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.ProcessorInputPort;
import org.apache.taverna.workflowmodel.ProcessorOutputPort;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.utils.Tools;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;

import com.fasterxml.jackson.databind.JsonNode;

public class AddXMLSplitterEdit implements Edit<Dataflow> {

	private final Edits edits;
	private final Activity<?> activity;
	private final String portName;
	private final boolean isInput;
	private CompoundEdit compoundEdit1 = null;
	private Edit<?> linkUpEdit;
	private final Dataflow dataflow;
	private boolean applied = false;

	public AddXMLSplitterEdit(Dataflow dataflow, Activity<?> activity,
			String portName, boolean isInput, Edits edits) {
		this.dataflow = dataflow;
		this.activity = activity;
		this.portName = portName;
		this.isInput = isInput;
		this.edits = edits;
	}

	@Override
	public Dataflow doEdit() throws EditException {
		if (applied) throw new EditException("Edit has already been applied!");
		List<Edit<?>> editList = new ArrayList<Edit<?>>();

		Activity<JsonNode> splitter = null;
		String sourcePortName = "";
		Processor sourceProcessor = null;
		Activity<?> sourceActivity = null;

		String sinkPortName = "";
		Processor sinkProcessor = null;
		Activity<?> sinkActivity = null;


		Processor activityProcessor = findProcessorForActivity(dataflow,
				activity);
		if (activityProcessor == null) {
			throw new EditException(
					"Cannot find the processor that the activity belongs to");
		}


		String displayName = portName;
		if (portName.equals("parameters")) {
			displayName = isInput ? "input" : "output";
		}
		String processorName = activityProcessor.getLocalName();
		String candidateName;
		if (displayName.startsWith(processorName)) {
			// No need to make GetRequest_GetRequestResponse
			candidateName = displayName;
		} else {
			// Combine with processor name
			String displayProcessorName;
			if (activity instanceof XMLOutputSplitterActivity || activity instanceof XMLInputSplitterActivity) {
				// For splitters on splitters - avoid adding up blah_bluh_blih_more_stuff
				String[] processorNameSplit = processorName.replace("_input", "").replace("_output", "").split("_");
				displayProcessorName = processorNameSplit[processorNameSplit.length-1];
			} else {
				displayProcessorName = activityProcessor.getLocalName();
			}
			candidateName = displayProcessorName + "_" + displayName;
		}
		String name = Tools.uniqueProcessorName(candidateName, dataflow);
		Processor splitterProcessor = edits.createProcessor(name);

		try {
			if (activity instanceof XMLInputSplitterActivity) {
				if (!isInput) {
					throw new EditException(
							"Can only add an input splitter to another input splitter");
				}
				TypeDescriptor descriptor = ((XMLInputSplitterActivity) activity)
						.getTypeDescriptorForInputPort(portName);
				if (descriptor instanceof ArrayTypeDescriptor && !((ArrayTypeDescriptor)descriptor).isWrapped()) {
					descriptor=((ArrayTypeDescriptor)descriptor).getElementType();
				}

				JsonNode bean = XMLSplitterConfigurationBeanBuilder
						.buildBeanForInput(descriptor);
				splitter = new XMLInputSplitterActivity();
				editList.add(edits.getConfigureActivityEdit(splitter, bean));

			} else if (activity instanceof XMLOutputSplitterActivity) {
				if (isInput) {
					throw new EditException(
							"Can only add an output splitter to another output splitter");
				}
				TypeDescriptor descriptor = ((XMLOutputSplitterActivity) activity)
						.getTypeDescriptorForOutputPort(portName);

				if (descriptor instanceof ArrayTypeDescriptor && !((ArrayTypeDescriptor)descriptor).isWrapped()) {
					descriptor=((ArrayTypeDescriptor)descriptor).getElementType();
				}

				JsonNode bean = XMLSplitterConfigurationBeanBuilder
						.buildBeanForOutput(descriptor);
				splitter = new XMLOutputSplitterActivity();
				editList.add(edits.getConfigureActivityEdit(splitter, bean));

			} else if (activity instanceof WSDLActivity) {
				if (isInput) {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForInputPort(portName);
					JsonNode bean = XMLSplitterConfigurationBeanBuilder
							.buildBeanForInput(descriptor);
					splitter = new XMLInputSplitterActivity();
					editList
							.add(edits.getConfigureActivityEdit(splitter, bean));
				} else {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForOutputPort(portName);
					JsonNode bean = XMLSplitterConfigurationBeanBuilder
							.buildBeanForOutput(descriptor);
					splitter = new XMLOutputSplitterActivity();
					editList
							.add(edits.getConfigureActivityEdit(splitter, bean));
				}
			} else {
				throw new EditException(
						"The activity type is not suitable for adding xml processing processors");
			}
		} catch (Exception e) {
			throw new EditException(
					"An error occured whilst tyring to add an XMLSplitter to the activity:"
							+ activity, e);
		}

		if (isInput) {
			sourcePortName = "output";
			sinkPortName = portName;
			sinkProcessor = activityProcessor;
			sinkActivity = activity;
			sourceProcessor = splitterProcessor;
			sourceActivity = splitter;
		}
		else {
			sourcePortName = portName;
			sinkPortName = "input";
			sinkProcessor = splitterProcessor;
			sinkActivity = splitter;
			sourceProcessor = activityProcessor;
			sourceActivity = activity;
		}

		editList.add(edits.getDefaultDispatchStackEdit(splitterProcessor));
		editList.add(edits.getAddActivityEdit(splitterProcessor, splitter));
//		editList.add(edits
//				.getMapProcessorPortsForActivityEdit(splitterProcessor));
		editList.add(edits.getAddProcessorEdit(dataflow, splitterProcessor));

		compoundEdit1 = new CompoundEdit(editList);
		compoundEdit1.doEdit();

		List<Edit<?>> linkUpEditList = new ArrayList<Edit<?>>();

		EventForwardingOutputPort source = getSourcePort(sourceProcessor, sourceActivity,
				sourcePortName, linkUpEditList);
		EventHandlingInputPort sink = getSinkPort(sinkProcessor, sinkActivity, sinkPortName, linkUpEditList);

		if (source == null)
			throw new EditException(
					"Unable to find the source port when linking up "
							+ sourcePortName + " to " + sinkPortName);
		if (sink == null)
			throw new EditException(
					"Unable to find the sink port when linking up "
							+ sourcePortName + " to " + sinkPortName);

		linkUpEditList.add(org.apache.taverna.workflowmodel.utils.Tools.getCreateAndConnectDatalinkEdit(dataflow, source, sink, edits));

		linkUpEdit = new CompoundEdit(linkUpEditList);
		linkUpEdit.doEdit();
		applied = true;
		return dataflow;
	}

	private EventHandlingInputPort getSinkPort(Processor processor, Activity<?> activity,
			String portName, List<Edit<?>> editList) {
		InputPort activityPort = org.apache.taverna.workflowmodel.utils.Tools.getActivityInputPort(activity, portName);
		//check if processor port exists
		EventHandlingInputPort input = org.apache.taverna.workflowmodel.utils.Tools.getProcessorInputPort(processor, activity, activityPort);
		if (input == null) {
			//port doesn't exist so create a processor port and map it
			ProcessorInputPort processorInputPort =
				edits.createProcessorInputPort(processor, activityPort.getName(), activityPort.getDepth());
			editList.add(edits.getAddProcessorInputPortEdit(processor, processorInputPort));
			editList.add(edits.getAddActivityInputPortMappingEdit(activity, activityPort.getName(), activityPort.getName()));
			input = processorInputPort;
		}
		return input;
	}

	private EventForwardingOutputPort getSourcePort(Processor processor, Activity<?> activity,
			String portName, List<Edit<?>> editList) {
		OutputPort activityPort = org.apache.taverna.workflowmodel.utils.Tools.getActivityOutputPort(activity, portName);
		//check if processor port exists
		EventForwardingOutputPort output = org.apache.taverna.workflowmodel.utils.Tools.getProcessorOutputPort(processor, activity, activityPort);
		if (output == null) {
			//port doesn't exist so create a processor port and map it
			ProcessorOutputPort processorOutputPort =
				edits.createProcessorOutputPort(processor, activityPort.getName(), activityPort.getDepth(), activityPort.getGranularDepth());
			editList.add(edits.getAddProcessorOutputPortEdit(processor, processorOutputPort));
			editList.add(edits.getAddActivityOutputPortMappingEdit(activity, activityPort.getName(), activityPort.getName()));
			output = processorOutputPort;
		}
		return output;
	}

	@Override
	public void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		if (linkUpEdit.isApplied())
			linkUpEdit.undo();
		if (compoundEdit1.isApplied())
			compoundEdit1.undo();
		applied = false;
	}

	@Override
	public boolean isApplied() {
		return applied;
	}

	private Processor findProcessorForActivity(Dataflow dataflow,
			Activity<?> activity) {
		for (Processor p : dataflow.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a == activity)
					return p;
			}
		}
		return null;
	}

	@Override
	public Object getSubject() {
		return dataflow;
	}

}
