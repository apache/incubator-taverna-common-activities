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

import java.net.URL;
import java.util.LinkedHashSet;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity.ClassLoaderSharing;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * Parent configuration bean for activities that have local JAR or artifact dependencies,
 * such as API Consumer and Beanshell activity.
 * 
 * @author Alex Nenadic
 *
 */
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
	 * Artifact dependencies. These artifacts should be available from
	 * the centrally known repositories or from one of the listed repositories in
	 * {@link #repositories}.
	 */
	private LinkedHashSet<BasicArtifact> artifactDependencies ;//= new LinkedHashSet<BasicArtifact>();
	
	/**
	 * Repositoryies to use when searching for artifacts. In addition, the system
	 * repositories will be searched.
	 */
	@Deprecated
	private LinkedHashSet<URL> repositories ;//= new LinkedHashSet<URL>();
		
	/**
	 * Constructor.
	 */
	public DependencyActivityConfigurationBean(){
		classLoaderSharing = ClassLoaderSharing.workflow;
		localDependencies = new LinkedHashSet<String>();
		artifactDependencies = new LinkedHashSet<BasicArtifact>();
	}
	
	/**
	 * As XStream is not calling the default constructor during deserialization,
	 * we have to set the default values here. This method will be called by XStream
	 * after instantiating this bean.
	 */
	private Object readResolve(){
		if (classLoaderSharing == null)
			classLoaderSharing = ClassLoaderSharing.workflow;

		if (localDependencies == null) 
			localDependencies = new LinkedHashSet<String>();

		if (artifactDependencies == null)
			artifactDependencies = new LinkedHashSet<BasicArtifact>();

			return this;
	}
	
	/**
	 * @param classLoaderSharing the classLoaderSharing to set
	 */
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
	
	/**
	 * @return the artifactDependencies
	 */
	public LinkedHashSet<BasicArtifact> getArtifactDependencies() {
		return artifactDependencies;
	}
	
	/**
	 * @param artifactDependencies the artifactDependencies to set
	 */
	public void setArtifactDependencies(LinkedHashSet<BasicArtifact> artifactDependencies) {
		this.artifactDependencies = artifactDependencies;
	}
	
	/**
	 * Adds an artifact dependency to the list of local dependencies.
	 * @param dep
	 */
	public void addArtifactDependency(BasicArtifact dep){
		artifactDependencies.add(dep);
	}

	/**
	 * Removes an artifact dependency from the list of local dependencies.
	 * @param dep
	 */
	public void removeArtifacteDependency(BasicArtifact dep){
		artifactDependencies.remove(dep);
	}
	
}

