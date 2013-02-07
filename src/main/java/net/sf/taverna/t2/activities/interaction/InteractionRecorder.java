/**
 * 
 */
package net.sf.taverna.t2.activities.interaction;

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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * This class is used to remember and forget interactions and their associated ATOM entries and files
 * @author alanrw
 *
 */
public class InteractionRecorder {
	
	private static final Logger logger = Logger.getLogger(InteractionRecorder.class);

	static Map <String, Map<String, Set<String>>> runToInteractionMap =
	Collections.synchronizedMap(new HashMap<String, Map<String, Set<String>>> ());
	
	private InteractionRecorder() {
		super();
	}
	
	public static void deleteRun(String runToDelete) {
		Set<String> interactionIds = new HashSet<String>(getInteractionMap(runToDelete).keySet());
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
				logger.error("Unable to delete " + urlString, e);
			}
			
		}
		getInteractionMap(runId).remove(interactionId);
	}

	private static void deleteUrl(String urlString) throws IOException {
		logger.info("Deleting resource " + urlString);
		URL url = new URL(urlString);
		final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestMethod("DELETE");
		int response = httpCon.getResponseCode();
		if (response >= 400) {
			logger.error("Received response code" + response);
		}
	}
	
	public static void addResource(String runId, String interactionId, String resourceId) {
		if (resourceId == null) {
			logger.error("Attempt to add null resource", new NullPointerException(""));
			return;
		}
		logger.info("Adding resource " + resourceId);
		Set<String> resourceSet = getResourceSet(runId, interactionId);
		
		resourceSet.add (resourceId);
	}

	private static Set<String> getResourceSet(String runId, String interactionId) {
		Map<String, Set<String>> interactionMap = getInteractionMap(runId);
		Set<String> resourceSet = interactionMap.get(interactionId);
		if (resourceSet == null) {
			resourceSet = Collections.synchronizedSet(new HashSet<String>());
			interactionMap.put(interactionId, resourceSet);
		}
		return resourceSet;
	}

	private static Map<String, Set<String>> getInteractionMap(String runId) {
		Map<String, Set<String>> interactionMap = InteractionRecorder.runToInteractionMap.get(runId);
		if (interactionMap == null) {
			interactionMap = Collections.synchronizedMap(Collections.synchronizedMap(new HashMap<String, Set<String>>()));
			InteractionRecorder.runToInteractionMap.put(runId, interactionMap);
		}
		return interactionMap;
	}
	
	public static void persist() {
		File outputFile = getUsageFile();
		try {
			FileUtils.writeStringToFile(outputFile, InteractionUtils.objectToJson(InteractionRecorder.runToInteractionMap));
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private static File getUsageFile() {
		return new File(InteractionUtils.getInteractionServiceDirectory(), "usage");
	}

	public static void load() {
		File inputFile = getUsageFile();
		try {
			String usageString = FileUtils.readFileToString(inputFile);
			final ObjectMapper mapper = new ObjectMapper();
			final Map<String,Object> rootAsMap = mapper.readValue(usageString, Map.class);
			InteractionRecorder.runToInteractionMap.clear();
			for (String runId : rootAsMap.keySet()) {
				Map<String, Object> runMap = (Map<String, Object>) rootAsMap.get(runId);
				for (String interactionId : runMap.keySet()) {
					List<String> urlList = (List<String>) runMap.get(interactionId);
					for (String url : urlList) {
						addResource(runId, interactionId, url);
					}
				}
			}
		} catch (IOException e) {
			logger.info(e);
		}
	}


}
