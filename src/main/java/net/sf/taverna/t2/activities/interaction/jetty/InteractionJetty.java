/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.activities.interaction.velocity.InteractionVelocity;
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
    connector.setPort(Integer.parseInt(InteractionPreference.getInstance().getPort()));
    server.addConnector(connector);
    
    
    WebdavServlet interactionServlet = new WebdavServlet();
    
    ServletHolder interactionHolder = new ServletHolder(interactionServlet);
    interactionHolder.setInitParameter("rootpath", "/tmp/interaction");
    
    HandlerList handlers = new HandlerList();
    Context overallContext = new Context(handlers, "/", Context.SESSIONS);

    AbderaServlet abderaServlet = new AbderaServlet();
    ServletHolder abderaHolder = new ServletHolder(abderaServlet);
    abderaHolder.setInitParameter(ServiceManager.PROVIDER, BasicProvider.class.getName());

    overallContext.addServlet(abderaHolder,"/*");
    overallContext.addServlet(interactionHolder, "/interaction/*");
    createDirectory("/tmp/fs");
    
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

}
