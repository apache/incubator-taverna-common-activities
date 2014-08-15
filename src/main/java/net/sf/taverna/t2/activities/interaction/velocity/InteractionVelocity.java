/**
 *
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER;
import static org.apache.velocity.runtime.RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS;
import static org.apache.velocity.runtime.resource.loader.StringResourceLoader.getRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

/**
 * @author alanrw
 * 
 */
public class InteractionVelocity {
	public static Logger logger = Logger.getLogger(InteractionVelocity.class);

	private static boolean velocityInitialized = false;

	private static Template interactionTemplate = null;
	private static String INTERACTION_TEMPLATE_NAME = "interaction";
	private static String TEMPLATE_SUFFIX = ".vm";

	private static ArrayList<String> templateNames = new ArrayList<>();

	private static VelocityEngine velocityEngine;

	private static synchronized VelocityEngine getVelocity() {
		if (velocityEngine == null)
			velocityEngine = new VelocityEngine();
		return velocityEngine;
	}

	public static void checkVelocity() {
		if (velocityInitialized) {
			return;
		}
		VelocityEngine velocity = getVelocity();
		velocity.setProperty(RESOURCE_LOADER, "string");
		velocity.setProperty("resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		velocity.setProperty(RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.Log4JLogChute");
		velocity.setProperty("runtime.log.logsystem.log4j.logger",
				"net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity");
		velocity.init();
		velocity.setProperty("userdirective", RequireDirective.class.getName()
				+ "," + ProduceDirective.class.getName() + ","
				+ NotifyDirective.class.getName());
		velocityInitialized = true;

		loadTemplates();

		interactionTemplate = velocity.getTemplate(INTERACTION_TEMPLATE_NAME);
		if (interactionTemplate == null) {
			logger.error("Could not open interaction template "
					+ INTERACTION_TEMPLATE_NAME);
		}
	}

	private static InputStream getResource(String name) {
		return InteractionActivity.class.getResourceAsStream("/" + name);
	}

	private static void loadTemplates() {
		InputStream is = getResource("index");
		if (is == null) {
			logger.error("Unable to read index");
			return;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					continue;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String templatePath = line + TEMPLATE_SUFFIX;
				logger.info("Looking for " + templatePath);
				StringResourceRepository repo = getRepository();
				try {
					repo.putStringResource(line,
							getTemplateFromResource(templatePath));
				} catch (IOException e) {
					logger.error(
							"Failed reading template from " + templatePath, e);
				}
				if (getVelocity().getTemplate(line) == null) {
					logger.error("Registration failed");
				}
				if (!line.equals(INTERACTION_TEMPLATE_NAME)) {
					templateNames.add(line);
				}
			}
		} catch (IOException e) {
			logger.error("Failed reading template index", e);
		}
	}

	public static Template getInteractionTemplate(
			InteractionActivityConfigurationBean configBean) {
		return getVelocity().getTemplate(configBean.getPresentationOrigin());
	}

	public static Template getInteractionTemplate() {
		checkVelocity();
		return interactionTemplate;
	}

	private static String getTemplateFromResource(String templatePath)
			throws IOException {
		checkVelocity();
		return IOUtils.toString(getResource(templatePath), "UTF-8");
	}

	public static ArrayList<String> getTemplateNames() {
		checkVelocity();
		return templateNames;
	}
}
