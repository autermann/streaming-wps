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

import static com.google.common.base.Preconditions.checkNotNull;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.Identifiable;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.MessageReceivers;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingProcess implements Identifiable<StreamingProcessID> {
    private final ProcessConfiguration configuration;
    private final MessageReceiver fromClient;

    public StreamingProcess(ProcessConfiguration configuration) throws ExceptionReport {
        this.configuration = checkNotNull(configuration);
        StreamingProcessor processor = new StreamingProcessor(this.configuration.getProcessID());
        this.fromClient = MessageReceivers.onlyIncomingMessages(processor);
        MessageReceiver callback = MessageReceivers.onlyOutgoingMessages(processor);
        StreamingDependencyExecutor dependencyExecutor = new StreamingDependencyExecutor(
                configuration.createStreamingExecutor(callback),
                configuration.createThreadPool(),
                configuration.createMessageRepository());
        processor.setExecutor(dependencyExecutor);
        processor.setDescription(configuration.describe());
    }

    @Override
    public StreamingProcessID getID() {
        return this.configuration.getProcessID();
    }

    public MessageReceiver getInput() {
        return this.fromClient;
    }
}
