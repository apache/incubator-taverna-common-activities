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
package net.sf.taverna.t2.activities.dependencyactivity;

import java.util.LinkedHashSet;

import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity.ClassLoaderSharing;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * Parent configuration bean for activities that have local JAR,
 * such as API Consumer and Beanshell activity.
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
@ConfigurationBean(uri = "http://ns.taverna.org.uk/2010/activity/dependency#Config")
public class DependencyActivityConfigurationBean extends
		ActivityPortsDefinitionBean {

	/**
	 * Activity's classloader sharing policy.
	 */
	private ClassLoaderSharing classLoaderSharing ;// = ClassLoaderSharing.workflow;
	
	/**
	 * Local dependencies, i.e. filenames of JARs the activity depends on. 
	 * The files should be present in {@link AbstractAsynchronousActivityWithDependencies#libDir}, 
	 * and the paths should be relative.
	 */
	private LinkedHashSet<String> localDependencies ;//= new LinkedHashSet<String>();
			
	/**
	 * Constructor.
	 */
	public DependencyActivityConfigurationBean(){
		classLoaderSharing = ClassLoaderSharing.DEFAULT;
		localDependencies = new LinkedHashSet<String>();
	}
	
	/**
	 * As XStream is not calling the default constructor during deserialization,
	 * we have to set the default values here. This method will be called by XStream
	 * after instantiating this bean.
	 */
	private Object readResolve(){
		if (classLoaderSharing == null)
			classLoaderSharing = ClassLoaderSharing.DEFAULT;

		if (localDependencies == null) 
			localDependencies = new LinkedHashSet<String>();

			return this;
	}
	
	/**
	 * @param classLoaderSharing the classLoaderSharing to set
	 */
	@ConfigurationProperty(name = "classLoaderSharing", label = "ClassLoader Sharing Policy", required = false)
	public void setClassLoaderSharing(ClassLoaderSharing classLoaderSharing) {
		this.classLoaderSharing = classLoaderSharing;
	}

	/**
	 * @return the classLoaderSharing
	 */
	public ClassLoaderSharing getClassLoaderSharing() {
		return classLoaderSharing;
	}

	/**
	 * @param localDependencies the localDependencies to set
	 */
	@ConfigurationProperty(name = "localDependency", label = "Local Dependencies", required = false)
	public void setLocalDependencies(LinkedHashSet<String> localDependencies) {
		this.localDependencies = localDependencies;
	}

	/**
	 * @return the localDependencies
	 */
	public LinkedHashSet<String> getLocalDependencies() {
		return localDependencies;
	}
	
	/**
	 * Adds a dependency to the list of local dependencies.
	 * @param dep
	 */
	public void addLocalDependency(String dep){
		localDependencies.add(dep);
	}

	/**
	 * Removes a dependency from the list of local dependencies.
	 * @param dep
	 */
	public void removeLocalDependency(String dep){
		localDependencies.remove(dep);
	}
		
}

