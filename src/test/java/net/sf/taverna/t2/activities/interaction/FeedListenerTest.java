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
		final ResponseFeedListener listener1 = ResponseFeedListener.getInstance();
		final ResponseFeedListener listener2 = ResponseFeedListener.getInstance();

		assertEquals("ResponseFeedListener should have one instance", listener1,
				listener2);
	}
}
