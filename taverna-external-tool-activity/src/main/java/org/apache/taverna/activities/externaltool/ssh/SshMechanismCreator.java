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

import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import org.apache.taverna.activities.externaltool.manager.MechanismCreator;

import org.apache.log4j.Logger;
import org.jdom.Element;

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
			Element nodeElement = (Element) nodeObject;
			Element hostElement = nodeElement.getChild("host");
			String host;
			int port;
			String directory;

				host = hostElement.getText();

			Element portElement = nodeElement.getChild("port");
			port = Integer.parseInt(portElement.getText());
			
			Element directoryElement = nodeElement.getChild("directory");

				directory = directoryElement.getText();

				boolean newNode = !SshNodeFactory.getInstance().containsSshNode(host, port, directory);
			
			SshNode node = SshNodeFactory.getInstance().getSshNode(host, port, directory);
			
			if (newNode) {

			Element linkCommandElement = nodeElement.getChild("linkCommand");
			if (linkCommandElement != null) {
				node.setLinkCommand(linkCommandElement.getText());
			}

			Element copyCommandElement = nodeElement.getChild("copyCommand");
			if (copyCommandElement != null) {
				node.setCopyCommand(copyCommandElement.getText());
			}
			
			Element retrieveDataElement = nodeElement.getChild("retrieveData");
			node.setRetrieveData(retrieveDataElement != null);
			}
			nodeList.add(node);

		}
		result.setNodes(nodeList);		
		return result;
	}

}
