/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.util.ArrayList;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator;

import org.apache.log4j.Logger;
import org.jdom.Element;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public class SshMechanismCreator extends MechanismCreator {


	private static Logger logger = Logger.getLogger(SshMechanismCreator.class);
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(SshUseCaseInvocation.SSH_USE_CASE_INVOCATION_TYPE);
	}


	@Override
	public InvocationMechanism convert(Element detailsElement,
			String mechanismName) {
		ExternalToolSshInvocationMechanism result = new ExternalToolSshInvocationMechanism();
		result.setName(mechanismName);
		ArrayList<SshNode> nodeList = new ArrayList<SshNode>();
		for (Object nodeObject : detailsElement.getChildren("sshNode")) {
			SshNode node = new SshNode();
			Element nodeElement = (Element) nodeObject;
			Element hostElement = nodeElement.getChild("host");
			if (hostElement != null) {
				node.setHost(hostElement.getText());
			}
			Element directoryElement = nodeElement.getChild("directory");
			if (directoryElement != null) {
				node.setDirectory(directoryElement.getText());
			}
			Element portElement = nodeElement.getChild("port");
			node.setPort(Integer.parseInt(portElement.getText()));
			nodeList.add(node);
		}
		result.setNodes(nodeList);		
		return result;
	}

}
