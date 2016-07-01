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

package org.apache.taverna.activities.externaltool;

import java.net.URI;

import org.apache.taverna.activities.externaltool.invocation.AskUserForPw;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.UsernamePassword;

public class RetrieveLoginFromTaverna implements AskUserForPw {
	private final String url;
	private final CredentialManager credentialManager;

	public RetrieveLoginFromTaverna(String url, CredentialManager credentialManager) {
		this.url = url;
		this.credentialManager = credentialManager;
	}

	private UsernamePassword getUserPass() {
		try {
			final String urlStr = url;
			URI userpassUrl = URI.create(urlStr.replace("//", "/"));
			final UsernamePassword userAndPass = credentialManager.getUsernameAndPasswordForService(userpassUrl, false, null);
			return userAndPass;
		} catch (CMException e) {
			throw new RuntimeException("Error in Taverna Credential Manager", e);
		}
	}

	public String getUsername() throws RuntimeException {
		final UsernamePassword userPass = getUserPass();
		if (userPass == null) {
			throw new RuntimeException("Unable to obtain valid username and password");
		}
		userPass.resetPassword();
		return userPass.getUsername();
	}

	public String getPassword() {
		final UsernamePassword userPass = getUserPass();
		final String pw = userPass.getPasswordAsString();
		userPass.resetPassword();
		return pw;
	}


	public String getKeyfile() {
		return "";
	}

	public String getPassphrase() {
		return "";
	}

	public void authenticationSucceeded() {
	}
}
