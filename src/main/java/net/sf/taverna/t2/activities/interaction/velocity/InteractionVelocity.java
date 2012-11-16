/**
 *
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

/**
 * @author alanrw
 *
 */
public class InteractionVelocity {

	public static Logger logger = Logger.getLogger(InteractionVelocity.class);

	private static boolean velocityInitialized = false;

	private static String TEMPLATE_SUFFIX = ".vm";

	private static Template interactionTemplate = null;
	private static String INTERACTION_TEMPLATE_NAME = "interaction";

	private static ArrayList<String> templateNames = new ArrayList<String>();

	public static void checkVelocity () {
		if (velocityInitialized) {
			return;
		}
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
		Velocity
				.setProperty("resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
			      "org.apache.velocity.runtime.log.Log4JLogChute");
		Velocity.setProperty("runtime.log.logsystem.log4j.logger",
                "net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity");
		Velocity.init();
		RuntimeSingleton.getRuntimeInstance().addDirective(
				new RequireDirective());
		RuntimeSingleton.getRuntimeInstance().addDirective(
				new ProduceDirective());
		velocityInitialized = true;

		loadTemplates();

		interactionTemplate = Velocity.getTemplate(INTERACTION_TEMPLATE_NAME);
		if (interactionTemplate == null) {
			logger.error("Could not open interaction template " + INTERACTION_TEMPLATE_NAME);
		}
	}

	private static void loadTemplates() {
		final InputStream is = InteractionActivity.class.getResourceAsStream("/index");
		if (is == null) {
			logger.error("Unable to read /index");
			return;
		}
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					if (line.startsWith("#")) {
						continue;
					}
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					final String templatePath = line + TEMPLATE_SUFFIX;
					logger.info("Looking for " + templatePath);
					final StringResourceRepository repo = StringResourceLoader.getRepository();
					try {
						repo.putStringResource(line, getTemplateFromResource(templatePath));
					}
					catch (final IOException e) {
						logger.error("Failed reading template from " + templatePath, e);
					}
				    final Template t = Velocity.getTemplate(line);
				    if (t == null) {
				    	logger.error("Registration failed");
				    }
				    if (!line.equals(INTERACTION_TEMPLATE_NAME)) {
				    	templateNames.add(line);
				    }
				}
			} catch (final IOException e) {
				logger.error("Failed reading template index", e);
			}
	}

	public static Template getInteractionTemplate() {
		checkVelocity();
		return interactionTemplate;
	}

	private static String getTemplateFromResource(final String templatePath) throws IOException {
		checkVelocity();
	        final InputStream stream = InteractionVelocity.class.getResourceAsStream("/" + templatePath);
	        final String result = IOUtils.toString(stream, "UTF-8");
	        return result;
	}

	public static ArrayList<String> getTemplateNames() {
		checkVelocity();
		return templateNames;
	}
}
