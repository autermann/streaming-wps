/*
 * Copyright (C) 2014 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
