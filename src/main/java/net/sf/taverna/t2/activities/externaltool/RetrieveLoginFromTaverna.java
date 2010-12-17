package net.sf.taverna.t2.activities.externaltool;

import java.net.URI;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;

public class RetrieveLoginFromTaverna implements AskUserForPw {
	private CredentialManager credentialManager = null;

	public RetrieveLoginFromTaverna() {
		try {
			credentialManager = CredentialManager.getInstance();
		} catch (CMException e) {
			throw new RuntimeException("Error in Taverna Credential Manager", e);
		}
	}

	private transient SshNode workerNode = null;

	public void setSshNode(SshNode sshNode) {
		this.workerNode = sshNode;
	}

	private UsernamePassword getUserPass() {
		try {
			final String urlStr = "ssh://" + workerNode.host + "/" + workerNode.directory;
			URI userpassUrl = URI.create(urlStr.replace("//", "/"));
			final UsernamePassword userAndPass = credentialManager.getUsernameAndPasswordForService(userpassUrl, false, null);
			return userAndPass;
		} catch (CMException e) {
			throw new RuntimeException("Error in Taverna Credential Manager", e);
		}
	}

	public String getUsername() {
		final UsernamePassword userPass = getUserPass();
		userPass.resetPassword();
		return userPass.getUsername();
	}

	public String getPassword() {
		final UsernamePassword userPass = getUserPass();
		final String pw = userPass.getPasswordAsString();
		userPass.resetPassword();
		return pw;
	}

	// Keyfile and passphrase authentication is disabled until i find out how to
	// make that work with Taverna CredentialManager.

	// private UsernamePassword getKeyfilePassphrase() {
	// try {
	// URI keyfphraUrl = URI.create("ssh://" + workerNode.host + "/" +
	// workerNode.directory + "/keyfile");
	// final UsernamePassword keyfileAndItsPassphrase =
	// credentialManager.getUsernameAndPasswordForService(keyfphraUrl, false,
	// null);
	// return keyfileAndItsPassphrase;
	// } catch (CMException e) {
	// throw new RuntimeException("Error in Taverna Credential Manager", e);
	// }
	// }
	//
	// public String getKeyfile() {
	// final UsernamePassword userPass = getKeyfilePassphrase();
	// userPass.resetPassword();
	// final String keyfile = userPass.getUsername();
	// if (keyfile.startsWith("/"))
	// return keyfile;
	// if (keyfile.startsWith("~"))
	// return keyfile;
	// return "";
	// }
	//
	// public String getPassphrase() {
	// final UsernamePassword userPass = getKeyfilePassphrase();
	// final String pw = userPass.getPasswordAsString();
	// userPass.resetPassword();
	// return pw;
	// }

	public String getKeyfile() {
		return "";
	}

	public String getPassphrase() {
		return "";
	}

	public void authenticationSucceeded() {
	}
}
