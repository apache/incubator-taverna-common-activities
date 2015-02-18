package net.sf.taverna.t2.activities.externaltool;

import java.util.List;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

@ConfigurationBean(uri = ExternalToolActivity.URI + "#Config")
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

	private List<MechanismCreator> mechanismCreators;

    public boolean isEdited() {
		return edited;
	}

	public ExternalToolActivityConfigurationBean() {
	}

	public InvocationGroup getInvocationGroup() {
	    return group;
	}

	@ConfigurationProperty(name = "invocationGroup", label = "InvocationGroup", required=false)
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
	@ConfigurationProperty(name = "repositoryUrl", label = "Repository URL", required=false)
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
	@ConfigurationProperty(name = "toolId", label = "Tool ID")
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
	@ConfigurationProperty(name = "toolDescription", label = "Tool Description")
	public void setUseCaseDescription(UseCaseDescription useCaseDescription) {
		this.useCaseDescription = useCaseDescription;
	}

	@ConfigurationProperty(name = "edited", label = "Edited", required=false)
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
	@ConfigurationProperty(name = "mechanismType", label = "Mechanism Type", required=false)
	public void setMechanismType(String mechanismType) {
		this.mechanismType = mechanismType;
	}

	/**
	 * @param mechanismName the mechanismName to set
	 */
	@ConfigurationProperty(name = "mechanismName", label = "Mechanism Name", required=false)
	public void setMechanismName(String mechanismName) {
		this.mechanismName = mechanismName;
	}

	/**
	 * @param mechanismXML the mechanismXML to set
	 */
	@ConfigurationProperty(name = "mechanismXML", label = "Mechanism XML", required=false)
	public void setMechanismXML(String mechanismXML) {
		this.mechanismXML = mechanismXML;
	}

	public void convertDetailsToMechanism() {
		if (mechanismXML != null) {
			for (MechanismCreator mc : mechanismCreators) {
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

	public void setMechanismCreators(List<MechanismCreator> mechanismCreators) {
		this.mechanismCreators = mechanismCreators;
	}

}
