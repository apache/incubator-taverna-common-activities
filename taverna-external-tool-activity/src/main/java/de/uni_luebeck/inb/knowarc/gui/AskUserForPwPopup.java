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
package de.uni_luebeck.inb.knowarc.gui;

import javax.swing.JOptionPane;

import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;

public final class AskUserForPwPopup implements AskUserForPw {
	private SshNode ret;
	private String pw, pp, kf, us;

	public static String ask(String message, String title) {
		return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, null, null, "");
	}

	public static boolean askYN(String message, String title) {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	public String getPassword() {
		getUsername();
		if (pw != null)
			return pw;
		pw = ask("Please enter your password for " + us + " @ " + ret.getHost(), us + " @ " + ret.getHost());
		return pw;
	}

	public String getPassphrase() {
		getUsername();
		if (pp != null)
			return pp;
		pp = ask("Please enter your passphrase for " + kf + " used for " + us + " @ " + ret.getHost(), us + " @ " + ret.getHost());
		return pp;
	}

	public String getKeyfile() {
		getUsername();
		if (kf != null)
			return kf;
		kf = ask("Please enter the keyfile for " + us + " @ " + ret.getHost(), us + " @ " + ret.getHost());
		return kf;
	}

	public String getUsername() {
		if (us != null)
			return us;
		us = ask("Please enter the username for " + ret.getHost(), "Username for " + ret.getHost());
		return us;
	}

	public void setSshNode(SshNode sshNode) {
		this.ret = sshNode;
	}

	public void authenticationSucceeded() {
	}
}