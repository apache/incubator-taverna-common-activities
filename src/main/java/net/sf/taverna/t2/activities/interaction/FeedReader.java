package net.sf.taverna.t2.activities.interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.Abdera;
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
			if (InteractionPreference.getInstance().getUseJetty()) {
				InteractionJetty.checkJetty();
			}
			final Parser parser = new FOMParser();
			Date lastCheckedDate = new Date();
			while (true) {
				try {
					sleep(5000);
				} catch (final InterruptedException e1) {
					logger.error(e1);
				}
				InputStream openStream = null;
				try {
					final Date newLastCheckedDate = new Date();
					final URL url = InteractionPreference.getFeedUrl();
					openStream = url.openStream();
					final Document<Feed> doc = parser.parse(openStream,
							url.toString());
					final Feed feed = doc.getRoot().sortEntriesByEdited(true);

					for (final Entry entry : feed.getEntries()) {

						Date d = entry.getEdited();
						if (d == null) {
							d = entry.getUpdated();
						}
						if (d == null) {
							d = entry.getPublished();
						}
						 if (d.before(lastCheckedDate)) {
						 break;
						 }
						this.considerEntry(entry);
					}
					lastCheckedDate = newLastCheckedDate;
				} catch (final MalformedURLException e) {
					logger.error(e);
				} catch (final ParseException e) {
					logger.error(e);
				} catch (final IOException e) {
					logger.error(e);
				} finally {
					try {
						if (openStream != null) {
							openStream.close();
						}
					} catch (final IOException e) {
						logger.error(e);
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e);
		}
	}
}