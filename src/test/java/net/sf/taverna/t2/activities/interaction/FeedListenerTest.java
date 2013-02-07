/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author alanrw
 *
 */
public class FeedListenerTest {

	@Test
	public void checkGetInstance() {
		FeedListener listener1 = FeedListener.getInstance();
		FeedListener listener2 = FeedListener.getInstance();
		
		assertEquals("FeedListener should have one instance", listener1, listener2);
	}
}
