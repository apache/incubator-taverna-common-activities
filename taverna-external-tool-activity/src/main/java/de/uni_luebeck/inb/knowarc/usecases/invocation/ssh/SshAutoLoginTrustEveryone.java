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

package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import org.apache.log4j.Logger;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;

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