/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.security.oauth.OAuthLoginSite;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.webdav.WebdavServlet;

import org.apache.abdera.protocol.server.ServiceManager;
import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.abdera.protocol.server.adapters.filesystem.FilesystemAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

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
	
    private static InteractionPreference interactionPreference = InteractionPreference.getInstance();
    
	private static SPIRegistry<JettyStartupHook> registry = new SPIRegistry<JettyStartupHook> (JettyStartupHook.class);
	
	private static String REALM_NAME = "TavernaInteraction";


	public static synchronized void checkJetty() {
		if (server != null) {
			return;
		}
		ClassLoader previousContextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		Thread.currentThread().setContextClassLoader(
				InteractionJetty.class.getClassLoader());

		server = new Server();
		server.setStopAtShutdown(true);
		SelectChannelConnector connector = new SelectChannelConnector();
		String port = interactionPreference.getPort();
		connector.setPort(Integer.parseInt(port));
		server.addConnector(connector);

		WebdavServlet interactionServlet = new WebdavServlet();

		ServletHolder interactionHolder = new ServletHolder(interactionServlet);
		try {
			interactionHolder.setInitParameter("rootpath",
					getInteractionDirectory().getCanonicalPath());
		} catch (IOException e1) {
			logger.error("Unable to set root of interaction", e1);
		}

		HandlerList handlers = new HandlerList();
		Context overallContext = new Context(handlers, "/", Context.SESSIONS);

		AbderaServlet abderaServlet = new AbderaServlet();
		ServletHolder abderaHolder = new ServletHolder(abderaServlet);
		abderaHolder.setInitParameter(ServiceManager.PROVIDER,
				BasicProvider.class.getName());

		overallContext.addServlet(abderaHolder, "/*");
		overallContext.addServlet(interactionHolder, "/interaction/*");

		if (interactionPreference.getUseUsername()) {
			Constraint constraint = new Constraint();
			constraint.setName(Constraint.__BASIC_AUTH);
			;
			constraint.setRoles(new String[] { "user", "admin", "moderator" });
			constraint.setAuthenticate(true);

			ConstraintMapping cm = new ConstraintMapping();
			cm.setConstraint(constraint);
			cm.setPathSpec("/*");

			SecurityHandler sh = new SecurityHandler();
			try {
				HashUserRealm realm = new HashUserRealm(REALM_NAME);
				URI serviceURI = createServiceURI(port);
				UsernamePassword up = CredentialManager
						.getInstance()
						.getUsernameAndPasswordForService(
								serviceURI, true,
								"Please specify the username and password to secure your interactions");
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
			sh.setConstraintMappings(new ConstraintMapping[] { cm });
			overallContext.addHandler(sh);

		}

		getFeedDirectory();

		handlers.setHandlers(new Handler[] { overallContext,
				new DefaultHandler() });
		server.setHandler(handlers);

		try {
			server.start();
			while (!server.isRunning()) {
				Thread.sleep(5000);
			}
			
			for (JettyStartupHook h : registry.getInstances()) {
				h.jettyStarted();
			}
			Thread.sleep(5000);
		} catch (Exception e) {
			logger.error("Unable to start Jetty");
		}
		Thread.currentThread()
				.setContextClassLoader(previousContextClassLoader);
	}

	public static URI createServiceURI(String port) throws URISyntaxException {
		return new URI("http://localhost:" + port + "/#"
				+ REALM_NAME);
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
