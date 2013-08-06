/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import net.sf.taverna.t2.workflowmodel.RunDeletionListener;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class InteractionRunDeletionListener implements RunDeletionListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(InteractionRunDeletionListener.class);

	@Override
	public void deleteRun(final String runToDelete) {
		InteractionRecorder.deleteRun(runToDelete);
	}

}
