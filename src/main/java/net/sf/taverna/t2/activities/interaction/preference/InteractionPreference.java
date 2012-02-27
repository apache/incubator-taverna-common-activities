/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.preference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

/**
 * @author alanrw
 *
 */
public class InteractionPreference {
	
	private static final String PORT = "port";

	private static final String DEFAULT_PORT = "8080";

	private static final String WORKING_DIRECTORY = "workingDirectory";
	
	private static final String DEFAULT_WORKING_DIRECTORY = "/tmp";

	private static final String PRESENTATION_DIRECTORY = "presentationDirectory";
	
	private static final String DEFAULT_PRESENTATION_DIRECTORY = "/tmp/interaction";
	
	private static final String FEED_DIRECTORY = "feedDirectory";
	
	private static final String DEFAULT_FEED_DIRECTORY = "/tmp/fs";

	private static final String HOSTNAME = "hostname";

	private static final String DEFAULT_HOSTNAME = "localhost";

	private Logger logger = Logger.getLogger(InteractionPreference.class);
	
	private static InteractionPreference instance = null;
	
	private Properties properties;
	
	public static InteractionPreference getInstance() {
		if (instance == null) {
			instance = new InteractionPreference();
		}
		return instance;
	}
	
	private File getConfigFile() {
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File config = new File(home,"conf");
		if (!config.exists()) {
			config.mkdir();
		}
		File configFile = new File(config,
				this.getFilePrefix()+"-"+this.getUUID() + ".config");
		return configFile;
	}
	
	private InteractionPreference() {
		File configFile = getConfigFile();
		properties = new Properties();
		if (configFile.exists()) {
			try {
				FileReader reader = new FileReader(configFile);
				properties.load(reader);
				reader.close();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		fillDefaultProperties();
	}

	private void fillDefaultProperties() {
		if (!properties.containsKey(PORT)) {
			properties.setProperty(PORT, DEFAULT_PORT);
		}
		if (!properties.containsKey(WORKING_DIRECTORY)) {
			properties.setProperty(WORKING_DIRECTORY, DEFAULT_WORKING_DIRECTORY);
		}
		if (!properties.containsKey(FEED_DIRECTORY)) {
			properties.setProperty(FEED_DIRECTORY, DEFAULT_FEED_DIRECTORY);
		}
		if (!properties.containsKey(PRESENTATION_DIRECTORY)) {
			properties.setProperty(PRESENTATION_DIRECTORY, DEFAULT_PRESENTATION_DIRECTORY);
		}
		if (!properties.containsKey(HOSTNAME)) {
			properties.setProperty(HOSTNAME, DEFAULT_HOSTNAME);
		}
	}

	public String getFilePrefix() {
		return "Interaction";
	}
	
	public void store() {
		try {
			FileOutputStream out = new FileOutputStream(getConfigFile());
			properties.store(out, "");
			out.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
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

	public void setPresentationDirectory(String text) {
		properties.setProperty(PRESENTATION_DIRECTORY, text);
	}

	public void setHostname(String text) {
		properties.setProperty(HOSTNAME, text);
	}

	public String getPort() {
		return properties.getProperty(PORT);
	}

	public String getPresentationDirectory() {
		return properties.getProperty(PRESENTATION_DIRECTORY);
	}

	public String getWorkingDirectory() {
		return properties.getProperty(WORKING_DIRECTORY);
	}

	public String getFeedDirectory() {
		return properties.getProperty(FEED_DIRECTORY);
	}

	public String getHostname() {
		return properties.getProperty(HOSTNAME);
	}

	public String getFeedUrl() {
		return "http://" + getHostname() + ":" + getPort() + "/feed";
	}

	public String getLocationUrl() {
		return "http://" + getHostname() + ":" + getPort() + "/interaction";
	}
	
	

}
