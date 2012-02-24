/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

/**
 * @author alanrw
 *
 */
public class InteractionVelocity {
	
	private static Logger logger = Logger.getLogger(InteractionVelocity.class);
	
	private static boolean velocityInitialized = false;
	
	private static String TEMPLATE_SUFFIX = ".vm";
	
	private static Template interactionTemplate = null;
	private static String INTERACTION_TEMPLATE_NAME = "interaction";
	
	private static Template communicationTemplate = null;
	private static String COMMUNICATION_TEMPLATE_NAME = "communication";
	
	private static ArrayList<String> templateNames = new ArrayList<String>();

	public static void checkVelocity () {
		if (velocityInitialized) {
			return;
		}
		Velocity.setProperty(Velocity.RESOURCE_LOADER, "string");
		Velocity
				.setProperty("resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		Velocity.init();
		RuntimeSingleton.getRuntimeInstance().addDirective(
				new RequireDirective());
		RuntimeSingleton.getRuntimeInstance().addDirective(
				new ProduceDirective());
		velocityInitialized = true;
		copyJavacript(InteractionActivity.getTempDir(), "pmrpc.js");
		
		loadTemplates();
		communicationTemplate = Velocity
		.getTemplate(COMMUNICATION_TEMPLATE_NAME);
		interactionTemplate = Velocity.getTemplate(INTERACTION_TEMPLATE_NAME);
	}

	private static void loadTemplates() {
		InputStream is = InteractionActivity.class.getResourceAsStream("/index");
		if (is == null) {
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					if (line.startsWith("#")) {
						continue;
					}
					String templatePath = line + TEMPLATE_SUFFIX;
					StringResourceRepository repo = StringResourceLoader.getRepository();
				    repo.putStringResource(line, getTemplateFromResource(templatePath));
				    if (!line.equals(COMMUNICATION_TEMPLATE_NAME) && !line.equals(INTERACTION_TEMPLATE_NAME)) {
				    	templateNames.add(line);
				    }
				}
			} catch (IOException e) {
				logger.error(e);
			}
	}

	private static void copyJavacript(File tempDir2, String javascriptFileName) {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = InteractionActivity.class.getResourceAsStream("/" + javascriptFileName);
			File jsonFile = new File(tempDir2, javascriptFileName);
			fos = new FileOutputStream(jsonFile);
			IOUtils.copy(is, fos);
			is.close();
			fos.close();
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}		
	}

	public static Template getCommunicationTemplate() {
		return communicationTemplate;
	}

	public static Template getInteractionTemplate() {
		return interactionTemplate;
	}
	
	private static Template getTemplate(final String templatePath) {
	    if (!Velocity.resourceExists(templatePath)) {
	        StringResourceRepository repo = StringResourceLoader.getRepository();
	        repo.putStringResource(templatePath, getTemplateFromResource(templatePath));
	    }
	    return Velocity.getTemplate(templatePath);
	}
	
	private static String getTemplateFromResource(final String templatePath) {
	    try {
	        InputStream stream = InteractionVelocity.class.getResourceAsStream("/" + templatePath);
	        return IOUtils.toString(stream, "UTF-8");
	    } catch (IOException ex) {
	        throw new RuntimeException(ex);
	    }
	}

	public static ArrayList<String> getTemplateNames() {
		return templateNames;
	}
}
