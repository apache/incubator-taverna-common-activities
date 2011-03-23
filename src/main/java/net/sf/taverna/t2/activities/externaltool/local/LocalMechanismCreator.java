/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import org.jdom.Element;

import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator;

/**
 * @author alanrw
 *
 */
public class LocalMechanismCreator extends MechanismCreator {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE);
	}

	public InvocationMechanism convert(Element detailsElement,
			String mechanismName) {
		ExternalToolLocalInvocationMechanism result = new ExternalToolLocalInvocationMechanism();
		result.setName(mechanismName);
		return result;
	}

}
