/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import java.io.IOException;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.externaltool.InvocationCreator;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public final class LocalInvocationCreator implements
		InvocationCreator {
	
	private static Logger logger = Logger.getLogger(LocalInvocationCreator.class);

	public boolean equals(Object o) {
		return (o instanceof LocalInvocationCreator);
	}

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(String xml, UseCaseDescription description) {
		UseCaseInvocation result = null;
		try {
			result = new LocalUseCaseInvocation(description);
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}
}
