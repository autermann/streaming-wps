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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.data.Identifiable;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.message.StopMessage;
import com.github.autermann.wps.streaming.message.receiver.DelegatingMessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.IncomingMessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.OutgoingMessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.UnsupportedMessageTypeException;
import com.github.autermann.wps.streaming.util.CyclicDependencyException;
import com.github.autermann.wps.streaming.util.DependencyExecutor;
import com.github.autermann.wps.streaming.util.MissingInputException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingProcess extends IncomingMessageReceiver
        implements Identifiable<StreamingProcessID> {
    private static final Logger log =  LoggerFactory.getLogger(StreamingProcess.class);
    private static final int THREADS = 10;
    private final ProcessConfiguration configuration;
    private final DependencyExecutor<MessageID, InputMessage, OutputMessage>  executor;
    private final OutgoingMessageReceiverImpl output;
    private final DelegatingMessageReceiver clients;
    private final CallbackJobExecutor jobExecutor;
    private final Lock lock = new ReentrantLock();

    public StreamingProcess(ProcessConfiguration configuration) {
        this.configuration = checkNotNull(configuration);
        this.clients = new DelegatingMessageReceiver();
        this.output = new OutgoingMessageReceiverImpl();
        this.jobExecutor = configuration.createExecutor(output);
        String nameFormat = "streaming-process-" +
                  configuration.getProcessID() + "-%d";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(nameFormat).build();
        ExecutorService threadPool
                = Executors.newFixedThreadPool(THREADS, threadFactory);
        this.executor
                = new DependencyExecutor<MessageID, InputMessage, OutputMessage>(
                        jobExecutor, threadPool);
    }

    @Override
    public StreamingProcessID getID() {
        return this.configuration.getProcessID();
    }

    @Override
    protected void receiveOutputRequest(OutputRequestMessage message) {
        log.debug("{} Receiving output request message {}", getID(), message);
        this.clients.bind(message.getReceiver());
    }

    @Override
    protected void receiveStop(StopMessage message) {
        log.debug("{} Receiving stop message {}", getID(), message);
        try {
            stop();
        } catch (StreamingError ex) {
            sendError(ex, message);
        }
    }

    @Override
    protected void receiveInput(InputMessage message) {
        log.debug("{} Receiving input message {}", getID(), message);
        try {
            input(message);
        } catch (StreamingError error) {
            sendError(error, message);
        }
    }

    private void stop() throws StreamingError {
        try {
            lock.lock();
            try {
                executor.shutdown(true);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException ex) {
            throw new StreamingError(
                    "Computation could not complete",
                    StreamingError.NO_APPLICABLE_CODE, ex);
        } catch (MissingInputException ex) {
            throw new StreamingError(
                    "Stopped while input for referenced input is missing",
                    StreamingError.UNRESOLVABLE_INPUT, ex);
        } catch (IllegalStateException ex) {
            throw new StreamingError("Process is shutting down",
                                     StreamingError.NO_APPLICABLE_CODE, ex);
        } finally {
            try {
                jobExecutor.close();
            } catch (IOException e) {
                log.error("Error closing JobExecutor", e);
            }
        }
    }

    private void input(InputMessage message) throws StreamingError {
        lock.lock();
        try {
            Set<MessageID> dependencies = message
                    .getRelatedMessages(RelationshipType.Needs);

            executor.addJob(message.getID(), dependencies);
            executor.setInput(message.getID(), message);
        } catch (CyclicDependencyException ex) {
            throw new StreamingError(
                    "Cyclic dependency",
                    StreamingError.INVALID_PARAMETER_VALUE, ex);
        } catch (IllegalStateException ex) {
            throw new StreamingError("Process is shutting down",
                                     StreamingError.NO_APPLICABLE_CODE, ex);
        } finally {
            lock.unlock();
        }
    }

    private void sendError(StreamingError error, Message cause)
            throws UnsupportedMessageTypeException {
        ErrorMessage errorMessage = error.toMessage(cause);
        cause.getReceiver().receive(errorMessage);
        output.receive(errorMessage);
    }

    private class OutgoingMessageReceiverImpl extends OutgoingMessageReceiver {

        @Override
        protected void receiveError(ErrorMessage message) {
            log.debug("{} Receiving error message {}", getID(), message);
            clients.receive(message);
            try {
                stop();
            } catch (StreamingError ex) {
                log.warn("Error stopping on error", ex);
            }
        }

        @Override
        protected void receiveOutput(OutputMessage message) {
            log.debug("{} Receiving output message {}", getID(), message);
            clients.receive(message);
        }
    }

}
