/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import static net.sf.taverna.t2.activities.interaction.InteractionRecorder.addResource;
import static net.sf.taverna.t2.activities.interaction.InteractionRecorder.deleteInteraction;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getInReplyToQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getResultStatusQName;
import static net.sf.taverna.t2.activities.interaction.atom.AtomUtils.getRunIdQName;
import static net.sf.taverna.t2.activities.interaction.preference.InteractionPreference.getOutputDataUrlString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author alanrw
 * 
 */
public final class ResponseFeedListener extends FeedReader {

	private static final String STATUS_OK = "OK";

	private static final String DATA_READ_FAILED = "Data read failed";

	private static ResponseFeedListener instance;

	private static final Logger logger = Logger
			.getLogger(ResponseFeedListener.class);

	private static final Map<String, InteractionRequestor> requestorMap = new HashMap<>();

	public static synchronized ResponseFeedListener getInstance() {
		if (instance == null) {
			instance = new ResponseFeedListener();
		}
		return instance;
	}

	private ResponseFeedListener() {
		super("ResponseFeedListener");
	}

	@Override
	protected void considerEntry(final Entry entry) {
		considerInReplyTo(entry);
	}

	static void considerInReplyTo(final Entry entry) {
		synchronized (requestorMap) {
			String refString = getReplyTo(entry);
			if (refString == null) {
				return;
			}
			String runId = getRunId(entry);

			String entryUrl = InteractionPreference.getInstance()
					.getFeedUrlString(true)
					+ "/"
					+ entry.getId().toASCIIString();
			addResource(runId, refString, entryUrl);

			if (requestorMap.containsKey(refString)) {
				InteractionRequestor requestor = requestorMap.get(refString);

				Element statusElement = entry
						.getExtension(getResultStatusQName());
				String statusContent = statusElement.getText().trim();
				if (!statusContent.equals(STATUS_OK)) {
					cleanup(refString);
					requestor.fail(statusContent);
					return;
				}
				String outputDataUrl = getOutputDataUrlString(true, refString);
				// Note that this may not really exist
				addResource(runId, refString, outputDataUrl);
				String content = null;
				try (InputStream iStream = new URL(outputDataUrl).openStream()) {
					content = IOUtils.toString(iStream);
				} catch (final IOException e1) {
					logger.error(e1);
					requestor.fail(DATA_READ_FAILED);
					return;
				}

				try {
					ObjectMapper mapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> rootAsMap = mapper.readValue(content,
							Map.class);
					requestor.receiveResult(rootAsMap);
					cleanup(refString);
					deleteInteraction(runId, refString);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

	private static void cleanup(final String refString) {
		requestorMap.remove(refString);
	}

	private static String getReplyTo(Entry entry) {
		Element replyTo = entry.getFirstChild(getInReplyToQName());
		if (replyTo == null) {
			return null;
		}
		return replyTo.getText();
	}

	private static String getRunId(Entry entry) {
		Element runIdElement = entry.getFirstChild(getRunIdQName());
		if (runIdElement == null) {
			return null;
		}
		return runIdElement.getText();
	}

	public void registerInteraction(Entry entry, InteractionRequestor requestor) {
		synchronized (requestorMap) {
			String refString = entry.getId().toString();
			requestorMap.put(refString, requestor);
		}
	}
}
