/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
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