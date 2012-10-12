/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.webdav.WebdavServlet;

import org.apache.abdera.protocol.server.ServiceManager;
import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.abdera.protocol.server.adapters.filesystem.FilesystemAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.component.AbstractLifeCycle;

/**
 * @author alanrw
 *
 */
public class InteractionJetty {

	private static Logger logger = Logger.getLogger(InteractionJetty.class);

	private static Server server;
	
	public static synchronized void checkJetty() {
		if (server != null) {
            return;
    }
		ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(InteractionJetty.class.getClassLoader());
    
    server = new Server();
    server.setStopAtShutdown(true);
            SelectChannelConnector connector = new SelectChannelConnector();
    String port = InteractionPreference.getInstance().getPort();
	connector.setPort(Integer.parseInt(port));
    server.addConnector(connector);
    
    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);;
    constraint.setRoles(new String[]{"user","admin","moderator"});
    constraint.setAuthenticate(true);
     
    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");
     
    SecurityHandler sh = new SecurityHandler();
    try {
		String realmName = "TavernaInteraction";
		HashUserRealm realm = new HashUserRealm(realmName);
		UsernamePassword up = CredentialManager.getInstance().getUsernameAndPasswordForService(new URI("http://localhost:" + port + "/#" + realmName),
				true, "Please specify the username and password to secure your interactions");
		if (up != null) {
			String username = up.getUsername();
			realm.put(username, up.getPasswordAsString());
			realm.addUserToRole(username, "user");
		}
		sh.setUserRealm(realm);
	} catch (CMException e) {
		logger.error(e);
	} catch (URISyntaxException e) {
		logger.error(e);
	}
    sh.setConstraintMappings(new ConstraintMapping[]{cm});
    
    WebdavServlet interactionServlet = new WebdavServlet();
    
    ServletHolder interactionHolder = new ServletHolder(interactionServlet);
    try {
		interactionHolder.setInitParameter("rootpath", getInteractionDirectory().getCanonicalPath());
	} catch (IOException e1) {
		logger.error("Unable to set root of interaction", e1);
	}
    
    HandlerList handlers = new HandlerList();
    Context overallContext = new Context(handlers, "/", Context.SESSIONS);

    AbderaServlet abderaServlet = new AbderaServlet();
    ServletHolder abderaHolder = new ServletHolder(abderaServlet);
    abderaHolder.setInitParameter(ServiceManager.PROVIDER, BasicProvider.class.getName());

    overallContext.addServlet(abderaHolder,"/*");
    overallContext.addServlet(interactionHolder, "/interaction/*");
    overallContext.addHandler(sh);
    getFeedDirectory();
    
    handlers.setHandlers(new Handler[] { overallContext, new DefaultHandler() });
    server.setHandler(handlers);
    
            try {
                    server.start();
                    while (!server.isRunning()) {
                            Thread.sleep(5000);
                    }
            } catch (Exception e) {
                    logger.error("Unable to start Jetty");
            }
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
	}

	private static void createDirectory(String dirPath) {
		File dir = new File(dirPath);
		dir.mkdirs();
	}

	public static File getJettySubdirectory(String subdirectoryName) {
		File workingDir = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File subDir = new File(workingDir, "jetty/" + subdirectoryName);
		subDir.mkdirs();
		return subDir;
	}
	
	public static File getFeedDirectory() {
		return getJettySubdirectory("feed");
	}
	
	public static File getInteractionDirectory() {
		return getJettySubdirectory("interaction");
	}

}
