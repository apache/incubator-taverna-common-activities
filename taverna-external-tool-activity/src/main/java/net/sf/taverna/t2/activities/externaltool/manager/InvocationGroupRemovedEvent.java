package net.sf.taverna.t2.activities.externaltool.manager;

public class InvocationGroupRemovedEvent extends InvocationManagerEvent {
	
	private InvocationGroup removedGroup;
	private final InvocationGroup replacementGroup;
	
	public InvocationGroupRemovedEvent(InvocationGroup removedGroup, InvocationGroup replacementGroup) {
		this.removedGroup = removedGroup;
		this.replacementGroup = replacementGroup;
	}

	/**
	 * @return the removedGroup
	 */
	public InvocationGroup getRemovedGroup() {
		return removedGroup;
	}
	
	public InvocationGroup getReplacementGroup() {
		return replacementGroup;
	}

}
