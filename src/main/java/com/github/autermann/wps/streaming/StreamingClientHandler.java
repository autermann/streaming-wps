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

import java.util.Set;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.StreamingIteration;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.StopMessage;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingClientHandler {
    private final Set<Callback> callbacks = Sets.newHashSet();
    private final StreamingHandler handler;
    private final StreamingMessageSink sink;

    public StreamingClientHandler(StreamingHandler handler,
                                  StreamingMessageSink sink) {
        this.handler = Preconditions.checkNotNull(handler);
        this.sink = Preconditions.checkNotNull(sink);
    }

    public void onOutputRequestMessage(OutputRequestMessage message) {
        Callback callback = new Callback();
        callbacks.add(callback);
        getHandler().register(message.getProcessID(), callback);
    }

    public void onStopMessage(StopMessage message) {
        getHandler().stop(message.getProcessID());
    }

    public void onInputMessage(InputMessage message) {
        getHandler().input(message.getProcessID(), message.getPayload());
    }

    public void onOutputMessage(OutputMessage message) {
        //TODO throw error...
    }

    public StreamingHandler getHandler() {
        return handler;
    }

    public void onClose() {
        for (Callback callback : callbacks) {
            handler.unregister(callback);
        }
    }

    private OutputMessage createOutputMessage(StreamingIteration.Outputs output) {
        /* TODO implement com.github.autermann.wps.streaming.ws.WebSockets.StreamingEndpoint.StreamingOutputListenerImpl.createOutputMessage() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.WebSockets.StreamingEndpoint.StreamingOutputListenerImpl.createOutputMessage() not yet implemented");
    }

    private ErrorMessage createErrorMessage(StreamingError error) {
        /* TODO implement com.github.autermann.wps.streaming.ws.WebSockets.StreamingEndpoint.createErrorMessage() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.WebSockets.StreamingEndpoint.createErrorMessage() not yet implemented");
    }

    private class Callback implements StreamingOutputCallback {

        @Override
        public void output(StreamingIteration.Outputs output) {
            sink.accept(createOutputMessage(output));
        }

        @Override
        public void error(StreamingError error) {
            sink.accept(createErrorMessage(error));
        }
    }
}
