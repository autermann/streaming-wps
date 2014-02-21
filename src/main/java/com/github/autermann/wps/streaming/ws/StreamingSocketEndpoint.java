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
import java.net.SocketTimeoutException;

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

import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
@ServerEndpoint(value = StreamingSocketEndpoint.PATH,
                encoders = SocketMessageEncoding.class,
                decoders = SocketMessageEncoding.class)
public class StreamingSocketEndpoint implements MessageReceiver {
    public static final String PATH = "/streaming";
    private static final Logger log = LoggerFactory
            .getLogger(StreamingSocketEndpoint.class);
    private final MessageBroker broker = MessageBroker.getInstance();
    private Session session;

    @OnOpen
    public void onOpen(Session session, EndpointConfig c) {
        log.info("Client session {} opened", session.getId());
        this.session = session;
    }

    @OnClose
    public void onClose(CloseReason reason) {
        log.info("Client session {} closed: {}", session.getId(), reason);
    }

    @OnMessage
    public void onMessage(Message message) {
        log.info("Receiving client message: {}", message);
        message.setReceiver(this);
        this.broker.receive(message);
    }

    @OnError
    public void onError(Throwable cause) {
        if (cause instanceof SocketTimeoutException) {
            log.info("Client session {} timed out: {}", session.getId(), cause.getMessage());
        } else {
            log.info("Client session " + session.getId() + " errored", cause);
        }
    }

    @Override
    public void receive(Message message) {
        try {
            if (session != null && session.isOpen()) {
                log.info("Receiving server message: {}", message);
                session.getBasicRemote().sendObject(message);
            }
        } catch (IOException | EncodeException ex) {
            throw new RuntimeException(ex);
        }
    }
}
