package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getFeedUrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.log4j.Logger;

public abstract class FeedReader extends Thread {

	static final Logger logger = Logger.getLogger(FeedReader.class);

	public FeedReader(final String name) {
		super(name);
		this.setDaemon(true);
	}

	protected abstract void considerEntry(Entry entry);

	@Override
	public void run() {
		try {
			final Parser parser = new FOMParser();
			Date lastCheckedDate = new Date();
			while (true) {
				try {
					sleep(5000);
				} catch (InterruptedException e1) {
					logger.error(e1);
				}
				try {
					final Date newLastCheckedDate = new Date();
					final URL url = getFeedUrl(true);
					final Document<Feed> doc;
					try (InputStream openStream = url.openStream()) {
						doc = parser.parse(openStream, url.toString());
					}
					final Feed feed = doc.getRoot().sortEntriesByEdited(true);

					for (Entry entry : feed.getEntries()) {
						Date d = entry.getEdited();
						if (d == null) {
							d = entry.getUpdated();
						}
						if (d == null) {
							d = entry.getPublished();
						}
						if (d == null || d.before(lastCheckedDate)) {
							break;
						}
						considerEntry(entry);
					}
					lastCheckedDate = newLastCheckedDate;
				} catch (ParseException | IOException e) {
					logger.error(e);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}