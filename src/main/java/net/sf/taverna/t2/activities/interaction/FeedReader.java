package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getFeedUrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.log4j.Logger;

public abstract class FeedReader extends Thread {
	static final Logger logger = Logger.getLogger(FeedReader.class);

	protected FeedReader(String name) {
		super(name);
		setDaemon(true);
	}

	protected abstract void considerEntry(Entry entry);

	@Override
	public final void run() {
		try {
			final Parser parser = new FOMParser();
			Date lastChecked = new Date();
			while (!interrupted()) {
				try {
					sleep(5000);
					Date newLastChecked = new Date();
					for (Entry entry : getFeedContent(parser)) {
						Date d = entry.getEdited();
						if (d == null) {
							d = entry.getUpdated();
						}
						if (d == null) {
							d = entry.getPublished();
						}
						if (d == null || d.before(lastChecked)) {
							break;
						}
						considerEntry(entry);
					}
					lastChecked = newLastChecked;
				} catch (InterruptedException e) {
					break;
				} catch (ParseException | IOException e) {
					logger.error(e);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private List<Entry> getFeedContent(Parser parser)
			throws MalformedURLException, IOException {
		URL url = getFeedUrl(true);
		try (InputStream openStream = url.openStream()) {
			Document<Feed> doc = parser.parse(openStream, url.toString());
			return doc.getRoot().sortEntriesByEdited(true).getEntries();
		}
	}
}