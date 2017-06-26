package org.apache.taverna.cwl.utilities.preprocessing;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Created by maanadev on 6/26/17.
 */
public class JettyFileServer {
    final private static Logger logger = Logger.getLogger(JettyFileServer.class);

    static Server server;
    public static Server startServer() {
         server = new Server(0);


        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(JettyFileServer.class.getResource("/preprocessing/serverContent/").getPath());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            logger.error("Cannot start Jetty server!",e);
        }

        return server;

    }
    public static int getPort(){
        return server.getURI().getPort();
    }

    public static void stopServer(){
        try {
            server.stop();
        } catch (Exception e) {
            logger.error("Cannot stop Jetty Server",e);
        }
    }
}
