/**
 *
 */
package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public final class WSDLActivityDescriptorChecker implements HealthChecker<InputPortTypeDescriptorActivity> {

	private static Logger logger = Logger.getLogger(WSDLActivityDescriptorChecker.class);

	public boolean canVisit(Object o) {
		return ((o != null) && (o instanceof InputPortTypeDescriptorActivity));
	}

	public boolean isTimeConsuming() {
		return false;
	}

	public VisitReport visit(InputPortTypeDescriptorActivity o,
			List<Object> ancestry) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
		try {
			Map<String, TypeDescriptor> typeMap = o.getTypeDescriptorsForInputPorts();
			Processor p = (Processor) VisitReport.findAncestor(ancestry, Processor.class);
			Dataflow d = (Dataflow) VisitReport.findAncestor(ancestry, Dataflow.class);


			for (Entry<String, TypeDescriptor> entry : typeMap.entrySet()) {
				TypeDescriptor descriptor = entry.getValue();
				if (!descriptor.getMimeType().contains("'text/xml'")) {
					continue;
				}
				if (!((descriptor instanceof ArrayTypeDescriptor) || (descriptor instanceof ComplexTypeDescriptor))) {
					continue;
				}
				// Find the processor port, if any that corresponds to the activity port
				ActivityInputPort aip = Tools.getActivityInputPort((Activity<?>) o, entry.getKey());
				if (aip == null) {
					continue;
				}
				ProcessorInputPort pip = Tools.getProcessorInputPort(p, (Activity<?>) o, aip);

				if (pip == null) {
					continue;
				}

				for (Datalink dl : d.getLinks()) {

					if (dl.getSink().equals(pip)) {
						Port source = dl.getSource();
						Set<VisitReport> subReports = checkSource(source, d, (Activity<?>) o, aip);
						for (VisitReport vr : subReports) {
						    vr.setProperty("activity", o);
						    vr.setProperty("sinkPort", pip);
						}
						reports.addAll(subReports);
					}
				}

			}
		} catch (UnknownOperationException e) {
			logger.error("Problem getting type descriptors for activity", e);
		} catch (IOException e) {
			logger.error("Problem getting type descriptors for activity", e);
		} catch (NullPointerException e) {
			logger.error("Problem getting type desciptors for activity", e);
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

	private Set<VisitReport> checkSource(Port source, Dataflow d, Activity<?> o, ActivityInputPort aip) {
		Set<VisitReport> reports = new HashSet<VisitReport>();
		if (source instanceof ProcessorPort) {
			ProcessorPort processorPort = (ProcessorPort) source;
			Processor sourceProcessor = processorPort.getProcessor();
			Activity<?> sourceActivity = sourceProcessor.getActivityList().get(0);
			if (!(sourceActivity instanceof InputPortTypeDescriptorActivity)) {
				VisitReport newReport = new VisitReport(HealthCheck.getInstance(), o, "Source of " + aip.getName(), HealthCheck.DATATYPE_SOURCE, Status.WARNING);
				newReport.setProperty("sinkPortName", aip.getName());
				newReport.setProperty("sourceName", sourceProcessor.getLocalName());
				newReport.setProperty("isProcessorSource", "true");
				reports.add(newReport);
			}
		} else if (source instanceof MergeOutputPort) {
			Merge merge = ((MergePort) source).getMerge();
			for (MergeInputPort mip : merge.getInputPorts()) {
				for (Datalink dl : d.getLinks()) {
					if (dl.getSink().equals(mip)) {
						reports.addAll(checkSource(dl.getSource(), d, o, aip));
					}
				}

			}
		} else /* if (source instanceof DataflowInputPort) */  {
			VisitReport newReport = new VisitReport(HealthCheck.getInstance(), o, "Source of " + aip.getName(), HealthCheck.DATATYPE_SOURCE, Status.WARNING);
			newReport.setProperty("sinkPortName", aip.getName());
			newReport.setProperty("sourceName", source.getName());
			newReport.setProperty("isProcessorSource", "false");
			reports.add(newReport);
		}
		return reports;
	}

}
