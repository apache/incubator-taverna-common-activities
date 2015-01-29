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
	
	private InteractionRecorder interactionRecorder;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(InteractionRunDeletionListener.class);

	@Override
	public void deleteRun(final String runToDelete) {
		interactionRecorder.deleteRun(runToDelete);
	}

	public void setInteractionRecorder(InteractionRecorder interactionRecorder) {
		this.interactionRecorder = interactionRecorder;
	}

}
