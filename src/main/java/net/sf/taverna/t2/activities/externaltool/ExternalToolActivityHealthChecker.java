/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhoeft, INB, University of Luebeck   
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

package net.sf.taverna.t2.activities.externaltool;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;

/**
 * Investigates if everything is going fine with a job
 * 
 * @author Hajo Nils Krabbenhoeft
 */
public class ExternalToolActivityHealthChecker implements HealthChecker<ExternalToolActivity> {

	public boolean canVisit(Object subject) {
		return subject != null && subject instanceof ExternalToolActivity;
	}

	public VisitReport visit(ExternalToolActivity activity, List<Object> ancestry) {
		ExternalToolActivityConfigurationBean configuration = activity.getConfiguration();
		List<VisitReport> reports = new ArrayList<VisitReport>();

		// currently a use case is doing fine if the repository is fine and
		// contains the needed use case
		reports.add(checkRepository(activity, configuration));
		reports.add(checkExternalTool(activity, configuration));
		reports.add(checkQueuePresent(activity, configuration));
		reports.add(checkCertificateNotExpired(activity, configuration));

		Status status = getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "Janitor Use Case Activity", HealthCheck.NO_PROBLEM, status, reports);

		return report;
	}

	private VisitReport checkRepository(ExternalToolActivity activity, ExternalToolActivityConfigurationBean configuration) {
		try {
			// try to parse the use case repository XML file
			UseCaseEnumeration.enumerateXmlInner(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()), configuration.getRepositoryUrl(),
					new ArrayList<UseCaseDescription>());
		} catch (Throwable e) {
			return new VisitReport(HealthCheck.getInstance(), activity, "Could not enumerate repository \"" + configuration.getRepositoryUrl()
					+ "\" due to error: " + e.toString(), HealthCheck.INVALID_URL, Status.SEVERE);
		}
		return new VisitReport(HealthCheck.getInstance(), activity, "Repository is fine: " + configuration.getRepositoryUrl(), HealthCheck.NO_PROBLEM,
				Status.OK);
	}

	private UseCaseDescription useCaseDescription;

	private VisitReport checkExternalTool(ExternalToolActivity activity, ExternalToolActivityConfigurationBean configuration) {
		useCaseDescription = null;
		// get a list of use cases from the repository XML file
		List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
				configuration.getRepositoryUrl());
		// search for the needed use case
		for (UseCaseDescription usecase : usecases) {
			if (!usecase.usecaseid.equalsIgnoreCase(configuration.getExternaltoolid()))
				continue;

			useCaseDescription = usecase;
			return new VisitReport(HealthCheck.getInstance(), activity, "Usecase " + configuration.getExternaltoolid() + " was found.", HealthCheck.NO_PROBLEM,
					Status.OK);
		}

		return new VisitReport(HealthCheck.getInstance(), activity, "Could not find usecase: " + configuration.getExternaltoolid(),
				HealthCheck.INVALID_CONFIGURATION, Status.SEVERE);
	}

	private VisitReport checkQueuePresent(ExternalToolActivity activity, ExternalToolActivityConfigurationBean configuration) {
		final ArrayList<String> compatibleQueues = KnowARCConfigurationFactory.getConfiguration().info.getCompatibleQueuesForREs(useCaseDescription.REs);

		final int queueCount = compatibleQueues.size();
		final boolean ok = queueCount > 0;
		return new VisitReport(HealthCheck.getInstance(), activity, "Found " + queueCount + " compatible queues.", ok ? HealthCheck.NO_PROBLEM
				: HealthCheck.NO_ENDPOINTS, ok ? Status.OK : Status.SEVERE);
	}

	private VisitReport checkCertificateNotExpired(ExternalToolActivity activity, ExternalToolActivityConfigurationBean configuration) {
		final boolean ok = KnowARCConfigurationFactory.getConfiguration().info.getCertificateData().getTimeLeft() > 10;
		return new VisitReport(HealthCheck.getInstance(), activity, "Certificate " + (ok ? "is fine." : "has expired."), ok ? HealthCheck.NO_PROBLEM
				: HealthCheck.INVALID_CONFIGURATION, ok ? Status.OK : Status.SEVERE);
	}

	private Status getWorstStatus(List<VisitReport> reports) {
		Status status = Status.OK;
		for (VisitReport report : reports) {
			if (report.getStatus().equals(Status.WARNING) && status.equals(Status.OK))
				status = report.getStatus();
			if (report.getStatus().equals(Status.SEVERE))
				status = Status.SEVERE;
		}
		return status;
	}

	public boolean isTimeConsuming() {
		return true;
	}

}
