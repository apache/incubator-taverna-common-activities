/**
 * 
 */
package net.sf.taverna.t2.security.interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.activities.interaction.InteractionActivityType;
import net.sf.taverna.t2.activities.interaction.InteractionRequestor;
import net.sf.taverna.t2.activities.interaction.InteractionType;

/**
 * @author alanrw
 *
 */
public class InteractionSecurityRequestor implements InteractionRequestor {

	private final HashMap<String, Object> inputs;
	private final Object theLock;
	private Map<String, Object> resultMap;

	public InteractionSecurityRequestor(Object theLock, HashMap<String, Object> inputs) {
		this.theLock = theLock;
		this.inputs = inputs;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#carryOn()
	 */
	@Override
	public void carryOn() {
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#fail(java.lang.String)
	 */
	@Override
	public void fail(String string) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#generateId()
	 */
	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#getInputData()
	 */
	@Override
	public Map<String, Object> getInputData() {
		return inputs;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#getInteractionType()
	 */
	@Override
	public InteractionType getInteractionType() {
		return InteractionType.SecurityRequest;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#getPresentationOrigin()
	 */
	@Override
	public String getPresentationOrigin() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#getPresentationType()
	 */
	@Override
	public InteractionActivityType getPresentationType() {
		return InteractionActivityType.VelocityTemplate;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#getRunId()
	 */
	@Override
	public String getRunId() {
		// TODO Auto-generated method stub
		return "fred";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.interaction.InteractionRequestor#receiveResult(java.util.Map)
	 */
	@Override
	public void receiveResult(Map<String, Object> resultMap) {
		
		this.resultMap = resultMap;
		synchronized (theLock) {
		theLock.notifyAll();
		}
	}

	public Map<String, Object> getResults() {
		return resultMap;
	}

}
