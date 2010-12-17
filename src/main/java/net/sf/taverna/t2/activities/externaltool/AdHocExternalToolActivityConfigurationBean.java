/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

/**
 * @author alanrw
 *
 */
public class AdHocExternalToolActivityConfigurationBean extends
		ExternalToolActivityConfigurationBean {
	
	private UseCaseDescription useCaseDescription = null;

	public AdHocExternalToolActivityConfigurationBean() {
		super();
		useCaseDescription = new UseCaseDescription(UUID.randomUUID().toString());
	}
	

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.ExternalToolActivityConfigurationBean#getUseCaseDescription()
	 */
	@Override
	public UseCaseDescription getUseCaseDescription() {
		return useCaseDescription;
	}

}
