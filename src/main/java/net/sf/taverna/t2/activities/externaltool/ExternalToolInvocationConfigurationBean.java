/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

/**
 * @author alanrw
 *
 */
public abstract class ExternalToolInvocationConfigurationBean<T extends UseCaseInvocation> {
	
	public abstract T getAppropriateInvocation(ExternalToolActivity a);

}
