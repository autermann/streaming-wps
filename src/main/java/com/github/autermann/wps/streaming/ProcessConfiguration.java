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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.http.client.utils.URIBuilder;

import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.WebProcessingService;

import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.util.dependency.InMemoryRepository;
import com.github.autermann.wps.streaming.ws.StreamingSocketEndpoint;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class ProcessConfiguration {
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int INVALID_PORT = -1;
    private static final String HTTPS_SCHEME = "https";
    private static final String HTTP_SCHEME = "http";
    private static final String WS_SCHEME = "ws";
    private static final String WSS_SCHEME = "wss";
    private final URI socketURI;
    private final StreamingProcessID processId = StreamingProcessID.create();
    private ProcessInputs commonInputs = new ProcessInputs();

    public ProcessConfiguration() {
        this.socketURI = createSocketURI();
    }

    public ProcessInputs getStaticInputs() {
        return commonInputs;
    }
    public void setStaticInputs(ProcessInputs staticInputs) {
        this.commonInputs = checkNotNull(staticInputs);
    }

    public StreamingProcessID getProcessID() {
        return processId;
    }

    public ExecutorService createThreadPool() {
        String nameFormat = "streaming-process-" + getProcessID() + "-%d";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(nameFormat).build();
        return Executors.newCachedThreadPool(threadFactory);
    }

    public MessageRepository createMessageRepository() {
        return new DefaultMessageRepository();
    }

    public URI getInputSocketURI() {
        return this.socketURI;
    }

    public URI getOutputSocketURI() {
        return this.socketURI;
    }

    private URI createSocketURI() {
        try {
            String wpsEndpoint = CapabilitiesConfiguration.ENDPOINT_URL;
            URIBuilder builder = new URIBuilder(wpsEndpoint);
            switch (builder.getScheme()) {
                case HTTP_SCHEME:
                    builder.setScheme(WS_SCHEME);
                    if (builder.getPort() == DEFAULT_HTTP_PORT) {
                        builder.setPort(INVALID_PORT);
                    }
                    break;
                case HTTPS_SCHEME:
                    builder.setScheme(WSS_SCHEME);
                    if (builder.getPort() == DEFAULT_HTTPS_PORT) {
                        builder.setPort(INVALID_PORT);
                    }
                    break;
            }
            String webappPath = normalizePath(WebProcessingService.WEBAPP_PATH);
            String servletPath = normalizePath(StreamingSocketEndpoint.PATH);
            if (webappPath != null) {
                builder.setPath(webappPath + servletPath);
            } else {
                builder.setPath(servletPath);
            }
            return builder.build();
        } catch (URISyntaxException ex) {
            return URI.create("ws://localhost:8080/streaming");
        }
    }

    public abstract StreamingExecutor createStreamingExecutor(MessageReceiver callback);

    /**
     * Normalizes a path by returning {@code null} for {@code null}, {@code /}
     * or empty paths and transforming all other paths to {@code /path}.
     * @param path the path
     * @return the normalized path
     */
    private static String normalizePath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return null;
        }
        String s = path;
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.charAt(0) == '/' ? s : '/' + s;
    }

    private static class DefaultMessageRepository
            extends InMemoryRepository<MessageID, InputMessage, OutputMessage>
            implements MessageRepository {
    }
}
