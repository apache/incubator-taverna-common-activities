package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationGroupAddedEvent extends InvocationManagerEvent {
	
	private InvocationGroup addedGroup;
	
	public InvocationGroupAddedEvent(InvocationGroup addedGroup) {
		this.addedGroup = addedGroup;
	}

	/**
	 * @return the addedGroup
	 */
	public InvocationGroup getAddedGroup() {
		return addedGroup;
	}

}
