package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationGroupChangedEvent extends InvocationManagerEvent {
	
	private InvocationGroup changedGroup;
	
	public InvocationGroupChangedEvent(InvocationGroup changedGroup) {
		this.changedGroup = changedGroup;
	}

	/**
	 * @return the changedGroup
	 */
	public InvocationGroup getChangedGroup() {
		return changedGroup;
	}

}
