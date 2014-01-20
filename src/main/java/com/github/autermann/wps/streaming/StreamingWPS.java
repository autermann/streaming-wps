package com.github.autermann.wps.streaming;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;

import com.github.autermann.wps.commons.WPS;
import com.github.autermann.wps.streaming.ws.StreamingSocketEndpoint;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingWPS extends WPS {

    public StreamingWPS(String host, int port) throws Exception {
        super(host, port);
    }

    @Override
    protected Server createServer(int port) throws Exception {
        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(
                server, ROOT_CONTEXT,
                ServletContextHandler.SESSIONS);
        ServerContainer sc = WebSocketServerContainerInitializer
                .configureContext(handler);
        sc.addEndpoint(StreamingSocketEndpoint.class);
        handler.addServlet(WebProcessingService.class,
                           WEB_PROCESSING_SERVICE_PATH);
        handler.addServlet(RetrieveResultServlet.class,
                           RETRIEVE_RESULT_SERVLET_PATH);
        return server;
    }

}
