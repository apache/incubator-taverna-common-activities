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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.prelauncher.BootstrapClassLoader;
import net.sf.taverna.raven.prelauncher.PreLauncher;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * A parent abstract class for activities that require dependency management, such as
 * API Consumer and Beanshell. Defines dependencies on local JAR files 
 * and Raven artifacts.
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * 
 * @param <ConfigType> the configuration type used for this activity
 *
 */
public abstract class AbstractAsynchronousDependencyActivity<ConfigType> extends AbstractAsynchronousActivity<ConfigType>{
	
	private static final String LOCAL_JARS = "Local jars";

	private static final String ARTIFACTS = "Artifacts";

	private static Logger logger = Logger.getLogger(AbstractAsynchronousDependencyActivity.class);

	/**
	 * For persisting class loaders across a whole workflow run (when classloader sharing
	 * is set to 'workflow'). The key in the map is the workflow run ID and we are using 
	 * a WeakHashMap so we don't keep up references to classloaders of old workflow runs.
	 */
	private static WeakHashMap<String, ClassLoader> workflowClassLoaders =
		new WeakHashMap<String, ClassLoader>();
	
	/**
	 * System classloader, in case when classloader sharing is set to 'system'.
	 */
	private static ClassLoader systemClassLoader = null;

	/**
	 * Classloader to be used for 'executing' this activity, depending on the activity's
	 * class loader sharing policy.
	 */
	protected ClassLoader classLoader = null;
	
	/**
	 * The location of the <code>lib</code> directory in TAVERNA_HOME,
	 * where local JAR files the activity depends on should be located.
	 */
	public static File libDir = new File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "lib");

	/**
	 * Different ways to share a class loader among activities:
	 * 
	 * <dl>
	 * <dt>workflow</dt>
	 * <dd>Same classloader for all activities using the <code>workflow</code> classloader sharing policy</dd>
	 * <dt>system</dt>
	 * <dd>System classloader</dd>
	 * </dl>
	 * 
	 */
	public static enum ClassLoaderSharing {
		workflow, system
	}
	
	/**
	 * Finds or constructs the classloader. The classloader depends on the
	 * current classloader sharing policy as defined by {@link #ClassLoaderSharing}.
	 * <p>
	 * If the classloader sharing is {@link ClassLoaderSharing#workflow}, a
	 * common classloader will be used for the whole workflow for all activities
	 * with the same (i.e. {@link ClassLoaderSharing#workflow}) policy. 
	 * The dependencies will be constructed as union of local and artifact dependencies 
	 * of all 'workflow' classloader sharing activities at the point of the first 
	 * call to {@link #getClassLoader()}.
 	 * <p>
	 * If the classloader sharing is {@link ClassLoaderSharing#system}, the
	 * system classloader will be used. Note that both local and artifact dependencies 
	 * configured on the activity will be ignored. Local dependencies can be set by 
	 * using <code>-classpath</code> when starting the workbench.
	 * This is useful in combination with JNI based libraries, which would also
	 * require <code>-Djava.library.path</code> and possibly the operating
	 * system's PATH / LD_LIBRARY_PATH / DYLD_LIBRARY_PATH environment variable.
	 * @param classLoaderSharing 
	 * 
	 * @return A new or existing {@link ClassLoader} according to the
	 *         classloader sharing policy
	 */
	protected ClassLoader findClassLoader(DependencyActivityConfigurationBean configurationBean, String workflowRunID) throws RuntimeException{
		
		ClassLoaderSharing classLoaderSharing = configurationBean.getClassLoaderSharing();
		
		if (classLoaderSharing == ClassLoaderSharing.workflow) {
			synchronized (workflowClassLoaders) {
				ClassLoader cl = workflowClassLoaders.get(workflowRunID);
				if (cl == null) {
					cl = makeClassLoader(configurationBean, workflowRunID);
					workflowClassLoaders.put(workflowRunID, cl);
				}
				return cl;
			}
		}
		if (classLoaderSharing == ClassLoaderSharing.system) {
			if (systemClassLoader == null)
				systemClassLoader = PreLauncher.getInstance().getLaunchingClassLoader();
			
			if (systemClassLoader instanceof BootstrapClassLoader){
				// Add local and artifact dependencies to the classloader
				updateBootstrapClassLoader(
						(BootstrapClassLoader) systemClassLoader,
						configurationBean, workflowRunID);
				return systemClassLoader;
			}
			else{
				// Local dependencies will have to be set with the -classpath option
				// We cannot have artifact dependencies in this case
				String message = "System classloader is not Taverna's BootstrapClassLoader, so local dependencies " +
						"have to defined with -classpath. Artifact dependencies are ignored completely.";
				logger.warn(message);
				return systemClassLoader;
			}
		}
		String message = "Unknown classloader sharing policy named '"+ classLoaderSharing+ "' for " + this.getClass();
		logger.error(message);
		throw new RuntimeException(message);
	}

	/**
	 * Constructs a classloader capable of finding both local jar and artifact dependencies.
	 * Called when classloader sharing policy is set to 'workflow'.
	 * 
	 * @return A {@link ClassLoader} capable of accessing all the dependencies (both local jar and artifact)
	 */
	private ClassLoader makeClassLoader(
			DependencyActivityConfigurationBean configurationBean,
			String workflowID) {
		
		// Find all artifact dependencies
		HashSet<URL> urls = findDependencies(ARTIFACTS, configurationBean, workflowID);
		
		// Add all local jar dependencies
		urls.addAll(findDependencies(LOCAL_JARS, configurationBean, workflowID));
		
		// Create the classloader capable of loading both local jar and artifact dependencies
		ClassLoader parent = this.getClass().getClassLoader(); // this will be a LocalArtifactClassLoader
		
		return new URLClassLoader(urls.toArray(new URL[0]), parent) {
			
			// For finding native libraries that have to be stored in TAVERNA_HOME/lib
			@Override
			protected String findLibrary(String libname) {
				String filename = System.mapLibraryName(libname);
				File libraryFile = new File(libDir, filename);
				if (libraryFile.isFile()) {
					logger.info("Found library " + libname + ": " + libraryFile.getAbsolutePath());
					return libraryFile.getAbsolutePath();
				}
				return super.findLibrary(libname);
			}
		};
	}
	
	/**
	 * Adds local or artifact dependencies identified by {@link #findDependencies()} to the
	 * {@link BootstrapClassLoader} system classloader.
	 * Called when classloader sharing policy is set to 'system'.
	 * 
	 * @param loader The augmented BootstrapClassLoader system classloader
	 */
	private void updateBootstrapClassLoader(BootstrapClassLoader loader,
			DependencyActivityConfigurationBean configurationBean,
			String workflowRunID) {
		
		HashSet<URL> depsURLs = new HashSet<URL>();
		depsURLs.addAll(findDependencies(LOCAL_JARS, configurationBean, workflowRunID));
		depsURLs.addAll(findDependencies(ARTIFACTS, configurationBean, workflowRunID));

		Set<URL> exists = new HashSet<URL>(Arrays.asList(loader.getURLs()));
		for (URL url : depsURLs) {
			if (exists.contains(url)) {
				continue;
			}
			logger.info("Registering with system classloader: " + url);
			loader.addURL(url);
			exists.add(url);
		}		
	}

	/**
	 * Finds either local jar or artifact dependencies' URLs for the given classloader 
	 * sharing policy (passed inside configuration bean) and a workflowRunID (used to 
	 * retrieve the workflow) that will be added to this activity classloader's list of URLs.
	 */
	private HashSet<URL> findDependencies(String dependencyType,
			DependencyActivityConfigurationBean configurationBean,
			String workflowRunID) {

		ClassLoaderSharing classLoaderSharing = configurationBean.getClassLoaderSharing();
 		// Get the WorkflowInstanceFacade which contains the current workflow
		WeakReference<WorkflowInstanceFacade> wfFacadeRef = WorkflowInstanceFacade.workflowRunFacades.get(workflowRunID);
		WorkflowInstanceFacade wfFacade = null;		
		if (wfFacadeRef != null) {
			wfFacade = wfFacadeRef.get();
		}
		Dataflow wf = null;
		if (wfFacade != null) {
			wf = wfFacade.getDataflow();
		}

		// Files of dependencies for all activities in the workflow that share the classloading policy
		HashSet<File> dependencies = new HashSet<File>();
		// Urls of all dependencies
		HashSet<URL> dependenciesURLs = new HashSet<URL>();

		if (wf != null){
			// Merge in dependencies from all activities that have the same classloader-sharing
			// as this activity
			for (Processor proc : wf.getProcessors()) {
				// Nested workflow case
				if (Tools.containsNestedWorkflow(proc)){		
					// Get the nested workflow
					Dataflow nestedWorkflow = ((NestedDataflow) proc.getActivityList().get(0)).getNestedDataflow();
					dependenciesURLs.addAll(findNestedDependencies(dependencyType, configurationBean, nestedWorkflow));
				}
				else{ // Not nested - go through all of the processor's activities
					Activity<?> activity = proc.getActivityList().get(0);
					if (activity instanceof AbstractAsynchronousDependencyActivity){
						if (((DependencyActivityConfigurationBean) activity
								.getConfiguration()).getClassLoaderSharing() == classLoaderSharing) {
							if (dependencyType.equals(LOCAL_JARS)){
								// Collect the files of all found local dependencies
								for (String jar : ((DependencyActivityConfigurationBean)activity.getConfiguration()).getLocalDependencies()) {
									try {
										dependencies.add(new File(libDir, jar));
									} catch (Exception ex) {
										logger.warn("Invalid URL for " + jar, ex);
										continue;
									}
								}								
							} else if (dependencyType.equals(ARTIFACTS) && this.getClass().getClassLoader() instanceof LocalArtifactClassLoader){ 
								LocalArtifactClassLoader cl = (LocalArtifactClassLoader) this.getClass().getClassLoader(); // this class is always loaded with LocalArtifactClassLoader
								// Get the LocalReposotpry capable of finding artifact jar files
								LocalRepository rep  = (LocalRepository) cl.getRepository();
								for (BasicArtifact art : ((DependencyActivityConfigurationBean) activity
												.getConfiguration())
												.getArtifactDependencies()){
									dependencies.add(rep.jarFile(art));
								}
							}
						}
					}
				}
			}
		} else { // Just add dependencies for this activity since we can't get hold of the whole workflow
			if (dependencyType.equals(LOCAL_JARS)){
				for (String jar : configurationBean.getLocalDependencies()) {
					try {
						dependencies.add(new File(libDir, jar));
					} catch (Exception ex) {
						logger.warn("Invalid URL for " + jar, ex);
						continue;
					}
				}	
			}
			else if (dependencyType.equals(ARTIFACTS)){
				if (this.getClass().getClassLoader() instanceof LocalArtifactClassLoader){ // This should normally be the case
					LocalArtifactClassLoader cl = (LocalArtifactClassLoader)this.getClass().getClassLoader(); 
					LocalRepository rep  = (LocalRepository)cl.getRepository();
					if (rep != null){ 
						for (BasicArtifact art : configurationBean.getArtifactDependencies()){
							dependencies.add(rep.jarFile(art));
						}
					}
				}
				else{
					// Tests will not be loaded using the LocalArtifactClassLoader as athey are loaded 
					// outside Raven so there is nothing we can do about this - some tests
					// with dependencies will probably fail
				}
			}
		}
		
		// Collect the URLs of all found dependencies
		for (File file: dependencies){
			try{
				dependenciesURLs.add(file.toURI().toURL());
			}
			catch(Exception ex){
				logger.warn("Invalid URL for " + file.getAbsolutePath(), ex);
				continue;
			}
		}		
		return dependenciesURLs;		
	}

	/**
	 * Finds dependencies for a nested workflow. 
	 */
	private HashSet<URL> findNestedDependencies(String dependencyType,
			DependencyActivityConfigurationBean configurationBean,
			Dataflow nestedWorkflow) {
		
 		ClassLoaderSharing classLoaderSharing = configurationBean.getClassLoaderSharing();

		// Files of dependencies for all activities in the nested workflow that share the classloading policy
		HashSet<File> dependencies = new HashSet<File>();
		// Urls of all dependencies
		HashSet<URL> dependenciesURLs = new HashSet<URL>();
		
		for (Processor proc : nestedWorkflow.getProcessors()) {
			// Another nested workflow
			if (Tools.containsNestedWorkflow(proc)){		
				// Get the nested workflow
				Dataflow nestedNestedWorkflow = ((NestedDataflow) proc.getActivityList().get(0)).getNestedDataflow();
				dependenciesURLs.addAll(findNestedDependencies(dependencyType, configurationBean, nestedNestedWorkflow));
			}
			else{ // Not nested - go through all of the processor's activities
				Activity<?> activity = proc.getActivityList().get(0);
				if (activity instanceof AbstractAsynchronousDependencyActivity){		
					
					if (((DependencyActivityConfigurationBean) activity
							.getConfiguration()).getClassLoaderSharing() == classLoaderSharing) {
						
						if (dependencyType.equals(LOCAL_JARS)){
							// Collect the files of all found local dependencies
							for (String jar : ((DependencyActivityConfigurationBean)activity.getConfiguration()).getLocalDependencies()) {
								try {
									dependencies.add(new File(libDir, jar));
								} catch (Exception ex) {
									logger.warn("Invalid URL for " + jar, ex);
									continue;
								}
							}						}
						else if (dependencyType.equals(ARTIFACTS) && this.getClass().getClassLoader() instanceof LocalArtifactClassLoader){
							LocalArtifactClassLoader cl = (LocalArtifactClassLoader) this.getClass().getClassLoader(); // this class is always loaded with LocalArtifactClassLoader
							LocalRepository rep  = (LocalRepository) cl.getRepository();
							for (BasicArtifact art : ((DependencyActivityConfigurationBean) activity
											.getConfiguration())
											.getArtifactDependencies()){
								dependencies.add(rep.jarFile(art));
							}
						}						
					}
				}
			}
		}
		
		// Collect the URLs of all found dependencies
		for (File file: dependencies){
			try{
				dependenciesURLs.add(file.toURI().toURL());
			}
			catch(Exception ex){
				logger.warn("Invalid URL for " + file.getAbsolutePath(), ex);
				continue;
			}
		}	
		return dependenciesURLs;
	}
	
	/**
	 * File filter.
	 */
	public static class FileExtFilter implements FilenameFilter {

		String ext = null;

		public FileExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}
	
	/**
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}


