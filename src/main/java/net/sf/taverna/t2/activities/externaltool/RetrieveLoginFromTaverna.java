package net.sf.taverna.t2.activities.externaltool;

import java.net.URI;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;

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
