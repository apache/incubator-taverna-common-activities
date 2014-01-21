/**
 *
 */
package net.sf.taverna.t2.activities.interaction.feed;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.taverna.t2.activities.interaction.FeedReader;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ShowRequestFeedListener extends FeedReader {
	
	private static ShowRequestFeedListener instance;

	private static Logger logger = Logger
			.getLogger(ShowRequestFeedListener.class);
	
	private static boolean operational = !Boolean.valueOf(System.getProperty("taverna.interaction.ignore_requests"));
	
	public static synchronized ShowRequestFeedListener getInstance() {
		if ((instance == null) && operational) {
			instance = new ShowRequestFeedListener();
		}
		return instance;
	}

	private ShowRequestFeedListener() {
		super("ShowRequestFeedListener");
	}
	
			@Override
			protected void considerEntry(final Entry entry) {
				final Link presentationLink = entry.getLink("presentation");
				if (presentationLink != null) {
					try {
						Desktop.getDesktop().browse(
								presentationLink.getHref().toURI());
					} catch (final IOException e) {
						logger.error("Cannot open presentation");
					} catch (final URISyntaxException e) {
						logger.error("Cannot open presentation");
					}
				}
			}

}
