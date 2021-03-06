/*
 * Copyright (C) 2014 Christian Autermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.autermann.wps.streaming;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.UnsupportedMessageTypeException;
import com.google.common.collect.Maps;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MessageBroker implements MessageReceiver{
    private static final MessageBroker INSTANCE = new MessageBroker();
    private static final Logger log = LoggerFactory.getLogger(MessageBroker.class);
    private final Map<StreamingProcessID, StreamingProcess> processes = Maps.newConcurrentMap();

    public void addProcess(ProcessConfiguration configuration) throws ExceptionReport {
        StreamingProcess process = new StreamingProcess(configuration);
        log.debug("Created process {}", process.getID());
        this.processes.put(process.getID(), process);
    }

    public void removeProcess(StreamingProcessID process) {
        log.debug("Removed process {}", process);
        this.processes.remove(process);
    }

    @Override
    public void receive(Message message) {
        log.debug("Receiving client message: {} for {}", message, message.getProcessID());
        try {
            receive1(message);
        } catch (StreamingError ex) {
            message.getReceiver().receive(ex.toMessage(message));
        }
    }

    private void receive1(Message message) throws StreamingError {
        StreamingProcess process = processes.get(message.getProcessID());
        if (process == null) {
            log.debug("Request for unknown process: {}", message.getProcessID());
            throw new StreamingError("Unknown ProcessId",
                                     StreamingError.INVALID_PARAMETER_VALUE);
        }
        try {
            process.getInput().receive(message);
            log.debug("Served message for process: {}", process.getID());
        } catch (UnsupportedMessageTypeException e) {
            throw new StreamingError("Unsupported Message",
                                     StreamingError.OPERATION_NOT_SUPPORTED, e);
        }
    }

    public static MessageBroker getInstance() {
        return INSTANCE;
    }
}
