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
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.thread.InReplyTo;
import org.apache.abdera.ext.thread.ThreadConstants;
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
	
	private static final String STATUS_OK = "OK";

	private static FeedListener instance = null;
	
	private static Logger logger = Logger.getLogger(FeedListener.class);

	private static Map<String, AsynchronousActivityCallback> callbackMap = new HashMap<String, AsynchronousActivityCallback>();
	
	public static synchronized FeedListener getInstance() {
		if (instance == null) {
			instance = new FeedListener();
		}
		return instance;
	}

	private static Map<String, Set<OutputPort>> outputPortsMap = new HashMap<String, Set<OutputPort>>();
	
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
		synchronized(callbackMap) {
		String refString = getReplyTo(entry);
		if (refString == null) {
			return;
		}
		if (callbackMap.containsKey(refString)) {
			AsynchronousActivityCallback callback = callbackMap.get(refString);
			InvocationContext context = callback
			.getContext();
			Element statusElement = entry.getExtension(InteractionActivity.getResultStatusQName());
			String statusContent = statusElement.getText().trim();
			if (!statusContent.equals(STATUS_OK)) {
				cleanup (refString);
				callback.fail(statusContent);
				return;
			}
			Element resultElement = entry.getExtension(InteractionActivity.getResultDataQName());
			String content = resultElement.getText();
	ReferenceService referenceService = context
			.getReferenceService();
			
			try {
				content = URLDecoder.decode(content,"UTF-8");
				ObjectMapper mapper = new ObjectMapper();
				Map<?,?> rootAsMap = mapper.readValue(content, Map.class);
				
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
				for (Object key : rootAsMap.keySet()) {
					String keyString = (String) key;
					Object value = rootAsMap.get(key);
					Integer depth = findPortDepth(refString, keyString);
					if (depth == null) {
						cleanup (refString);
						callback.fail("Data sent for unknown port : " + keyString);
					}
					outputs.put(keyString, referenceService.register(value, depth, true, context));
				}
				cleanup (refString);
				callback.receiveResult(outputs, new int[0]);
				
			} catch (JsonParseException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
			
		}
		}
	}
	
	private static void cleanup (String refString) {
		outputPortsMap.remove(refString);
		callbackMap.remove(refString);		
	}
	
	private static String getReplyTo(Entry entry) {
	        Element replyTo = entry.getFirstChild(InteractionActivity.getInReplyToQName());
	        if (replyTo == null) {
	        	return null;
	        }
	        return replyTo.getText();
	}

	private static Integer findPortDepth(String refString, String portName) {
		Set<OutputPort> ports = outputPortsMap.get(refString);
		for (OutputPort op : ports) {
			if (op.getName().equals(portName)) {
				return op.getDepth();
			}
		}
		return null;
	}

	public void registerInteraction(Entry entry,
			AsynchronousActivityCallback callback, Set<OutputPort> outputPorts) {
		synchronized(callbackMap) {
		String refString = entry.getId().toString();
		callbackMap.put(refString, callback);
		outputPortsMap.put(refString, outputPorts);
		}
	}

}
