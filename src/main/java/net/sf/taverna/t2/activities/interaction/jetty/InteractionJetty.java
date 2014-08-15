/**
 *
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;
import static net.sf.taverna.t2.activities.interaction.InteractionUtils.getInteractionServiceDirectory;
import static org.apache.abdera.protocol.server.ServiceManager.PROVIDER;
import static org.mortbay.jetty.servlet.Context.SESSIONS;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.taverna.t2.activities.interaction.FeedReader;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.webdav.WebdavServlet;

import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author alanrw
 * 
 */
public class InteractionJetty {
	private static final String REALM_NAME = "TavernaInteraction";
	private static Logger logger = Logger.getLogger(InteractionJetty.class);

	private static Server server;
	private static boolean listenersStarted = false;

	private static InteractionPreference interactionPreference = InteractionPreference
			.getInstance();
	private static SPIRegistry<FeedReader> feedReaderRegistry = new SPIRegistry<>(
			FeedReader.class);

	public static synchronized void startJettyIfNecessary() {
		if (server != null) {
			return;
		}

		// final ClassLoader previousContextClassLoader = Thread.currentThread()
		// .getContextClassLoader();
		// Thread.currentThread().setContextClassLoader(
		// InteractionJetty.class.getClassLoader());

		String port = interactionPreference.getPort();
		server = new Server(parseInt(port));
		server.setStopAtShutdown(true);

		ServletHolder interactionHolder = new ServletHolder();
		interactionHolder.setServlet(new WebdavServlet());

		try {
			interactionHolder.setInitParameter("rootpath",
					getInteractionDirectory().getCanonicalPath());
		} catch (IOException e1) {
			logger.error("Unable to set root of interaction", e1);
		}

		Context overallContext = new Context(new HandlerList(), "/", SESSIONS);
		overallContext.setContextPath("/");
		server.setHandler(overallContext);

		ServletHolder abderaHolder = new ServletHolder(new AbderaServlet());
		abderaHolder.setInitParameter(PROVIDER, BasicProvider.class.getName());

		overallContext.addServlet(abderaHolder, "/*");
		overallContext.addServlet(interactionHolder, "/interaction/*");

		if (interactionPreference.getUseUsername()) {
			applyAccessCredentials(port, overallContext);
		}

		getFeedDirectory();

		try {
			server.start();
			while (!server.isRunning()) {
				sleep(5000);
			}
		} catch (Exception e) {
			logger.error("Unable to start Jetty");
		}
		// Thread.currentThread()
		// .setContextClassLoader(previousContextClassLoader);
	}

	private static void applyAccessCredentials(String port,
			Context overallContext) {
		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);

		constraint.setRoles(new String[] { "user", "admin", "moderator" });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");

		SecurityHandler sh = new SecurityHandler();
		try {
			HashUserRealm realm = new HashUserRealm(REALM_NAME);
			UsernamePassword up = CredentialManager
					.getInstance()
					.getUsernameAndPasswordForService(createServiceURI(port),
							true,
							"Please specify the username and password to secure your interactions");
			if (up != null) {
				String username = up.getUsername();
				realm.put(username, up.getPasswordAsString());
				realm.addUserToRole(username, "user");
			}
			sh.setUserRealm(realm);
		} catch (CMException | URISyntaxException e) {
			logger.error(e);
		}
		sh.setConstraintMappings(new ConstraintMapping[] { cm });
		overallContext.addHandler(sh);
	}

	public static URI createServiceURI(String port) throws URISyntaxException {
		return new URI("http://localhost:" + port + "/#" + REALM_NAME);
	}

	public static File getJettySubdirectory(String subdirectoryName) {
		File subDir = new File(getInteractionServiceDirectory(), "jetty/"
				+ subdirectoryName);
		subDir.mkdirs();
		return subDir;
	}

	public static File getFeedDirectory() {
		return getJettySubdirectory("feed");
	}

	public static File getInteractionDirectory() {
		return getJettySubdirectory("interaction");
	}

	public static synchronized void startListenersIfNecessary() {
		if (listenersStarted) {
			return;
		}
		for (FeedReader fr : feedReaderRegistry.getInstances()) {
			if (fr != null) {
				try {
					fr.start();
				} catch (Exception e) {
					logger.error("Failed to start "
							+ fr.getClass().getCanonicalName(), e);
				}
			}
		}
		listenersStarted = true;
	}
}
