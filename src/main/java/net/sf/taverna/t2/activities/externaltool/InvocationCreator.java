/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.util.Map;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * @author alanrw
 *
 */
public interface InvocationCreator {
	
	public boolean canHandle(String mechanismType);
	
	public UseCaseInvocation convert(InvocationMechanism mechanism, UseCaseDescription description, Map<String, T2Reference> data, ReferenceService referenceService);

}
