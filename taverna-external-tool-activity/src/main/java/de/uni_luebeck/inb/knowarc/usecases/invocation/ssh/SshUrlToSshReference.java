/**
 *
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

/**
 * @author alanrw
 *
 */
public class SshUrlToSshReference implements ValueToReferenceConverterSPI {

	private CredentialManager credentialManager;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.reference.ValueToReferenceConverterSPI#canConvert(java.lang.Object, net.sf.taverna.t2.reference.ReferenceContext)
	 */
	@Override
	public boolean canConvert(Object o, ReferenceContext context) {
		return (o instanceof SshUrl);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.reference.ValueToReferenceConverterSPI#convert(java.lang.Object, net.sf.taverna.t2.reference.ReferenceContext)
	 */
	@Override
	public ExternalReferenceSPI convert(Object o, ReferenceContext context)
			throws ValueToReferenceConversionException {
		SshReference result = new SshReference((SshUrl) o);
		result.setCredentialManager(credentialManager);
		return result;
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

}
