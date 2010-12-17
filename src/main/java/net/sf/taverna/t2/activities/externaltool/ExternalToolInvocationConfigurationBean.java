/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvokation;

/**
 * @author alanrw
 *
 */
public abstract class ExternalToolInvocationConfigurationBean<T extends UseCaseInvokation> {
	
	public abstract T getAppropriateInvocation(ExternalToolActivity a);

}
