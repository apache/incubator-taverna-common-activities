/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author alanrw
 *
 */
public class FeedListener {
	
	private static final String STATUS_OK = "OK";

	private static FeedListener instance = null;
	
	private static Logger logger = Logger.getLogger(FeedListener.class);

	private static Map<String, InteractionRequestor> requestorMap = new HashMap<String, InteractionRequestor>();
	
	public static synchronized FeedListener getInstance() {
		if (instance == null) {
			instance = new FeedListener();
		}
		return instance;
	}
	
	private FeedListener() {
		Thread feeListenerThread = new Thread() {

			@Override
			public void run() {
				if (InteractionPreference.getInstance().getUseJetty()) {
					InteractionJetty.checkJetty();
				}
				Parser parser = Abdera.getNewParser();
				Date lastCheckedDate = new Date();
				while (true) {
					try {
						sleep(5000);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
					InputStream openStream = null;
					try {
						Date newLastCheckedDate = new Date();
						URL url = new URL(InteractionPreference.getInstance().getFeedUrl());
						openStream = url.openStream();
						Document<Feed> doc = parser.parse(openStream, url
								.toString());
						Feed feed = doc.getRoot().sortEntriesByEdited(true);

						for (Entry entry : feed.getEntries()) {
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
							considerInReplyTo(feed, entry);
						}
						lastCheckedDate = newLastCheckedDate;
					} catch (MalformedURLException e) {
						logger.error(e);
					} catch (ParseException e) {
						logger.error(e);
					} catch (IOException e) {
						logger.error(e);
					} finally {
						try {
							if (openStream != null) {
								openStream.close();
							}
						} catch (IOException e) {
							logger.error(e);
						}
					}
				}
			}

		};
		feeListenerThread.start();
	}
	
	private static void considerInReplyTo(Feed feed, Entry entry) {
		synchronized(requestorMap) {
		String refString = getReplyTo(entry);
		if (refString == null) {
			return;
		}
		if (requestorMap.containsKey(refString)) {
			InteractionRequestor requestor = requestorMap.get(refString);

			Element statusElement = entry.getExtension(AtomUtils.getResultStatusQName());
			String statusContent = statusElement.getText().trim();
			if (!statusContent.equals(STATUS_OK)) {
				cleanup (refString);
				requestor.fail(statusContent);
				return;
			}
			Element resultElement = entry.getExtension(AtomUtils.getResultDataQName());
			String content = resultElement.getText();
			
			try {
				content = URLDecoder.decode(content,"UTF-8");
				ObjectMapper mapper = new ObjectMapper();
				Map<String,Object> rootAsMap = mapper.readValue(content, Map.class);
				requestor.receiveResult(rootAsMap);
				cleanup (refString);
				
			} catch (JsonParseException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
		}
	}
	
	private static void cleanup (String refString) {
		requestorMap.remove(refString);		
	}
	
	private static String getReplyTo(Entry entry) {
	        Element replyTo = entry.getFirstChild(AtomUtils.getInReplyToQName());
	        if (replyTo == null) {
	        	return null;
	        }
	        return replyTo.getText();
	}



	public void registerInteraction(Entry entry,
			InteractionRequestor requestor) {
		synchronized(requestorMap) {
		String refString = entry.getId().toString();
		requestorMap.put(refString, requestor);
		}
	}

}
