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
import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;

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
		Velocity.setProperty(Velocity.RESOURCE_LOADER, "string");
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
		InputStream is = InteractionActivity.class.getResourceAsStream("/index");
		if (is == null) {
			logger.error("Unable to reading /index");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					if (line.startsWith("#")) {
						continue;
					}
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					String templatePath = line + TEMPLATE_SUFFIX;
					logger.error("Looking for " + templatePath);
					StringResourceRepository repo = StringResourceLoader.getRepository();
					try {
						repo.putStringResource(line, getTemplateFromResource(templatePath));
					}
					catch (IOException e) {
						logger.error("Failed reading template from " + templatePath, e);
					}
				    Template t = Velocity.getTemplate(line);
				    if (t == null) {
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

	public static Template getInteractionTemplate() {
		checkVelocity();
		return interactionTemplate;
	}
	
	private static Template getTemplate(final String templatePath) throws IOException {
		checkVelocity();
	    if (!Velocity.resourceExists(templatePath)) {
	        StringResourceRepository repo = StringResourceLoader.getRepository();
	        repo.putStringResource(templatePath, getTemplateFromResource(templatePath));
	    }
	    return Velocity.getTemplate(templatePath);
	}
	
	private static String getTemplateFromResource(final String templatePath) throws IOException {
		checkVelocity();
	        InputStream stream = InteractionVelocity.class.getResourceAsStream("/" + templatePath);
	        String result = IOUtils.toString(stream, "UTF-8");
	        return result;
	}

	public static ArrayList<String> getTemplateNames() {
		checkVelocity();
		return templateNames;
	}
}
