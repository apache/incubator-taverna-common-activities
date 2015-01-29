/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import java.io.File;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister;
import net.sf.taverna.t2.activities.externaltool.ssh.SshInvocationPersister;

/**
 * @author alanrw
 *
 */
public class LocalInvocationPersister extends InvocationPersister {
	
	private static Logger logger = Logger.getLogger(LocalInvocationPersister.class);

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister#load()
	 */
	@Override
	public void load(File directory) {
		LocalUseCaseInvocation.load(directory);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.InvocationPersister#persist()
	 */
	@Override
	public void persist(File directory) {
		LocalUseCaseInvocation.persist(directory);
	}
	
	@Override
	public void deleteRun(String runId) {
			LocalUseCaseInvocation.cleanup(runId);
	}

}
