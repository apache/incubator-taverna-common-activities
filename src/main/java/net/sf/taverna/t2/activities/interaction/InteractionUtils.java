/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

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

	private InteractionUtils() {
		super();
	}

	protected static void copyJavaScript(final String javascriptFileName)
			throws IOException {
		final String targetUrl = InteractionPreference.getInstance()
				.getLocationUrl() + "/" + javascriptFileName;
		InteractionUtils.publishFile(
				targetUrl,
				InteractionActivity.class.getResourceAsStream("/"
						+ javascriptFileName), null, null);
	}

	public static void publishFile(final String urlString,
			final String contents, final String runId,
			final String interactionId) throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				contents.getBytes());
		InteractionUtils.publishFile(urlString, byteArrayInputStream, runId,
				interactionId);
	}

	static void publishFile(final String urlString, final InputStream is,
			final String runId, final String interactionId) throws IOException {
		if (InteractionUtils.publishedUrls.contains(urlString)) {
			return;
		}
		InteractionUtils.publishedUrls.add(urlString);
		if (runId != null) {
			InteractionRecorder.addResource(runId, interactionId, urlString);
		}

		final URL url = new URL(urlString);
		final HttpURLConnection httpCon = (HttpURLConnection) url
				.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		final OutputStream outputStream = httpCon.getOutputStream();
		IOUtils.copy(is, outputStream);
		is.close();
		outputStream.close();
		httpCon.getResponseCode();
	}

	public static String getUsedRunId(final String engineRunId) {
		String runId = engineRunId;
		final String specifiedId = System.getProperty("taverna.runid");
		if (specifiedId != null) {
			runId = specifiedId;
		}
		return runId;
	}

	public static File getInteractionServiceDirectory() {
		final File workingDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		final File interactionServiceDirectory = new File(workingDir,
				"interactionService");
		interactionServiceDirectory.mkdirs();
		return interactionServiceDirectory;
	}

	public static String objectToJson(final Object o) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final StringWriter sw = new StringWriter();
		mapper.writeValue(sw, o);
		final String theString = sw.toString();
		return theString;
	}
}
