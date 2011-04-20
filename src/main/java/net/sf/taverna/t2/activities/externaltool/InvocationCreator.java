/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

/**
 * @author alanrw
 *
 */
public interface InvocationCreator {
	
	public boolean canHandle(String mechanismType);
	
	public UseCaseInvocation convert(InvocationMechanism mechanism, UseCaseDescription description);

}
