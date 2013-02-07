/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.atom.AtomUtils;
import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author alanrw
 *
 */
public final class FeedListener {

	private static final String STATUS_OK = "OK";

	private static final String DATA_READ_FAILED = "Data read failed";

	private static FeedListener instance;

	private static final Logger logger = Logger.getLogger(FeedListener.class);

	private static final Map<String, InteractionRequestor> requestorMap = new HashMap<String, InteractionRequestor>();

	public static synchronized FeedListener getInstance() {
		if (instance == null) {
			instance = new FeedListener();
		}
		return instance;
	}

	private FeedListener() {
		final Thread feedListenerThread = new Thread("FeedListener") {

			@Override
			public void run() {
				if (InteractionPreference.getInstance().getUseJetty()) {
					InteractionJetty.checkJetty();
				}
				final Parser parser = Abdera.getNewParser();
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
						final Document<Feed> doc = parser.parse(openStream, url
								.toString());
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
							considerInReplyTo(entry);
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
			}

		};
		feedListenerThread.start();
	}

	private static void considerInReplyTo(final Entry entry) {
		synchronized(requestorMap) {
		final String refString = getReplyTo(entry);
		if (refString == null) {
			return;
		}
		String runId = getRunId(entry);
		
		String entryUrl = InteractionPreference.getInstance().getFeedUrlString() + "/" + entry.getId().toASCIIString();
		InteractionRecorder.addResource(runId, refString, entryUrl);
		
		if (requestorMap.containsKey(refString)) {
			
			final InteractionRequestor requestor = requestorMap.get(refString);

			final Element statusElement = entry.getExtension(AtomUtils.getResultStatusQName());
			final String statusContent = statusElement.getText().trim();
			if (!statusContent.equals(STATUS_OK)) {
				cleanup (refString);
				requestor.fail(statusContent);
				return;
			}
			final String outputDataUrl = InteractionPreference.getOutputDataUrlString(refString);
			// Note that this may not really exist
			InteractionRecorder.addResource(runId, refString, outputDataUrl);
			String content = null;
			InputStream iStream;
			try {
				iStream = new URL(outputDataUrl).openStream();
				content = IOUtils.toString(iStream);
				iStream.close();
			} catch (MalformedURLException e1) {
				logger.error(e1);
				requestor.fail(DATA_READ_FAILED);
				return;
			} catch (IOException e1) {
				logger.error(e1);
				requestor.fail(DATA_READ_FAILED);
				return;
			}

			try {
				final ObjectMapper mapper = new ObjectMapper();
				final Map<String,Object> rootAsMap = mapper.readValue(content, Map.class);
				requestor.receiveResult(rootAsMap);
				cleanup (refString);

			} catch (final JsonParseException e) {
				logger.error(e);
			} catch (final IOException e) {
				logger.error(e);
			} catch (final Exception e) {
				logger.error(e);
			}

		}
		}
	}

	private static void cleanup (final String refString) {
		requestorMap.remove(refString);
	}

	private static String getReplyTo(final Entry entry) {
	        final Element replyTo = entry.getFirstChild(AtomUtils.getInReplyToQName());
	        if (replyTo == null) {
	        	return null;
	        }
	        return replyTo.getText();
	}

	private static String getRunId(final Entry entry) {
        final Element runIdElement = entry.getFirstChild(AtomUtils.getRunIdQName());
        if (runIdElement == null) {
        	return null;
        }
        return runIdElement.getText();
}



	public void registerInteraction(final Entry entry,
			final InteractionRequestor requestor) {
		synchronized(requestorMap) {
		final String refString = entry.getId().toString();
		requestorMap.put(refString, requestor);
		}
	}

}
