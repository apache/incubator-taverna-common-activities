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
import java.util.Map;

import org.apache.taverna.activities.externaltool.InvocationCreator;
import org.apache.taverna.activities.externaltool.RetrieveLoginFromTaverna;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNodeFactory;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshReference;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUrl;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import org.apache.taverna.security.credentialmanager.CredentialManager;

/**
 * @author alanrw
 *
 */
public final class SshInvocationCreator implements InvocationCreator {

	private static Logger logger = Logger.getLogger(SshInvocationCreator.class);

    private static List<SshNode> knownNodes = new ArrayList<SshNode>();

	private CredentialManager credentialManager;

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(SshUseCaseInvocation.SSH_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(InvocationMechanism m, UseCaseDescription description, Map<String, T2Reference> data, ReferenceService referenceService) {
	    ExternalToolSshInvocationMechanism mechanism = (ExternalToolSshInvocationMechanism) m;
		SshUseCaseInvocation result = null;
		try {
		    SshNode chosenNode = chooseNode(mechanism.getNodes(), data, referenceService);
		    result = new SshUseCaseInvocation(description, chosenNode, new RetrieveLoginFromTaverna(new SshUrl(chosenNode).toString(), credentialManager), credentialManager);
		} catch (JSchException e) {
			logger.error("Null invocation", e);
		} catch (SftpException e) {
			logger.error("Null invocation", e);
		}
		return result;
	}

    private static SshNode chooseNode(List<SshNode> possibleNodes, Map<String, T2Reference> data, ReferenceService referenceService) {
	SshNode result = null;
	for (T2Reference ref : data.values()) {
		SshReference r = getAsSshReference(referenceService, ref);
		if (r != null) {
			SshNode dataNode = SshNodeFactory.getInstance().getSshNode(r.getHost(), r.getPort(), r.getDirectory());
			if (possibleNodes.contains(dataNode)) {
				logger.info("Running with data at " + r.getHost());
				return dataNode;
			}
		}
	}
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

    private static SshReference getAsSshReference(ReferenceService referenceService,
			T2Reference t2Reference) {
    	Identified identified = referenceService.resolveIdentifier(t2Reference, null, null);
		if (identified instanceof ReferenceSet) {
			for (ExternalReferenceSPI ref : ((ReferenceSet) identified).getExternalReferences()) {
				if (ref instanceof SshReference) {
					SshReference sshRef = (SshReference) ref;
					return sshRef;
				}
			}
		}
		return null;
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

}
