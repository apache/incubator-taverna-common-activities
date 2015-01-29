/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

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
