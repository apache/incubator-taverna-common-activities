/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.io.File;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public class SshInvocationPersister extends InvocationPersister {
	
	private static Logger logger = Logger.getLogger(SshInvocationPersister.class);



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister#load()
	 */
	@Override
	public void load(File directory) {
		SshUseCaseInvocation.load(directory);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister#persist()
	 */
	@Override
	public void persist(File directory) {
		SshUseCaseInvocation.persist(directory);
	}

	@Override
	public void deleteRun(String runId) {
		try {
			SshUseCaseInvocation.cleanup(runId);
		} catch (InvocationException e) {
			logger.error(e);
		}
	}

}
