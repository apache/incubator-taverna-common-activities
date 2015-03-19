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

package org.apache.taverna.activities.externaltool.local;

import org.jdom.Element;

import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;
import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import org.apache.taverna.activities.externaltool.manager.MechanismCreator;

/**
 * @author alanrw
 *
 */
public class LocalMechanismCreator extends MechanismCreator {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE);
	}

	public InvocationMechanism convert(Element detailsElement,
			String mechanismName) {
		ExternalToolLocalInvocationMechanism result = new ExternalToolLocalInvocationMechanism();
		result.setName(mechanismName);
		Element directoryElement = detailsElement.getChild("directory");
		if (directoryElement != null) {
			result.setDirectory(directoryElement.getText());
		}
		Element shellPrefixElement = detailsElement.getChild("shellPrefix");
		if (shellPrefixElement != null) {
			result.setShellPrefix(shellPrefixElement.getText());
		}
		Element linkCommandElement = detailsElement.getChild("linkCommand");
		if (linkCommandElement != null) {
			result.setLinkCommand(linkCommandElement.getText());
		}
		Element retrieveDataElement = detailsElement.getChild("retrieveData");
		result.setRetrieveData(retrieveDataElement != null);

		return result;
	}

}
