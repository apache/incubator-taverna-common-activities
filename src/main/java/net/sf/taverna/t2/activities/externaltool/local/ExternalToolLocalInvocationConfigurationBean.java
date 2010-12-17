/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import java.io.IOException;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.activities.externaltool.ExternalToolInvocationConfigurationBean;
import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvokation;

/**
 * @author alanrw
 *
 */
public final class ExternalToolLocalInvocationConfigurationBean extends
		ExternalToolInvocationConfigurationBean<LocalUseCaseInvokation> {

	@Override
	public LocalUseCaseInvokation getAppropriateInvocation(ExternalToolActivity a) {
		LocalUseCaseInvokation result = null;
			try {
				result = new LocalUseCaseInvokation(null, a.getConfiguration().getUseCaseDescription());
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		return result;
	}

	public boolean equals(Object o) {
		return (o instanceof ExternalToolLocalInvocationConfigurationBean);
	}
}
