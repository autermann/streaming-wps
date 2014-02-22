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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.message.StopMessage;
import com.github.autermann.wps.streaming.message.receiver.AbstractMessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.DelegatingMessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.MessageReceivers;
import com.github.autermann.wps.streaming.util.dependency.CyclicDependencyException;
import com.github.autermann.wps.streaming.util.dependency.MissingInputException;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class StreamingProcessor extends AbstractMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(StreamingProcessor.class);
    private final StreamingProcessID id;
    private final Lock lock = new ReentrantLock();
    private final DelegatingMessageReceiver toClients
            = new DelegatingMessageReceiver();
    private StreamingDependencyExecutor executor;

    public StreamingProcessor(StreamingProcessID id) {
        this.id = id;
    }

    public StreamingDependencyExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(StreamingDependencyExecutor executor) {
        this.executor = executor;
    }

    @Override
    protected void receiveError(ErrorMessage message) {
        error(message, MessageReceivers.nullReceiver());
    }

    @Override
    protected void receiveOutputRequest(OutputRequestMessage message) {
        toClients.bind(message.getReceiver());
    }

    @Override
    protected void receiveOutput(OutputMessage message) {
        toClients.receive(message);
    }

    @Override
    protected void receiveStop(StopMessage message) {
        try {
            stop();
        } catch (StreamingError error) {
            error(error.toMessage(message), message.getReceiver());
        }
    }

    @Override
    protected void receiveInput(InputMessage message) {
        try {
            input(message);
        } catch (StreamingError error) {
            error(error.toMessage(message), message.getReceiver());
        }
    }

    private void error(ErrorMessage message, MessageReceiver cause) {
        if (!toClients.contains(cause)) {
            cause.receive(message);
        }
        toClients.receive(message);
        try {
            stop();
        } catch (StreamingError ex) {
            log.error("Error stopping failed executor", ex);
        }
    }

    private void stop()
            throws StreamingError {
        log.debug("Stopping process {}", this.id);
        try {
            lock.lock();
            try {
                executor.shutdown(true);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException ex) {
            throw new StreamingError("Computation could not complete", StreamingError.NO_APPLICABLE_CODE, ex);
        } catch (MissingInputException ex) {
            throw new StreamingError("Stopped while input for referenced input is missing", StreamingError.UNRESOLVABLE_INPUT, ex);
        } catch (IllegalStateException ex) {
            throw new StreamingError("Process is shutting down", StreamingError.NO_APPLICABLE_CODE, ex);
        } finally {
            try {
                executor.close();
            } catch (IOException e) {
                log.error("Error closing JobExecutor", e);
            }
            MessageBroker.getInstance().removeProcess(this.id);
        }
    }

    private void input(InputMessage message)
            throws StreamingError {
        lock.lock();
        try {
            Set<MessageID> dependencies = message.getRelatedMessages(RelationshipType.Needs);
            executor.addJob(message.getID(), dependencies);
        } catch (CyclicDependencyException ex) {
            throw new StreamingError("Cyclic dependency", StreamingError.INVALID_PARAMETER_VALUE, ex);
        } catch (IllegalStateException ex) {
            throw new StreamingError("Process is shutting down", StreamingError.NO_APPLICABLE_CODE, ex);
        } finally {
            lock.unlock();
        }
        executor.setInput(message.getID(), message);
    }

}
