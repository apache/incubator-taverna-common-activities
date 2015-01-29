/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.manager;

import java.io.File;

/**
 * @author alanrw
 *
 */
public abstract class InvocationPersister {
	
	public abstract void persist(File directory);
	
	public abstract void load(File directory);

	public abstract void deleteRun(String runId);

}
