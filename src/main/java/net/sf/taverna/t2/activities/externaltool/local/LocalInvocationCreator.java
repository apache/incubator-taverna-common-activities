/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import java.io.IOException;
import java.util.Map;

import net.sf.taverna.t2.activities.externaltool.InvocationCreator;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

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
	public UseCaseInvocation convert(InvocationMechanism m, UseCaseDescription description, Map<String, T2Reference> data, ReferenceService referenceService) {
	    ExternalToolLocalInvocationMechanism mechanism = (ExternalToolLocalInvocationMechanism) m;
		UseCaseInvocation result = null;
		try {
		    result = new LocalUseCaseInvocation(description, mechanism.isRetrieveData(), mechanism.getDirectory(), mechanism.getShellPrefix(), mechanism.getLinkCommand());
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}
}
