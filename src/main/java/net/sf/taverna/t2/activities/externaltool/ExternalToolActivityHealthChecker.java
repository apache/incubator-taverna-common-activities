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
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

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
		String invocationGroupSpecification = null;
		String invocationMechanismSpecification = null;
		if (invocationGroup != null) {
			if (manager.containsGroup(invocationGroup)) {
				return true;
			}
			InvocationGroup replacementGroup = manager.getGroupReplacement(invocationGroup);
			if (replacementGroup != null) {
				configuration.setInvocationGroup(replacementGroup);
				return true;
			}
			invocationGroupSpecification = invocationGroup.getName() + ":" + invocationGroup.getMechanismXML();
			InvocationGroup importedGroup = manager.getImportedGroup(invocationGroupSpecification);
			if (importedGroup != null) {
				configuration.setInvocationGroup(importedGroup);
				return true;
			}
		}
		
		InvocationMechanism invocationMechanism = configuration.getMechanism();
		if (invocationMechanism != null) {
			if (manager.containsMechanism(invocationMechanism)) {
				return true;
			}
		}
		String mechanismXML = null;
		String mechanismName = null;

		if (invocationGroup != null) {
			mechanismXML = invocationGroup.getMechanismXML();
			mechanismName = invocationGroup.getMechanismName();
		} else {
			mechanismXML = configuration.getMechanismXML();
			mechanismName = configuration.getMechanismName();
		}
		invocationMechanismSpecification = mechanismName + ":" + mechanismXML;
		
		InvocationMechanism foundMechanism = null;
		HashSet<String> mechanismNames = new HashSet<String>();
		for (InvocationMechanism mechanism : manager.getMechanisms()) {
			mechanismNames.add(mechanism.getName());
			if (mechanism.getName().equals(mechanismName) && (mechanism.getXML().equals(mechanismXML))) {
				if (invocationMechanism != mechanism) {
					foundMechanism = mechanism;
				}
			}
		}
		
		if (foundMechanism == null) {
			foundMechanism = manager.getMechanismReplacement(invocationMechanismSpecification);
			if (foundMechanism == null) {
				foundMechanism = manager.getImportedMechanism(invocationMechanismSpecification);
			}
		}

		if (foundMechanism != null) {
			if (invocationGroup != null) {
				invocationGroup.setMechanism(foundMechanism);
				// Cannot return because invocationGroup is still unknown
			} else {
				configuration.setMechanism(foundMechanism);
				return true;
			}
		}
		
		if (foundMechanism == null) {
			InvocationMechanism createdMechanism;
			if (invocationGroup != null) {
				invocationGroup.convertDetailsToMechanism();
				createdMechanism = invocationGroup.getMechanism();
			} else {
				configuration.convertDetailsToMechanism();
				createdMechanism = configuration.getMechanism();
			}

			String chosenMechanismName = Tools.uniqueObjectName(mechanismName,
					mechanismNames);
			createdMechanism.setName(chosenMechanismName);
			if (invocationGroup != null) {
				invocationGroup.setMechanism(createdMechanism);
			} else {
				configuration.setMechanism(createdMechanism);
			}
			manager.importMechanism(invocationMechanismSpecification, createdMechanism);
			

			if (invocationGroup == null) {
				return true;
			}
		}
		
		InvocationGroup foundGroup = null;
		HashSet<String> groupNames = new HashSet<String>();
		for (InvocationGroup group : manager.getInvocationGroups()) {
			groupNames.add(group.getName());
			if (group.getName().equals(invocationGroup.getName()) && (group.getMechanism() == invocationGroup.getMechanism())) {
				foundGroup = group;
			}
		}
		if (foundGroup != null) {
			configuration.setInvocationGroup(foundGroup);
			return true;
		}
		invocationGroup.setName(Tools.uniqueObjectName(invocationGroup.getName(), groupNames));
		manager.importInvocationGroup(invocationGroupSpecification, invocationGroup);
		return true;
	}
	
	public boolean isTimeConsuming() {
		return false;
	}

}
