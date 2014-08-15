/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.getInteractionServiceDirectory;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.objectToJson;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * This class is used to remember and forget interactions and their associated
 * ATOM entries and files
 * 
 * @author alanrw
 * 
 */
public class InteractionRecorder {
	private static final Logger logger = Logger
			.getLogger(InteractionRecorder.class);
	static Map<String, Map<String, Set<String>>> runToInteractionMap = synchronizedMap(new HashMap<String, Map<String, Set<String>>>());

	private InteractionRecorder() {
		super();
	}

	public static void deleteRun(String runToDelete) {
		Set<String> interactionIds = new HashSet<>(getInteractionMap(
				runToDelete).keySet());
		for (String interactionId : interactionIds) {
			deleteInteraction(runToDelete, interactionId);
		}
		runToInteractionMap.remove(runToDelete);
	}

	public static void deleteInteraction(String runId, String interactionId) {
		for (String urlString : getResourceSet(runId, interactionId)) {
			try {
				deleteUrl(urlString);
			} catch (IOException e) {
				logger.info("Unable to delete " + urlString, e);
			}
		}
		getInteractionMap(runId).remove(interactionId);
	}

	private static void deleteUrl(String urlString) throws IOException {
		logger.info("Deleting resource " + urlString);
		URL url = new URL(urlString);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod("DELETE");
		int response = httpCon.getResponseCode();
		if (response >= 400) {
			logger.info("Received response code " + response
					+ " from deleting " + url);
		}
	}

	public static void addResource(String runId, String interactionId,
			String resourceId) {
		if (resourceId == null) {
			logger.error("Attempt to add null resource",
					new NullPointerException(""));
			return;
		}
		logger.info("Adding resource " + resourceId);
		getResourceSet(runId, interactionId).add(resourceId);
	}

	private static Set<String> getResourceSet(String runId, String interactionId) {
		Map<String, Set<String>> interactionMap = getInteractionMap(runId);
		Set<String> resourceSet = interactionMap.get(interactionId);
		if (resourceSet == null) {
			resourceSet = synchronizedSet(new HashSet<String>());
			interactionMap.put(interactionId, resourceSet);
		}
		return resourceSet;
	}

	private static Map<String, Set<String>> getInteractionMap(String runId) {
		Map<String, Set<String>> interactionMap = runToInteractionMap
				.get(runId);
		if (interactionMap == null) {
			interactionMap = synchronizedMap(Collections
					.synchronizedMap(new HashMap<String, Set<String>>()));
			runToInteractionMap.put(runId, interactionMap);
		}
		return interactionMap;
	}

	public static void persist() {
		File outputFile = getUsageFile();
		try {
			writeStringToFile(outputFile, objectToJson(runToInteractionMap));
		} catch (final IOException e) {
			logger.error(e);
		}
	}

	private static File getUsageFile() {
		return new File(getInteractionServiceDirectory(), "usage");
	}

	public static void load() {
		File inputFile = getUsageFile();
		try {
			String usageString = readFileToString(inputFile);
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Map<String, List<String>>> rootAsMap = mapper
					.readValue(usageString, Map.class);
			runToInteractionMap.clear();
			for (String runId : rootAsMap.keySet()) {
				Map<String, List<String>> runMap = rootAsMap.get(runId);
				for (String interactionId : runMap.keySet()) {
					for (String url : runMap.get(interactionId)) {
						addResource(runId, interactionId, url);
					}
				}
			}
		} catch (IOException e) {
			logger.info(e);
		}
	}
}
