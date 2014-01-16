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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.StreamingProcess.ID;
import com.github.autermann.wps.streaming.data.StreamingIteration;


public class StreamingHandlerImpl implements StreamingHandler {

    private static final Logger log = LoggerFactory.getLogger(StreamingHandlerImpl.class);

    @Override
    public void register(ID process, StreamingOutputCallback output) {
        /* TODO implement com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.register() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.register() not yet implemented");
    }

    @Override
    public void unregister(StreamingOutputCallback output) {
        /* TODO implement com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.unregister() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.unregister() not yet implemented");
    }

    @Override
    public void stop(ID process) {
        /* TODO implement com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.stop() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.stop() not yet implemented");
    }

    @Override
    public void input(ID process, StreamingIteration.Inputs input) {
        /* TODO implement com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.input() */
        throw new UnsupportedOperationException("com.github.autermann.wps.streaming.ws.StreamingHandlerImpl.input() not yet implemented");
    }

}
