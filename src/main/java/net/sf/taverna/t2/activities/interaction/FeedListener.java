/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.thread.InReplyTo;
import org.apache.abdera.ext.thread.ThreadHelper;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author alanrw
 *
 */
public class FeedListener {
	
	private static FeedListener instance = null;
	
	private static Logger logger = Logger.getLogger(FeedListener.class);

	private static Map<String, AsynchronousActivityCallback> callbackMap = new HashMap<String, AsynchronousActivityCallback>();
	
	public static FeedListener getInstance() {
		if (instance == null) {
			instance = new FeedListener();
		}
		return instance;
	}
	
	private FeedListener() {
		Thread feeListenerThread = new Thread(){

			@Override
			public void run() {
				Parser parser = Abdera.getNewParser();
				IRI lastCheckedId = null;
					while (true) {
						try {
							sleep(5000);
						} catch (InterruptedException e1) {
							logger.error(e1);
						}
						InputStream openStream = null;
						try {
						URL url = new URL(InteractionPreference.getInstance().getFeedUrl());
						openStream = url.openStream();
						Document<Feed> doc = parser.parse(openStream, url.toString());
						Feed feed = doc.getRoot().sortEntriesByEdited(true);

						IRI newLastCheckedId = null;
						for (Entry entry : feed.getEntries()) {
							if (entry.getId().equals(lastCheckedId)) {
								break;
							}
							if (newLastCheckedId == null) {
								newLastCheckedId = entry.getId();
							}
							if (ThreadHelper.getInReplyTo(entry) != null) {
								considerInReplyTo(feed, entry);
							}
						}
						if (newLastCheckedId != null) {
							lastCheckedId = newLastCheckedId;
						}
						} catch (MalformedURLException e) {
							logger.error(e);
						} catch (ParseException e) {
							logger.error(e);
						} catch (IOException e) {
							logger.error(e);
						}
						finally {
							/*try {
								openStream.close();
							} catch (IOException e) {
								logger.error(e);
							}*/

						}
					}
			}


			};
		feeListenerThread.start();
	}
	
	private static void considerInReplyTo(Feed feed, Entry entry) {
		InReplyTo irt = ThreadHelper.getInReplyTo(entry);
		String refString = irt.getRef().toString();
		if (callbackMap.containsKey(refString)) {
			AsynchronousActivityCallback callback = callbackMap.get(refString);
			InvocationContext context = callback
			.getContext();
			Element resultElement = entry.getExtension(InteractionActivity.getResultDataQName());
			String content = resultElement.getText();
	ReferenceService referenceService = context
			.getReferenceService();
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				Map<?,?> rootAsMap = mapper.readValue(content, Map.class);
				
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
				for (Object key : rootAsMap.keySet()) {
					String keyString = (String) key;
					Object value = rootAsMap.get(key);
					outputs.put(keyString, referenceService.register(value, 0, true, context));
				}
				callback.receiveResult(outputs, new int[0]);
				
			} catch (JsonParseException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
			
		}
	}

	public void registerInteraction(Entry entry,
			AsynchronousActivityCallback callback) {
		String refString = entry.getId().toString();
		callbackMap.put(refString, callback);
	}

}
