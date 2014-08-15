/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import net.sf.taverna.t2.workflowmodel.RunDeletionListener;

/**
 * @author alanrw
 * 
 */
// Registered via SPI
public class InteractionRunDeletionListener implements RunDeletionListener {
	@Override
	public void deleteRun(String runToDelete) {
		InteractionRecorder.deleteRun(runToDelete);
	}
}
