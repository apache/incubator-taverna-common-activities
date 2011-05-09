package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationGroupRemovedEvent extends InvocationManagerEvent {
	
	private InvocationGroup removedGroup;
	
	public InvocationGroupRemovedEvent(InvocationGroup removedGroup) {
		this.removedGroup = removedGroup;
	}

	/**
	 * @return the removedGroup
	 */
	public InvocationGroup getRemovedGroup() {
		return removedGroup;
	}

}
