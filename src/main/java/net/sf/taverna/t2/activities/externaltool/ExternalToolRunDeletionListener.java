/**
 *
 */
package net.sf.taverna.t2.activities.externaltool;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.workflowmodel.RunDeletionListener;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ExternalToolRunDeletionListener implements RunDeletionListener {

	private static Logger logger = Logger.getLogger(ExternalToolRunDeletionListener.class);

	private InvocationGroupManager invocationGroupManager;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.RunDeletionListener#deleteRun(java.lang.String)
	 */
	@Override
	public void deleteRun(String runToDelete) {
		invocationGroupManager.deleteRun(runToDelete);
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
