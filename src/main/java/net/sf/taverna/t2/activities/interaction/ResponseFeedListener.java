/**
 *
 */
package net.sf.taverna.t2.activities.interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.interaction.atom.AtomUtils;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author alanrw
 * 
 */
public final class ResponseFeedListener extends FeedReader {
	
	private InteractionRecorder interactionRecorder;

	private InteractionPreference interactionPreference;

	private static final String STATUS_OK = "OK";

	private static final String DATA_READ_FAILED = "Data read failed";

	private static ResponseFeedListener instance;

	private static final Logger logger = Logger.getLogger(ResponseFeedListener.class);

	private static final Map<String, InteractionRequestor> requestorMap = new HashMap<String, InteractionRequestor>();

	private ResponseFeedListener() {
		super("ResponseFeedListener");
	}
	
	@Override
	protected void considerEntry(final Entry entry) {
		synchronized (requestorMap) {
			final String refString = getReplyTo(entry);
			if (refString == null) {
				return;
			}
			final String runId = getRunId(entry);

			final String entryUrl = interactionPreference
					.getFeedUrlString() + "/" + entry.getId().toASCIIString();
			interactionRecorder.addResource(runId, refString, entryUrl);

			if (requestorMap.containsKey(refString)) {

				final InteractionRequestor requestor = requestorMap
						.get(refString);

				final Element statusElement = entry.getExtension(AtomUtils
						.getResultStatusQName());
				final String statusContent = statusElement.getText().trim();
				if (!statusContent.equals(STATUS_OK)) {
					cleanup(refString);
					requestor.fail(statusContent);
					return;
				}
				final String outputDataUrl = interactionPreference
						.getOutputDataUrlString(refString);
				// Note that this may not really exist
				interactionRecorder
						.addResource(runId, refString, outputDataUrl);
				String content = null;
				InputStream iStream;
				try {
					iStream = new URL(outputDataUrl).openStream();
					content = IOUtils.toString(iStream);
					iStream.close();
				} catch (final MalformedURLException e1) {
					logger.error(e1);
					requestor.fail(DATA_READ_FAILED);
					return;
				} catch (final IOException e1) {
					logger.error(e1);
					requestor.fail(DATA_READ_FAILED);
					return;
				}

				try {
					final ObjectMapper mapper = new ObjectMapper();
					@SuppressWarnings("unchecked")
					final Map<String, Object> rootAsMap = mapper.readValue(
							content, Map.class);
					requestor.receiveResult(rootAsMap);
					cleanup(refString);
					interactionRecorder.deleteInteraction(runId, refString);

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

	private static void cleanup(final String refString) {
		requestorMap.remove(refString);
	}

	private static String getReplyTo(final Entry entry) {
		final Element replyTo = entry.getFirstChild(AtomUtils
				.getInReplyToQName());
		if (replyTo == null) {
			return null;
		}
		return replyTo.getText();
	}

	private static String getRunId(final Entry entry) {
		final Element runIdElement = entry.getFirstChild(AtomUtils
				.getRunIdQName());
		if (runIdElement == null) {
			return null;
		}
		return runIdElement.getText();
	}

	public void registerInteraction(final Entry entry,
			final InteractionRequestor requestor) {
		synchronized (requestorMap) {
			final String refString = entry.getId().toString();
			requestorMap.put(refString, requestor);
		}
	}

	public void setInteractionRecorder(InteractionRecorder interactionRecorder) {
		this.interactionRecorder = interactionRecorder;
	}

	public void setInteractionPreference(InteractionPreference interactionPreference) {
		this.interactionPreference = interactionPreference;
	}

	@Override
	protected InteractionPreference getInteractionPreference() {
		return this.interactionPreference;
	}

}
