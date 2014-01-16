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
package com.github.autermann.wps.streaming.ws;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.StreamingClientHandler;
import com.github.autermann.wps.streaming.StreamingHandler;
import com.github.autermann.wps.streaming.StreamingMessageSink;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.StopMessage;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
@ServerEndpoint(value = "/streaming",
                encoders = SocketMessageEncoding.class,
                decoders = SocketMessageEncoding.class)
public class StreamingSocketEndpoint implements StreamingMessageSink {
    private static final Logger log = LoggerFactory
            .getLogger(StreamingSocketEndpoint.class);
    public static final String HANDLER = "handler";
    private StreamingClientHandler handler;
    private Session session;

    @OnOpen
    public void onOpen(Session session, EndpointConfig c) {
        log.info("Client session {} opened", session.getId());
        this.handler
                = new StreamingClientHandler((StreamingHandler) c
                        .getUserProperties()
                        .get(HANDLER), this);
        this.session = session;
    }

    @OnError
    public void onError(Throwable cause) {
        log.info("Client session " + session.getId() + " errrored", cause);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        log.info("Client session {} closed: {}", session.getId(), reason);
        handler.onClose();
    }

    @OnMessage
    public void onMessage(Message message) {
        log.info("Receiving message: {}", message);
        if (message instanceof InputMessage) {
            handler.onInputMessage((InputMessage) message);
        } else if (message instanceof StopMessage) {
            handler.onStopMessage((StopMessage) message);
        } else if (message instanceof OutputRequestMessage) {
            handler.onOutputRequestMessage((OutputRequestMessage) message);
        } else if (message instanceof OutputMessage) {
            handler.onOutputMessage((OutputMessage) message);
        }
    }

    @Override
    public void accept(Message message) {
        Preconditions.checkState(isOpen());
        try {
            log.info("Sending message: {}", message);
            session.getBasicRemote().sendObject(message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (EncodeException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isOpen() {
        return session != null && session.isOpen();
    }
}
