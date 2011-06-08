package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationMechanismRemovedEvent extends InvocationManagerEvent {
	
	private InvocationMechanism removedMechanism;
	private final InvocationMechanism replacementMechanism;
	
	public InvocationMechanismRemovedEvent(InvocationMechanism removedMechanism, InvocationMechanism replacementMechanism) {
		this.removedMechanism = removedMechanism;
		this.replacementMechanism = replacementMechanism;
	}

	/**
	 * @return the addeMechanism
	 */
	public InvocationMechanism getRemovedMechanism() {
		return removedMechanism;
	}
	
	public InvocationMechanism getReplacementMechanism() {
		return replacementMechanism;
	}

}
