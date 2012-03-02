/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import java.io.File;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.protocol.server.ServiceManager;
import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
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
		Thread.currentThread().setContextClassLoader(InteractionJetty.class.getClassLoader());
		
		server = new Server();
		server.setStopAtShutdown(true);
			SelectChannelConnector connector = new SelectChannelConnector();
	        connector.setPort(Integer.parseInt(InteractionPreference.getInstance().getPort()));
	        server.addConnector(connector);
	        
	        ContextHandler interactionContext = new ContextHandler();
	        interactionContext.setContextPath("/interaction");
	        interactionContext.setResourceBase(".");
	        interactionContext.setClassLoader(Thread.currentThread().getContextClassLoader());
	        
		ResourceHandler presentationHandler = new ResourceHandler();
		presentationHandler.setResourceBase(InteractionPreference.getInstance().getPresentationDirectory());
		interactionContext.setHandler(presentationHandler);
		
		HandlerList handlers = new HandlerList();
		Context abderaContext = new Context(handlers, "/", Context.SESSIONS);
 
        AbderaServlet abderaServlet = new AbderaServlet();
		ServletHolder servletHolder = new ServletHolder(abderaServlet);
		servletHolder.setInitParameter(ServiceManager.PROVIDER, BasicProvider.class.getName());
        
		abderaContext.addServlet(servletHolder,"/*");
		
       handlers.setHandlers(new Handler[] { interactionContext, abderaContext, new DefaultHandler() });
        server.setHandler(handlers);
        
        createDirectories();
		
			try {
				server.start();
				while (!server.isRunning()) {
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				logger.error("Unable to start Jetty");
			}
	}


	private static void createDirectories() {
		createDirectory(InteractionPreference.getInstance().getWorkingDirectory());
		createDirectory(InteractionPreference.getInstance().getPresentationDirectory());
		createDirectory(InteractionPreference.getInstance().getFeedDirectory());
		
	}


	private static void createDirectory(String dirPath) {
		File dir = new File(dirPath);
		dir.mkdirs();
	}

}
