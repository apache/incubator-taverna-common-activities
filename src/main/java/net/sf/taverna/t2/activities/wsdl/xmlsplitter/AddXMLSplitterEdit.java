/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.AbstractDataflowEdit;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;

public class AddXMLSplitterEdit extends AbstractDataflowEdit {

	private final Edits edits = EditsRegistry.getEdits();
	private final Activity<?> activity;
	private final String portName;
	private final boolean isInput;
	private CompoundEdit compoundEdit1 = null;
	private Edit<?> linkUpEdit;

	public AddXMLSplitterEdit(Dataflow dataflow, Activity<?> activity,
			String portName, boolean isInput) {
		super(dataflow);
		this.activity = activity;
		this.portName = portName;
		this.isInput = isInput;

	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		List<Edit<?>> editList = new ArrayList<Edit<?>>();

		Activity<XMLSplitterConfigurationBean> splitter = null;
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
				
				XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
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
				
				XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
						.buildBeanForOutput(descriptor);
				splitter = new XMLOutputSplitterActivity();
				editList.add(edits.getConfigureActivityEdit(splitter, bean));
				
			} else if (activity instanceof WSDLActivity) {
				if (isInput) {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForInputPort(portName);
					XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
							.buildBeanForInput(descriptor);
					splitter = new XMLInputSplitterActivity();
					editList
							.add(edits.getConfigureActivityEdit(splitter, bean));
				} else {
					TypeDescriptor descriptor = ((WSDLActivity) activity)
							.getTypeDescriptorForOutputPort(portName);
					XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder
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

		linkUpEditList.add(net.sf.taverna.t2.workflowmodel.utils.Tools.getCreateAndConnectDatalinkEdit(dataflow, source, sink));

		linkUpEdit = new CompoundEdit(linkUpEditList);
		linkUpEdit.doEdit();

	}

	private EventHandlingInputPort getSinkPort(Processor processor, Activity<?> activity,
			String portName, List<Edit<?>> editList) {
		InputPort activityPort = net.sf.taverna.t2.workflowmodel.utils.Tools.getActivityInputPort(activity, portName);
		//check if processor port exists
		EventHandlingInputPort input = net.sf.taverna.t2.workflowmodel.utils.Tools.getProcessorInputPort(processor, activity, activityPort);
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
		OutputPort activityPort = net.sf.taverna.t2.workflowmodel.utils.Tools.getActivityOutputPort(activity, portName);
		//check if processor port exists
		EventForwardingOutputPort output = net.sf.taverna.t2.workflowmodel.utils.Tools.getProcessorOutputPort(processor, activity, activityPort);
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
	protected void undoEditAction(DataflowImpl dataflow) {
		if (linkUpEdit.isApplied())
			linkUpEdit.undo();
		if (compoundEdit1.isApplied())
			compoundEdit1.undo();
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

}
