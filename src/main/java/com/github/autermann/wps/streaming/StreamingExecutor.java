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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.input.ReferenceProcessInput;
import com.github.autermann.wps.streaming.data.output.ProcessOutput;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.util.dependency.JobExecutor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class StreamingExecutor implements
        JobExecutor<InputMessage, OutputMessage>, Closeable {
    private final MessageReceiver callback;
    private final ProcessInputs commonInputs;

    public StreamingExecutor(MessageReceiver callback,
                             ProcessConfiguration configuration) {
        this.callback = checkNotNull(callback);
        this.commonInputs = checkNotNull(configuration.getStaticInputs());
    }

    @Override
    public OutputMessage execute(
            InputMessage input, Iterable<OutputMessage> dependencies)
            throws StreamingError {
        try {
            ProcessInputs inputs = createInputs(input, dependencies);
            ProcessOutputs outputs = execute(inputs);
            OutputMessage output = new OutputMessage();
            output.addRelatedMessages(RelationshipType.Used, dependencies);
            output.addRelatedMessage(RelationshipType.Reply, input);
            output.setPayload(outputs);
            output.setProcessID(input.getProcessID());
            this.callback.receive(output);
            return output;
        } catch (StreamingError ex) {
            this.callback.receive(ex.toMessage(input));
            throw ex;
        }
    }

    private ProcessInputs createInputs(InputMessage inputMessage,
                                       Iterable<OutputMessage> outputMessages)
            throws StreamingError {
        try {
            Dependencies dependencies = new Dependencies(outputMessages);
            ProcessInputs inputs = new ProcessInputs().addInputs(commonInputs);
            for (ProcessInput input : inputMessage.getPayload()) {
                if (input instanceof DataProcessInput) {
                    inputs.addInput(input);
                } else if (input instanceof ReferenceProcessInput) {
                    inputs.addInputs(dependencies.resolve((ReferenceProcessInput) input));
                }
            }
            return inputs;
        } catch (UnresolvableInputException e) {
            throw new StreamingError("ReferenceInput can not be resolved",
                                     StreamingError.UNRESOLVABLE_INPUT, e);
        }
    }

    protected abstract ProcessOutputs execute(ProcessInputs inputs)
            throws StreamingError;

    private static class Dependencies {
        private final Map<MessageID, OutputMessage> messages;

        Dependencies(Iterable<OutputMessage> dependencies)
                throws UnresolvableInputException {
            this.messages = buildMap(dependencies);
        }

        Iterable<? extends ProcessInput> resolve(ReferenceProcessInput input)
                throws UnresolvableInputException {
            List<ProcessOutput> outputs = getOutputs(input);
            List<ProcessInput> inputs = new ArrayList<>(outputs.size());
            for (ProcessOutput output : outputs) {
                inputs.add(new DataProcessInput(input.getID(), output.getData()));
            }
            return inputs;
        }

        private Map<MessageID, OutputMessage>  buildMap(Iterable<OutputMessage> dependencies)
                throws UnresolvableInputException {
            Builder<MessageID, OutputMessage> builder = ImmutableMap.builder();
            for (OutputMessage v : dependencies) {
                Set<MessageID> related = v.getRelatedMessages(RelationshipType.Reply);
                // this should never happen
                if (related.size() != 1) {
                    throw new UnresolvableInputException("OutputMessage %s replied to more than one message!", v);
                }
                builder.put(related.iterator().next(), v);
            }
            return builder.build();
        }

        private List<ProcessOutput> getOutputs(ReferenceProcessInput input)
                throws UnresolvableInputException {
            OutputMessage message = messages.get(input.getReferencedMessage());
            if (message == null) {
                throw new UnresolvableInputException("Message %s can not be found!", input.getReferencedMessage());
            }
            List<ProcessOutput> output = message.getPayload()
                    .getOutputs(input.getReferencedOutput());
            if (output.isEmpty()) {
                throw new UnresolvableInputException("Output %s can not be found.", input.getReferencedOutput());
            }
            return output;
        }
    }

    private static class UnresolvableInputException extends Exception {
        private static final long serialVersionUID = 879913574518102564L;

        UnresolvableInputException(String message, Object... param) {
            super(String.format(message, param));
        }
    }

}
