/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taverna.activities.externaltool.ssh;

import org.apache.log4j.Logger;
import org.apache.taverna.activities.externaltool.invocation.AskUserForPw;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

final class SshAutoLoginTrustEveryone implements UserInfo, UIKeyboardInteractive {
	
	private static Logger logger = Logger.getLogger(SshAutoLoginTrustEveryone.class);

	private final AskUserForPw askUserForPw;

	public SshAutoLoginTrustEveryone(AskUserForPw askUserForPw) {
		super();
		this.askUserForPw = askUserForPw;
	}

	public void showMessage(String arg0) {
		logger.info(arg0);
	}

	public boolean promptYesNo(String arg0) {
		if (arg0.startsWith("The authenticity of host"))
			return true;
		return false;
	}

	public boolean promptPassword(String arg0) {
		return true;
	}

	public boolean promptPassphrase(String arg0) {
		return true;
	}

	public String getPassword() {
		return askUserForPw.getPassword();
	}

	public String getPassphrase() {
		return askUserForPw.getPassphrase();
	}

	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
		if (prompt.length >= 1 && prompt[0].toLowerCase().startsWith("password"))
			return new String[] { askUserForPw.getPassword() };
		return null;
	}
}