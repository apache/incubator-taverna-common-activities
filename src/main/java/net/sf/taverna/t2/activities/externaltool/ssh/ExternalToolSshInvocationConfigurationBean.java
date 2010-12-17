/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvokation;
import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.activities.externaltool.ExternalToolInvocationConfigurationBean;
import net.sf.taverna.t2.activities.externaltool.RetrieveLoginFromTaverna;

/**
 * @author alanrw
 *
 */
public final class ExternalToolSshInvocationConfigurationBean extends
		ExternalToolInvocationConfigurationBean<SshUseCaseInvokation> {
	
	private transient int currentWorkerNode = 1;
	
	private List<SshNode> sshWorkerNodes = new ArrayList<SshNode>();

	public List<SshNode> getSshWorkerNodes() {
		return sshWorkerNodes;
	}

	@Override
	public SshUseCaseInvokation getAppropriateInvocation(ExternalToolActivity a) {
		SshUseCaseInvokation result = null;
	
		try {
			result = new SshUseCaseInvokation(null, a.getConfiguration().getUseCaseDescription(), getNextWorkerNode(), new RetrieveLoginFromTaverna());
		} catch (JSchException e) {
			// TODO Auto-generated catch block
		} catch (SftpException e) {
			// TODO Auto-generated catch block
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
