/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.activities.externaltool.ExternalToolInvocationConfigurationBean;
import net.sf.taverna.t2.activities.externaltool.RetrieveLoginFromTaverna;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public final class ExternalToolSshInvocationConfigurationBean extends
		ExternalToolInvocationConfigurationBean<SshUseCaseInvocation> {
	
	private transient int currentWorkerNode = 1;
	
	private List<SshNode> sshWorkerNodes = new ArrayList<SshNode>();
	
	private static Logger logger = Logger.getLogger(ExternalToolSshInvocationConfigurationBean.class);

	public List<SshNode> getSshWorkerNodes() {
		return sshWorkerNodes;
	}

	@Override
	public SshUseCaseInvocation getAppropriateInvocation(ExternalToolActivity a) {
		SshUseCaseInvocation result = null;
	
		try {
			SshNode node = getNextWorkerNode();
			result = new SshUseCaseInvocation(a.getConfiguration().getUseCaseDescription(), node, new RetrieveLoginFromTaverna(SshUseCaseInvocation.createSshUrl(node.getHost(), node.getPort(), node.getDirectory())));
		} catch (JSchException e) {
			logger.error("Null invocation", e);
		} catch (SftpException e) {
			logger.error("Null invocation", e);
		}
		return result;
	}

	private SshNode getNextWorkerNode() {
		if (sshWorkerNodes.isEmpty()) {
			return null;
		}
		currentWorkerNode %= sshWorkerNodes.size();
		return sshWorkerNodes.get(currentWorkerNode++);
	}
	
	public void setSshWorkerNodes(List<SshNode> workerNodes) {
		this.sshWorkerNodes = workerNodes;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ExternalToolSshInvocationConfigurationBean)) {
			return false;
		}
		ExternalToolSshInvocationConfigurationBean bean = (ExternalToolSshInvocationConfigurationBean) o;
		if (bean.getSshWorkerNodes().size() != this.getSshWorkerNodes().size()) {
			return false;
		}
		return bean.getSshWorkerNodes().containsAll(this.getSshWorkerNodes());
		
	}
}
