package net.sf.taverna.t2.activities.externaltool;

import net.sf.taverna.t2.activities.externaltool.local.ExternalToolLocalInvocationConfigurationBean;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

public abstract class ExternalToolActivityConfigurationBean {
	
	public abstract UseCaseDescription getUseCaseDescription();
	
	private ExternalToolInvocationConfigurationBean<?> invocationBean;
	
	protected ExternalToolActivityConfigurationBean() {
		invocationBean = new ExternalToolLocalInvocationConfigurationBean();
	}

	public ExternalToolInvocationConfigurationBean<?> getInvocationBean() {
		return invocationBean;
	}

	public void setInvocationBean(
			ExternalToolInvocationConfigurationBean<?> invocationBean) {
		this.invocationBean = invocationBean;
	}

}
