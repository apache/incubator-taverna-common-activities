/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

import net.sf.taverna.t2.workflowmodel.RunDeletionListener;

/**
 * @author alanrw
 *
 */
public class ExternalToolRunDeletionListener implements RunDeletionListener {
	
	private static Logger logger = Logger.getLogger(ExternalToolRunDeletionListener.class);

	private static Map<String, Set<UseCaseInvocation>> runToInvocationsMap = Collections.synchronizedMap(new HashMap<String, Set<UseCaseInvocation>>());

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.RunDeletionListener#deleteRun(java.lang.String)
	 */
	@Override
	public void deleteRun(String runToDelete) {
		Set<UseCaseInvocation> invocations = runToInvocationsMap.get(runToDelete);
		runToInvocationsMap.remove(runToDelete);
		for (UseCaseInvocation uci : invocations) {
			try {
				uci.cleanup();
			}
			catch (InvocationException e) {
				logger.error(e);
			}
		}
	}
	
	public static void rememberInvocation(String runId, UseCaseInvocation invocation) {
		Set<UseCaseInvocation> invocations = runToInvocationsMap.get(runId);
		if (invocations == null) {
			invocations = Collections.synchronizedSet(new HashSet<UseCaseInvocation>());
			runToInvocationsMap.put(runId, invocations);
		}
		invocations.add(invocation);
	}

}
