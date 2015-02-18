package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationMechanismAddedEvent extends InvocationManagerEvent {
	
	private InvocationMechanism addedMechanism;
	
	public InvocationMechanismAddedEvent(InvocationMechanism addedMechanism) {
		this.addedMechanism = addedMechanism;
	}

	/**
	 * @return the addeMechanism
	 */
	public InvocationMechanism getAddedMechanism() {
		return addedMechanism;
	}

}
