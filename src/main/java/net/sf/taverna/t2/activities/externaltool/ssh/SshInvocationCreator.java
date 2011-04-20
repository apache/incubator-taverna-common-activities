/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.externaltool.InvocationCreator;
import net.sf.taverna.t2.activities.externaltool.RetrieveLoginFromTaverna;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNodeFactory;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUrl;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

/**
 * @author alanrw
 *
 */
public final class SshInvocationCreator implements InvocationCreator {
	
	private static Logger logger = Logger.getLogger(SshInvocationCreator.class);

    private static List<SshNode> knownNodes = new ArrayList<SshNode>();

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(SshUseCaseInvocation.SSH_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(InvocationMechanism m, UseCaseDescription description) {
	    ExternalToolSshInvocationMechanism mechanism = (ExternalToolSshInvocationMechanism) m;
		SshUseCaseInvocation result = null;
		try {
		    SshNode chosenNode = chooseNode(mechanism.getNodes());
		    result = new SshUseCaseInvocation(description, chosenNode, new RetrieveLoginFromTaverna(new SshUrl(chosenNode).toString()));
		} catch (JSchException e) {
			logger.error("Null invocation", e);
		} catch (SftpException e) {
			logger.error("Null invocation", e);
		}
		return result;
	}

    private static SshNode chooseNode(List<SshNode> possibleNodes) {
	SshNode result = null;
	synchronized(knownNodes) {
	    int chosenIndex = Integer.MAX_VALUE;
	    for (SshNode p : possibleNodes) {
		if (!knownNodes.contains(p)) {
		    knownNodes.add(p);
		}
		int index = knownNodes.indexOf(p);
		if (index < chosenIndex) {
		    chosenIndex = index;
		}
	    }
	    if (chosenIndex != Integer.MAX_VALUE) {
		result = knownNodes.get(chosenIndex);
		// Move node to end of list
		knownNodes.remove(result);
		knownNodes.add(result);
	    }
	}
	return result;
    }
}
