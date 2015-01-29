/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.ssh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNodeFactory;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;

/**
 * @author alanrw
 *
 */
public class ExternalToolSshInvocationMechanism extends InvocationMechanism {
	
	private List<SshNode> nodes = new ArrayList<SshNode>();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism#getType()
	 */
	@Override
	public String getType() {
		return SshUseCaseInvocation.SSH_USE_CASE_INVOCATION_TYPE;
	}

	/**
	 * @param list the nodes to set
	 */
	public void setNodes(List<SshNode> list) {
		this.nodes = list;
	}

	public List<SshNode> getNodes() {
		if (this.nodes.isEmpty()) {
			this.nodes.add(SshNodeFactory.getInstance().getDefaultNode());
		}
		return this.nodes;
	}

	@Override
	public Element getXMLElement() {
		Element top = new Element("sshInvocation");
		for (SshNode node : nodes) {
			Element nodeElement = new Element("sshNode");
			String host = node.getHost();
			if (host != null) {
				Element hostElement = new Element("host");
				hostElement.addContent(new Text(host));
				nodeElement.addContent(hostElement);
			}
			int port = node.getPort();
			Element portElement = new Element("port");
			portElement.addContent(new Text(Integer.toString(port)));
			nodeElement.addContent(portElement);

			String directory = node.getDirectory();
			if (directory != null) {
				Element directoryElement = new Element("directory");
				directoryElement.addContent(new Text(directory));
				nodeElement.addContent(directoryElement);
			}
			
			String linkCommand = node.getLinkCommand();
			if (linkCommand != null) {
				Element linkCommandElement = new Element("linkCommand");
				linkCommandElement.addContent(new Text(linkCommand));
				nodeElement.addContent(linkCommandElement);
			}
			
			String copyCommand = node.getCopyCommand();
			if (copyCommand != null) {
				Element copyCommandElement = new Element("copyCommand");
				copyCommandElement.addContent(new Text(copyCommand));
				nodeElement.addContent(copyCommandElement);
			}
			if (node.isRetrieveData()) {
				Element retrieveDataElement = new Element("retrieveData");
				nodeElement.addContent(retrieveDataElement);
			}
			
			top.addContent(nodeElement);
		}
		return top;
	}

}
