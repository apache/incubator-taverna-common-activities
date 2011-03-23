/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

import org.jdom.Element;

import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public class ExternalToolLocalInvocationMechanism extends
		InvocationMechanism {

	@Override
	public String getType() {
		return LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE;
	}

	@Override
	public Element getXMLElement() {
		return new Element("localInvocation");

	}

}
