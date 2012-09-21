/**
 * 
 */
package net.sf.taverna.t2.security.interaction;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

import net.sf.taverna.t2.activities.interaction.InteractionActivityRunnable;
import net.sf.taverna.t2.activities.interaction.InteractionActivityType;
import net.sf.taverna.t2.activities.interaction.InteractionRequestor;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

/**
 * @author alanrw
 *
 */
public class InteractionUsernamePasswordProvider implements
		CredentialProviderSPI {
	
	Object theLock = new Object();
	
	private Template presentationTemplate;
	
	public InteractionUsernamePasswordProvider() {
		InteractionVelocity.checkVelocity();

			presentationTemplate = Velocity.getTemplate("UsernamePassword");

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#canHandleTrustConfirmation(java.security.cert.X509Certificate[])
	 */
	@Override
	public boolean canHandleTrustConfirmation(X509Certificate[] chain) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#canProvideJavaTruststorePassword()
	 */
	@Override
	public boolean canProvideJavaTruststorePassword() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#canProvideMasterPassword()
	 */
	@Override
	public boolean canProvideMasterPassword() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#canProvideUsernamePassword(java.net.URI)
	 */
	@Override
	public boolean canProvideUsernamePassword(URI serviceURI) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#getJavaTruststorePassword()
	 */
	@Override
	public String getJavaTruststorePassword() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#getMasterPassword(boolean)
	 */
	@Override
	public String getMasterPassword(boolean firstTime) {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#getProviderPriority()
	 */
	@Override
	public int getProviderPriority() {
		return 1000;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#getUsernamePassword(java.net.URI, java.lang.String)
	 */
	@Override
	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {
		
		HashMap<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("serviceURI", serviceURI.toASCIIString());
		inputs.put("requestingPrompt", requestingPrompt);

		InteractionSecurityRequestor requestor = new InteractionSecurityRequestor(theLock, inputs);
		try {
			synchronized(theLock) {
				Runnable interactionRunnable = new InteractionActivityRunnable(requestor, presentationTemplate);
				interactionRunnable.run();
				theLock.wait();
			}
			Map<String, Object> results = requestor.getResults();
			if ((results.get("username") == null) || (results.get("password") == null) || (results.get("shouldsave") == null)) {
				return null;
			}
			
			UsernamePassword username_password = new UsernamePassword();
			username_password.setUsername((String) results.get("username"));
			username_password.setPassword(((String) results.get("password")).toCharArray());
			username_password.setShouldSave(((Boolean)results.get("shouldsave")).booleanValue());
			return username_password;
		} catch (InterruptedException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI#shouldTrust(java.security.cert.X509Certificate[])
	 */
	@Override
	public TrustConfirmation shouldTrust(X509Certificate[] chain) {
		return null;
	}

}
