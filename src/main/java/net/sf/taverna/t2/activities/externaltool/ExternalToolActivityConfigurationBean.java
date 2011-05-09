package net.sf.taverna.t2.activities.externaltool;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

public final class ExternalToolActivityConfigurationBean {
	
	private InvocationGroup group;
	protected String repositoryUrl;
	protected String externaltoolid;
	protected UseCaseDescription useCaseDescription = null;
	private boolean edited = false;
	
    public boolean isEdited() {
		return edited;
	}

	private static InvocationGroupManager manager = InvocationGroupManager.getInstance();

	public ExternalToolActivityConfigurationBean() {
		group = manager.getDefaultGroup();
	}

	// You need to do setInvocationGroup because when you clone the bean you get a copy of the group
	public InvocationGroup getInvocationGroup() {
		setInvocationGroup(this.group);
	    return group;
	}

	public void setInvocationGroup(
			InvocationGroup group) {
		this.group = manager.checkGroup(group);
	}

	/**
	 * @return the repositoryUrl
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * @param repositoryUrl the repositoryUrl to set
	 */
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * @return the externaltoolid
	 */
	public String getExternaltoolid() {
		return externaltoolid;
	}

	/**
	 * @param externaltoolid the externaltoolid to set
	 */
	public void setExternaltoolid(String externaltoolid) {
		this.externaltoolid = externaltoolid;
	}

	/**
	 * @return the useCaseDescription
	 */
	public UseCaseDescription getUseCaseDescription() {
		return useCaseDescription;
	}

	/**
	 * @param useCaseDescription the useCaseDescription to set
	 */
	public void setUseCaseDescription(UseCaseDescription useCaseDescription) {
		this.useCaseDescription = useCaseDescription;
	}

	public void setEdited(boolean b) {
		this.edited  = b;
	}

}
