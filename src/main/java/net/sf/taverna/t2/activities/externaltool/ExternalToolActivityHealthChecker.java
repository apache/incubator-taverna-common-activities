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

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * Investigates if everything is going fine with a job
 * 
 * @author Hajo Nils Krabbenhoeft
 */
public class ExternalToolActivityHealthChecker implements HealthChecker<ExternalToolActivity> {
	
	private static InvocationGroupManager manager = InvocationGroupManager.getInstance();
	private ExternalToolActivity activity;

	public boolean canVisit(Object subject) {
		return subject != null && subject instanceof ExternalToolActivity;
	}

	public VisitReport visit(ExternalToolActivity activity, List<Object> ancestry) {
		this.activity = activity;
		ExternalToolActivityConfigurationBean configuration = activity.getConfigurationNoConversion();
		List<VisitReport> reports = new ArrayList<VisitReport>();
		
		VisitReport locationReport = checkLocation(configuration);
		if (locationReport != null) {
			reports.add(locationReport);
		}

		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "External tool service", HealthCheck.NO_PROBLEM, reports);

		return report;
	}


	private VisitReport checkLocation(
			ExternalToolActivityConfigurationBean configuration) {
		
		if (!updateLocation(configuration)) {
			return new VisitReport(HealthCheck.getInstance(), activity, "Unmanaged invocation mechanism", HealthCheck.UNMANAGED_LOCATION, Status.WARNING);
		} else {
			return null;
		}
	}
	
	public static boolean updateLocation(ExternalToolActivityConfigurationBean configuration) {
		InvocationGroup invocationGroup = configuration.getInvocationGroup();
		if (invocationGroup != null) {
			String invocationGroupName = invocationGroup.getName();
			String mechanismXML = invocationGroup.getMechanismXML();
			for (InvocationGroup group : manager.getInvocationGroups()) {
				if (group.getName().equals(invocationGroupName) &&
						group.getMechanismXML().equals(mechanismXML)) {
					if (configuration.getInvocationGroup() != group) {
						configuration.setInvocationGroup(group);
					}
					return true;
				}
			}
			return false;
		} else {
			String mechanismXML = configuration.getMechanismXML();
			String mechanismName = configuration.getMechanismName();
			for (InvocationMechanism mechanism : manager.getMechanisms()) {
				if (mechanism.getName().equals(mechanismName) && (mechanism.getXML().equals(mechanismXML))) {
					if (configuration.getMechanism() != mechanism) {
						configuration.setMechanism(mechanism);
					}
					return true;
				}
			}
			return false;
		}
		
	}
	
	public boolean isTimeConsuming() {
		return false;
	}

}
