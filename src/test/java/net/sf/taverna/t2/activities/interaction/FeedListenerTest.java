/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author alanrw
 * 
 */
public class FeedListenerTest {

	@Test
	public void checkGetInstance() {
		final FeedListener listener1 = FeedListener.getInstance();
		final FeedListener listener2 = FeedListener.getInstance();

		assertEquals("FeedListener should have one instance", listener1,
				listener2);
	}
}
