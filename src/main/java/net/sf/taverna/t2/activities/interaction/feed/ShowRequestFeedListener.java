/**
 *
 */
package net.sf.taverna.t2.activities.interaction.feed;

import static java.awt.Desktop.getDesktop;
import static java.lang.Boolean.getBoolean;
import static org.apache.log4j.Logger.getLogger;

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
	private static Logger logger = getLogger(ShowRequestFeedListener.class);
	private final static boolean operational;
	static {
		operational = !getBoolean("taverna.interaction.ignore_requests");
	}

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
	protected void considerEntry(Entry entry) {
		Link presentationLink = entry.getLink("presentation");
		if (presentationLink != null) {
			try {
				getDesktop().browse(presentationLink.getHref().toURI());
			} catch (final IOException | URISyntaxException e) {
				logger.error("Cannot open presentation");
			}
		}
	}
}
