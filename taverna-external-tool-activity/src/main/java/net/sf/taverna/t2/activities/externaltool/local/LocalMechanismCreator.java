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
		Element directoryElement = detailsElement.getChild("directory");
		if (directoryElement != null) {
			result.setDirectory(directoryElement.getText());
		}
		Element shellPrefixElement = detailsElement.getChild("shellPrefix");
		if (shellPrefixElement != null) {
			result.setShellPrefix(shellPrefixElement.getText());
		}
		Element linkCommandElement = detailsElement.getChild("linkCommand");
		if (linkCommandElement != null) {
			result.setLinkCommand(linkCommandElement.getText());
		}
		Element retrieveDataElement = detailsElement.getChild("retrieveData");
		result.setRetrieveData(retrieveDataElement != null);

		return result;
	}

}
