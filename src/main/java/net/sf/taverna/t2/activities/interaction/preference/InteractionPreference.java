/**
 *
 */
package net.sf.taverna.t2.activities.interaction.preference;

import static java.awt.GraphicsEnvironment.isHeadless;
import static java.lang.Boolean.getBoolean;
import static java.lang.System.getProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class InteractionPreference {

	private static final String USE_JETTY = "useJetty";
	private static final String DEFAULT_USE_JETTY = "true";

	private static final String PORT = "port";
	private static final String DEFAULT_PORT = "8080";

	private static final String HOST = "host";
	private static final String DEFAULT_HOST = "http://localhost";

	private static final String WEBDAV_PATH = "webdavPath";
	private static final String DEFAULT_WEBDAV_PATH = "/interaction";

	private static final String FEED_PATH = "feedPath";
	private static final String DEFAULT_FEED_PATH = "/feed";

	private static final String USE_USERNAME = "Secure with username / password";
	private static final String DEFAULT_USE_USERNAME = "false";

	// private static final String USE_HTTPS = "Use HTTPS";
	// private static final String DEFAULT_USE_HTTPS = "false";

	private final Logger logger = Logger.getLogger(InteractionPreference.class);

	private static InteractionPreference instance = null;

	private final Properties properties;

	/**
	 * If not <tt>null</tt>, a replacement in the form "
	 * <tt>http://foo.example.com:12345</tt>" to use to configure where exactly
	 * to talk to for access to the WebDAV and Atom feed. Needed for the case
	 * where an odd network configuration is present, such as inside a Docker
	 * VM. (See T3-809 for more details.)
	 * <p>
	 * Note that this effectively allows overriding of the protocol, host and
	 * port used, but not the path.
	 */
	private String publishAddressOverride;

	public static InteractionPreference getInstance() {
		if (instance == null) {
			instance = new InteractionPreference();
		}
		return instance;
	}

	private File getConfigFile() {
		File config = new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), "conf");
		if (!config.exists()) {
			config.mkdir();
		}
		return new File(config, getFilePrefix() + "-" + getUUID() + ".config");
	}

	private InteractionPreference() {
		File configFile = getConfigFile();
		properties = new Properties();
		if (configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				properties.load(reader);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		if (isHeadless() || getBoolean("java.awt.headless")) {
			String definedHost = getProperty("taverna.interaction.host");
			publishAddressOverride = getProperty("taverna.interaction.publishAddressOverride");
			if (definedHost != null) {
				properties.setProperty(USE_JETTY, "false");
				logger.info("USE_JETTY set to false");
				properties.setProperty(HOST, definedHost);
			}
			String definedPort = getProperty("taverna.interaction.port");
			if (definedPort != null) {
				properties.setProperty(PORT, definedPort);
			}
			String definedWebDavPath = getProperty("taverna.interaction.webdav_path");
			if (definedWebDavPath != null) {
				properties.setProperty(WEBDAV_PATH, definedWebDavPath);
			}
			String definedFeedPath = getProperty("taverna.interaction.feed_path");
			if (definedFeedPath != null) {
				properties.setProperty(FEED_PATH, definedFeedPath);
			}
		} else {
			logger.info("Running non-headless");
		}
		fillDefaultProperties();
	}

	private void fillDefaultProperties() {
		if (!properties.containsKey(USE_JETTY)) {
			properties.setProperty(USE_JETTY, DEFAULT_USE_JETTY);
			logger.info("USE_JETTY set to " + DEFAULT_USE_JETTY);
		}
		if (!properties.containsKey(PORT)) {
			properties.setProperty(PORT, DEFAULT_PORT);
		}
		if (!properties.containsKey(HOST)) {
			properties.setProperty(HOST, DEFAULT_HOST);
		}
		if (!properties.containsKey(WEBDAV_PATH)) {
			properties.setProperty(WEBDAV_PATH, DEFAULT_WEBDAV_PATH);
		}
		if (!properties.containsKey(FEED_PATH)) {
			properties.setProperty(FEED_PATH, DEFAULT_FEED_PATH);
		}
		if (!properties.containsKey(USE_USERNAME)) {
			properties.setProperty(USE_USERNAME, DEFAULT_USE_USERNAME);
		}
		/*
		 * if (!properties.containsKey(USE_HTTPS)) {
		 * properties.setProperty(USE_HTTPS, DEFAULT_USE_HTTPS); }
		 */
	}

	public String getFilePrefix() {
		return "Interaction";
	}

	public void store() {
		try (FileOutputStream out = new FileOutputStream(getConfigFile())) {
			properties.store(out, "");
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public String getUUID() {
		return "DA992717-5A46-469D-AE25-883F0E4CD348";
	}

	public void setPort(String text) {
		properties.setProperty(PORT, text);
	}

	public void setHost(String text) {
		properties.setProperty(HOST, text);
	}

	public void setUseJetty(boolean use) {
		properties.setProperty(USE_JETTY, Boolean.toString(use));
	}

	public void setFeedPath(String path) {
		properties.setProperty(FEED_PATH, path);
	}

	public void setWebDavPath(String path) {
		properties.setProperty(WEBDAV_PATH, path);
	}

	public String getPort() {
		return properties.getProperty(PORT);
	}

	public String getHost() {
		return properties.getProperty(HOST);
	}

	public boolean getUseJetty() {
		return Boolean.parseBoolean(properties.getProperty(USE_JETTY));
	}

	public String getFeedPath() {
		return properties.getProperty(FEED_PATH);
	}

	public String getWebDavPath() {
		return properties.getProperty(WEBDAV_PATH);
	}

	public String getDefaultHost() {
		return DEFAULT_HOST;
	}

	public String getDefaultFeedPath() {
		return DEFAULT_FEED_PATH;
	}

	public String getDefaultWebDavPath() {
		return DEFAULT_WEBDAV_PATH;
	}

	private String getAuthority(boolean forNetworkActivity) {
		if (forNetworkActivity && publishAddressOverride != null)
			return publishAddressOverride;
		return getHost() + ":" + getPort();
	}

	public String getFeedUrlString(boolean forNetworkActivity) {
		return getAuthority(forNetworkActivity) + getFeedPath();
	}

	public String getLocationUrl(boolean forNetworkActivity) {
		return getAuthority(forNetworkActivity) + getWebDavPath();
	}

	public boolean getUseUsername() {
		return Boolean.parseBoolean(properties.getProperty(USE_USERNAME));
	}

	public void setUseUsername(boolean useUsername) {
		properties.setProperty(USE_USERNAME, Boolean.toString(useUsername));
	}

	public static String getOutputDataUrlString(boolean forNetworkActivity, String interactionId) {
		return InteractionPreference.getInstance().getLocationUrl(forNetworkActivity)
				+ "/interaction" + interactionId + "OutputData.json";
	}

	public static String getInputDataUrlString(boolean forNetworkActivity, String interactionId) {
		return InteractionPreference.getInstance().getLocationUrl(forNetworkActivity)
				+ "/interaction" + interactionId + "InputData.json";
	}

	public static URL getFeedUrl(boolean forNetworkActivity) throws MalformedURLException {
		return new URL(InteractionPreference.getInstance().getFeedUrlString(forNetworkActivity));
	}

	public static String getInteractionUrlString(boolean forNetworkActivity, String interactionId) {
		return InteractionPreference.getInstance().getLocationUrl(forNetworkActivity)
				+ "/interaction" + interactionId + ".html";
	}

	public static String getPresentationUrlString(boolean forNetworkActivity, String interactionId) {
		return InteractionPreference.getInstance().getLocationUrl(forNetworkActivity)
				+ "/presentation" + interactionId + ".html";
	}

	public static String getPublicationUrlString(boolean forNetworkActivity, String interactionId,
			String key) {
		return InteractionPreference.getInstance().getLocationUrl(forNetworkActivity)
				+ "/interaction" + interactionId + "_" + key;
	}
}
