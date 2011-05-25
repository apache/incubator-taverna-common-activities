/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.workflowmodel.RunDeletionListener;

/**
 * @author alanrw
 *
 */
public class ExternalToolRunDeletionListener implements RunDeletionListener {
	
	private static Logger logger = Logger.getLogger(ExternalToolRunDeletionListener.class);

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.RunDeletionListener#deleteRun(java.lang.String)
	 */
	@Override
	public void deleteRun(String runToDelete) {
		InvocationGroupManager.getInstance().deleteRun(runToDelete);
	}

}
