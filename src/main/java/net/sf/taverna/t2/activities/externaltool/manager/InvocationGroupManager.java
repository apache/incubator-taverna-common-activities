/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.activities.externaltool.manager;

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