package net.sf.taverna.t2.activities.externaltool;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator;
import net.sf.taverna.t2.spi.SPIRegistry;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

public final class ExternalToolActivityConfigurationBean {
	
	private InvocationGroup group;
	
	private String mechanismType;
	
	private String mechanismName;
	
	private String mechanismXML;
	
	private transient InvocationMechanism mechanism;

	protected String repositoryUrl;
	protected String externaltoolid;
	protected UseCaseDescription useCaseDescription = null;
	private boolean edited = false;
	
    public boolean isEdited() {
		return edited;
	}

	public ExternalToolActivityConfigurationBean() {
	}

	public InvocationGroup getInvocationGroup() {
	    return group;
	}

	public void setInvocationGroup(
			InvocationGroup group) {
		this.group = group;
		clearMechanismInformation();
	}

	private void clearMechanismInformation() {
		this.mechanismType = null;
		this.mechanismName = null;
		this.mechanismXML = null;
		this.mechanism = null;
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

	/**
	 * Note this also sets the details
	 * 
	 * @param mechanism the mechanism to set
	 */
	public void setMechanism(InvocationMechanism mechanism) {
		this.mechanism = mechanism;
		convertMechanismToDetails();
		this.group = null;
	}
	
	public void convertMechanismToDetails() {
		if (mechanism != null) {
			this.setMechanismXML(mechanism.getXML());
			this.setMechanismName(mechanism.getName());
			this.setMechanismType(mechanism.getType());			
		}
	}

	/**
	 * @param mechanismType the mechanismType to set
	 */
	public void setMechanismType(String mechanismType) {
		this.mechanismType = mechanismType;
	}

	/**
	 * @param mechanismName the mechanismName to set
	 */
	public void setMechanismName(String mechanismName) {
		this.mechanismName = mechanismName;
	}

	/**
	 * @param mechanismXML the mechanismXML to set
	 */
	public void setMechanismXML(String mechanismXML) {
		this.mechanismXML = mechanismXML;
	}
	
	private static SPIRegistry<MechanismCreator> mechanismCreatorRegistry = new SPIRegistry(MechanismCreator.class);
	
	public void convertDetailsToMechanism() {
		if (mechanismXML != null) {
			for (MechanismCreator mc : mechanismCreatorRegistry.getInstances()) {
				if (mc.canHandle(getMechanismType())) {
					mechanism = mc.convert(getMechanismXML(), getMechanismName());
					break;
				}
			}
		}
	}

	/**
	 * @return the mechanism
	 */
	public InvocationMechanism getMechanism() {
		
		return mechanism;
	}

	/**
	 * @return the mechanismType
	 */
	public String getMechanismType() {
		return mechanismType;
	}

	/**
	 * @return the mechanismName
	 */
	public String getMechanismName() {
		return mechanismName;
	}

	/**
	 * @return the mechanismXML
	 */
	public String getMechanismXML() {
		return mechanismXML;
	}

}
