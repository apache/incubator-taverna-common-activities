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

package org.apache.taverna.activities.externaltool.manager;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observer;

/**
 *
 *
 * @author David Withers
 */
public interface InvocationGroupManager {

	public void addInvocationGroup(InvocationGroup group);

	public void replaceInvocationGroup(InvocationGroup originalGroup,
			InvocationGroup replacementGroup);

	public void removeInvocationGroup(InvocationGroup group);

	public void replaceInvocationMechanism(InvocationMechanism originalMechanism,
			InvocationMechanism replacementMechanism);

	public void removeMechanism(InvocationMechanism mechanism);

	public HashSet<InvocationGroup> getInvocationGroups();

	public InvocationGroup getDefaultGroup();

	public Set<InvocationMechanism> getMechanisms();

	public void addMechanism(InvocationMechanism mechanism);

	public InvocationMechanism getDefaultMechanism();

	public boolean containsGroup(InvocationGroup group);

	public InvocationMechanism getInvocationMechanism(String defaultMechanismName);

	public void mechanismChanged(InvocationMechanism im);
	
	/**
	 * Get the directory where the invocation information will be/is saved to.
	 */
	public File getInvocationManagerDirectory();

	public void saveConfiguration();

	public void groupChanged(InvocationGroup group);

	public void addObserver(Observer<InvocationManagerEvent> observer);

	public List<Observer<InvocationManagerEvent>> getObservers();

	public void removeObserver(Observer<InvocationManagerEvent> observer);

	public void deleteRun(String runId);

	public void persistInvocations();

	public void loadInvocations();

	public boolean containsMechanism(InvocationMechanism invocationMechanism);

	public InvocationGroup getGroupReplacement(InvocationGroup group);

	public InvocationMechanism getMechanismReplacement(String invocationMechanismSpecification);

	public InvocationGroup getImportedGroup(String groupSpecification);

	public InvocationMechanism getImportedMechanism(String mechanismSpecification);

	public void importMechanism(String invocationMechanismSpecification,
			InvocationMechanism createdMechanism);

	public void importInvocationGroup(String invocationGroupSpecification,
			InvocationGroup invocationGroup);

}
