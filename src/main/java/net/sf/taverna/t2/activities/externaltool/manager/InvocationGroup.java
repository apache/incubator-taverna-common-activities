/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.manager;

import net.sf.taverna.t2.spi.SPIRegistry;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class InvocationGroup {

	private static Logger logger = Logger.getLogger(InvocationGroup.class);

	private String invocationGroupName;
	
	private String mechanismType;
	
	private String mechanismName;
	
	private String mechanismXML;
	
	private transient InvocationMechanism mechanism;

	/**
	 * @return the invocationGroupName
	 */
	public String getName() {
		return invocationGroupName;
	}

	/**
	 * @param invocationGroupName the invocationGroupName to set
	 */
	public void setName(String invocationGroupName) {
		this.invocationGroupName = invocationGroupName;
	}

	/**
	 * @return the mechanismType
	 */
	public String getMechanismType() {
		return mechanismType;
	}

	/**
	 * @param mechanismType the mechanismType to set
	 */
	public void setMechanismType(String mechanismType) {
		this.mechanismType = mechanismType;
	}

	/**
	 * @return the mechanismName
	 */
	public String getMechanismName() {
		return mechanismName;
	}

	/**
	 * @param mechanismName the mechanismName to set
	 */
	public void setMechanismName(String mechanismName) {
		this.mechanismName = mechanismName;
	}

	/**
	 * @return the mechanismXML
	 */
	public String getMechanismXML() {
		return mechanismXML;
	}

	/**
	 * @param mechanismXML the mechanismXML to set
	 */
	public void setMechanismXML(String mechanismXML) {
		this.mechanismXML = mechanismXML;
	}

	private static SPIRegistry<MechanismCreator> mechanismCreatorRegistry = new SPIRegistry(MechanismCreator.class);
	
	/**
	 * @return the mechanism
	 */
	public InvocationMechanism getMechanism() {
		return mechanism;
	}

	/**
	 * Note this also sets the corresponding details
	 * 
	 * @param mechanism the mechanism to set
	 */
	public void setMechanism(InvocationMechanism mechanism) {
		this.mechanism = mechanism;
		convertMechanismToDetails();
	}
	
	public String toString() {
		return getName();
	}

	public void convertMechanismToDetails() {
		this.setMechanismXML(mechanism.getXML());
		this.setMechanismName(mechanism.getName());
		this.setMechanismType(mechanism.getType());	
	}

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
}
