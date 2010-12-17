/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.grid;

import de.uni_luebeck.inb.knowarc.grid.GridInfosystem;
import de.uni_luebeck.inb.knowarc.usecases.invocation.grid.GridUseCaseInvokation;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.activities.externaltool.ExternalToolInvocationConfigurationBean;

/**
 * @author alanrw
 *
 */
public final class ExternalToolGridInvocationConfigurationBean extends
		ExternalToolInvocationConfigurationBean<GridUseCaseInvokation> {
	
	private static GridInfosystem info = new GridInfosystem(ApplicationRuntime.getInstance().getApplicationHomeDir().getAbsolutePath(), null);

	@Override
	public GridUseCaseInvokation getAppropriateInvocation(ExternalToolActivity a) {
		GridUseCaseInvokation result = null;
		try {
			result = new GridUseCaseInvokation(info, a.getConfiguration().getUseCaseDescription());
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return result;

	}
	
	public boolean equals(Object o) {
		// TODO Fix this
		return (o instanceof ExternalToolGridInvocationConfigurationBean);
	}

}
