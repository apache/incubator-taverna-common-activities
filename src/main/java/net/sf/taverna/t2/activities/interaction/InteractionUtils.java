/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import static java.lang.System.getProperty;
import static net.sf.taverna.t2.activities.interaction.InteractionRecorder.addResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author alanrw
 * 
 */
public class InteractionUtils {
	static final Set<String> publishedUrls = Collections
			.synchronizedSet(new HashSet<String>());
	private static volatile InteractionPreference prefs;

	private InteractionUtils() {
		super();
	}

	protected static void copyFixedFile(String fixedFileName)
			throws IOException {
		if (prefs == null)
			prefs = InteractionPreference.getInstance();
		publishFile(
				prefs.getLocationUrl(true) + "/" + fixedFileName,
				InteractionActivity.class.getResourceAsStream("/"
						+ fixedFileName), null, null);
	}

	public static void publishFile(String urlString, String contents,
			String runId, String interactionId) throws IOException {
		publishFile(urlString,
				new ByteArrayInputStream(contents.getBytes("UTF-8")), runId,
				interactionId);
	}

	static void publishFile(String urlString, InputStream is, String runId,
			String interactionId) throws IOException {
		if (publishedUrls.contains(urlString)) {
			return;
		}
		publishedUrls.add(urlString);
		if (runId != null) {
			addResource(runId, interactionId, urlString);
		}

		HttpURLConnection httpCon = (HttpURLConnection) new URL(urlString)
				.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		try (OutputStream outputStream = httpCon.getOutputStream()) {
			IOUtils.copy(is, outputStream);
		} finally {
			is.close();
		}
		int code = httpCon.getResponseCode();
		if ((code >= 400) || (code < 0)) {
			throw new IOException("Received code " + code);
		}
	}

	public static String getUsedRunId(String engineRunId) {
		String runId = engineRunId;
		String specifiedId = getProperty("taverna.runid");
		if (specifiedId != null) {
			runId = specifiedId;
		}
		return runId;
	}

	public static File getInteractionServiceDirectory() {
		File interactionServiceDirectory = new File(ApplicationRuntime
				.getInstance().getApplicationHomeDir(), "interactionService");
		interactionServiceDirectory.mkdirs();
		return interactionServiceDirectory;
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String objectToJson(Object o) throws IOException {
		StringWriter sw = new StringWriter();
		mapper.writeValue(sw, o);
		return sw.toString();
	}
}
