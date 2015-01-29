package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationMechanismChangedEvent extends InvocationManagerEvent {
	
	private InvocationMechanism changedMechanism;
	
	public InvocationMechanismChangedEvent(InvocationMechanism changedMechanism) {
		this.changedMechanism = changedMechanism;
	}

	/**
	 * @return the changedMechanism
	 */
	public InvocationMechanism getChangedMechanism() {
		return changedMechanism;
	}

}
