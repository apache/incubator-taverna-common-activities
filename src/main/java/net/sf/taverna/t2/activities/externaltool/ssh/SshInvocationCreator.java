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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUrl;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public final class SshInvocationCreator implements InvocationCreator {
	
	private static SAXBuilder builder = new SAXBuilder();
	
	private static Logger logger = Logger.getLogger(SshInvocationCreator.class);

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(SshUseCaseInvocation.SSH_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(String xml, UseCaseDescription description) {
		List<SshNode> sshWorkerNodes = new ArrayList<SshNode>();
		
		Document document;
		try {
			document = builder.build(new StringReader(xml));
		} catch (JDOMException e1) {
			logger.error("Null invocation", e1);
			return null;
		} catch (IOException e1) {
			logger.error("Null invocation", e1);
			return null;
		}
		Element top = document.getRootElement();
		for (Object nodeObject : top.getChildren("sshNode")) {
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
			sshWorkerNodes.add(node);
		}
		SshNode chosenNode = sshWorkerNodes.get(0);
		SshUseCaseInvocation result = null;
		try {
			result = new SshUseCaseInvocation(description, chosenNode, new RetrieveLoginFromTaverna(new SshUrl(chosenNode).toString()));
		} catch (JSchException e) {
			logger.error("Null invocation", e);
		} catch (SftpException e) {
			logger.error("Null invocation", e);
		}
		return result;
	}

}
