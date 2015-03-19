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
import java.util.HashSet;
import java.util.List;

import org.apache.taverna.activities.externaltool.manager.InvocationGroup;
import org.apache.taverna.activities.externaltool.manager.InvocationGroupManager;
import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ExternalToolActivityHealthChecker implements HealthChecker<ExternalToolActivity> {

	private InvocationGroupManager invocationGroupManager;
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

	public boolean updateLocation(ExternalToolActivityConfigurationBean configuration) {
		InvocationGroup invocationGroup = configuration.getInvocationGroup();
		String invocationGroupSpecification = null;
		String invocationMechanismSpecification = null;
		if (invocationGroup != null) {
			if (invocationGroupManager.containsGroup(invocationGroup)) {
				return true;
			}
			InvocationGroup replacementGroup = invocationGroupManager.getGroupReplacement(invocationGroup);
			if (replacementGroup != null) {
				configuration.setInvocationGroup(replacementGroup);
				return true;
			}
			invocationGroupSpecification = invocationGroup.getName() + ":" + invocationGroup.getMechanismXML();
			InvocationGroup importedGroup = invocationGroupManager.getImportedGroup(invocationGroupSpecification);
			if (importedGroup != null) {
				configuration.setInvocationGroup(importedGroup);
				return true;
			}
		}

		InvocationMechanism invocationMechanism = configuration.getMechanism();
		if (invocationMechanism != null) {
			if (invocationGroupManager.containsMechanism(invocationMechanism)) {
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
		for (InvocationMechanism mechanism : invocationGroupManager.getMechanisms()) {
			mechanismNames.add(mechanism.getName());
			if (mechanism.getName().equals(mechanismName) && (mechanism.getXML().equals(mechanismXML))) {
				if (invocationMechanism != mechanism) {
					foundMechanism = mechanism;
				}
			}
		}

		if (foundMechanism == null) {
			foundMechanism = invocationGroupManager.getMechanismReplacement(invocationMechanismSpecification);
			if (foundMechanism == null) {
				foundMechanism = invocationGroupManager.getImportedMechanism(invocationMechanismSpecification);
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
			invocationGroupManager.importMechanism(invocationMechanismSpecification, createdMechanism);


			if (invocationGroup == null) {
				return true;
			}
		}

		InvocationGroup foundGroup = null;
		HashSet<String> groupNames = new HashSet<String>();
		for (InvocationGroup group : invocationGroupManager.getInvocationGroups()) {
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
		invocationGroupManager.importInvocationGroup(invocationGroupSpecification, invocationGroup);
		return true;
	}

	public boolean isTimeConsuming() {
		return false;
	}

	/**
	 * Sets the invocationGroupManager.
	 *
	 * @param invocationGroupManager the new value of invocationGroupManager
	 */
	public void setInvocationGroupManager(InvocationGroupManager invocationGroupManager) {
		this.invocationGroupManager = invocationGroupManager;
	}

}
