package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationMechanismRemovedEvent extends InvocationManagerEvent {
	
	private InvocationMechanism removedMechanism;
	
	public InvocationMechanismRemovedEvent(InvocationMechanism removedMechanism) {
		this.removedMechanism = removedMechanism;
	}

	/**
	 * @return the addeMechanism
	 */
	public InvocationMechanism getRemovedMechanism() {
		return removedMechanism;
	}

}
