/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.externaltool.ssh;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Text;

import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNodeFactory;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;
import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;

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
