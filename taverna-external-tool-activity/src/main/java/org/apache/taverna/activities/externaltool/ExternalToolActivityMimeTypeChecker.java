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

package org.apache.taverna.activities.externaltool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Datalink;
import org.apache.taverna.workflowmodel.Merge;
import org.apache.taverna.workflowmodel.MergeInputPort;
import org.apache.taverna.workflowmodel.MergeOutputPort;
import org.apache.taverna.workflowmodel.MergePort;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.workflowmodel.Port;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.ProcessorInputPort;
import org.apache.taverna.workflowmodel.ProcessorPort;
import org.apache.taverna.workflowmodel.health.HealthCheck;
import org.apache.taverna.workflowmodel.health.HealthChecker;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;
import org.apache.taverna.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.ScriptInputUser;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;

/**
 * @author alanrw
 *
 */
public final class ExternalToolActivityMimeTypeChecker implements HealthChecker<ExternalToolActivity> {
	
	private static Logger logger = Logger.getLogger(ExternalToolActivityMimeTypeChecker.class);

	public boolean canVisit(Object o) {
		return o != null && o instanceof ExternalToolActivity;
	}

	public boolean isTimeConsuming() {
		return false;
	}

	public VisitReport visit(ExternalToolActivity o,
			List<Object> ancestry) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
			Processor p = (Processor) VisitReport.findAncestor(ancestry, Processor.class);
			Dataflow d = (Dataflow) VisitReport.findAncestor(ancestry, Dataflow.class);
			
			for (ActivityInputPort aip : o.getInputPorts()) {
				Set<String> sinkMimeSet = getMimeTypesForInput(o, aip);
				if (sinkMimeSet.isEmpty()) {
					continue;
				}
				ProcessorInputPort pip = Tools.getProcessorInputPort(p, (Activity<?>) o, aip);
				
				for (Datalink dl : d.getLinks()) {

					if (dl.getSink().equals(pip)) {
						Port source = dl.getSource();
						Set<VisitReport> subReports = checkSource(dl, source, d, (Activity) o, aip, sinkMimeSet);
						for (VisitReport vr : subReports) {
							vr.setProperty("sinkProcessor", p);
						    vr.setProperty("activity", o);
						    vr.setProperty("sinkPort", pip);
						}
						reports.addAll(subReports);
					}
				}
			}
			
		if (reports.isEmpty()) {
			return null;
		}
		if (reports.size() == 1) {
			return reports.get(0);
		}
		else {
			return new VisitReport(HealthCheck.getInstance(), o, "Collation", HealthCheck.DEFAULT_VALUE, reports);
		}
	}
	
	private Set<VisitReport> checkSource(Datalink datalink, Port source, Dataflow d, Activity o, ActivityInputPort aip, Set<String> sinkMimeSet) {
		Set<VisitReport> reports = new HashSet<VisitReport>();
		if (source instanceof ProcessorPort) {
			ProcessorPort processorPort = (ProcessorPort) source;
			Processor sourceProcessor = processorPort.getProcessor();
			Activity sourceActivity = sourceProcessor.getActivityList().get(0);
			if (sourceActivity instanceof ExternalToolActivity) {
				ActivityOutputPort aop = getActivityOutputPort(sourceActivity, processorPort);
				Set<String> sourceMimeTypes = getMimeTypesForOutput((ExternalToolActivity) sourceActivity, aop);
				if (!sourceMimeTypes.isEmpty()) {
					Set<String> sinkMimeTypesClone  = new HashSet<String>();
					sinkMimeTypesClone.addAll(sinkMimeSet);
					sinkMimeTypesClone.retainAll(sourceMimeTypes);
					if (sinkMimeTypesClone.isEmpty()) {
						VisitReport vr = new VisitReport(HealthCheck.getInstance(), o, "Incompatible mime types", HealthCheck.INCOMPATIBLE_MIMETYPES, Status.WARNING);
						vr.setProperty("sourcePort", processorPort);
						vr.setProperty("sourceProcessor", sourceProcessor);
						vr.setProperty("link", datalink);
						reports.add(vr);
					}
				}
			}
		} else if (source instanceof MergeOutputPort) {
			Merge merge = ((MergePort) source).getMerge();
			for (MergeInputPort mip : merge.getInputPorts()) {
				for (Datalink dl : d.getLinks()) {
					if (dl.getSink().equals(mip)) {
						reports.addAll(checkSource(dl, dl.getSource(), d, o, aip, sinkMimeSet));
					}
				}
				
			}
		}
		return reports;
	}
	
	private Set<String> getMimeTypesForOutput(ExternalToolActivity o, ActivityOutputPort aop) {
		ScriptOutput so = (ScriptOutput) o.getConfiguration().getUseCaseDescription().getOutputs().get(aop.getName());
		if (so == null) {
			return Collections.EMPTY_SET;
		}
		List mimeList = Arrays.asList(so.getMime());
		Set mimeSet = new HashSet(mimeList);
		return mimeSet;
	}

	private Set<String> getMimeTypesForInput(ExternalToolActivity a, ActivityInputPort aip) {
		ScriptInputUser si = (ScriptInputUser) a.getConfiguration().getUseCaseDescription().getInputs().get(aip.getName());
		if (si == null) {
			return Collections.EMPTY_SET;
		}
		ArrayList<String> mime = si.getMime();
		if (mime != null) {
			List mimeList = Arrays.asList(mime);
			Set mimeSet = new HashSet(mimeList);
		
			return mimeSet;
		} else {
			return Collections.EMPTY_SET;
		}
	}
	
	private static ActivityOutputPort getActivityOutputPort(
			Activity<?> activity, ProcessorPort processorOutputPort) {
		ProcessorInputPort result = null;
		for (Entry<String, String> mapEntry : activity.getOutputPortMapping()
				.entrySet()) {
			if (mapEntry.getKey().equals(processorOutputPort.getName())) {
				for (OutputPort activityOutputPort : activity
						.getOutputPorts()) {
					if (activityOutputPort.getName().equals(mapEntry.getValue())) {
						return (ActivityOutputPort) activityOutputPort;
					}
				}
				break;
			}
		}
		return null;
	}

}
