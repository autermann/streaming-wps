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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.ProcessInput;
import com.github.autermann.wps.streaming.data.ProcessInput.DataInput;
import com.github.autermann.wps.streaming.data.ProcessInput.ReferenceInput;
import com.github.autermann.wps.streaming.data.ProcessInputs;
import com.github.autermann.wps.streaming.data.ProcessOutput;
import com.github.autermann.wps.streaming.data.ProcessOutputs;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.util.JobExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class CallbackJobExecutor implements
        JobExecutor<InputMessage, OutputMessage>, Closeable {
    private final MessageReceiver callback;

    public CallbackJobExecutor(MessageReceiver callback) {
        this.callback = checkNotNull(callback);
    }

    @Override
    public OutputMessage execute(
            InputMessage input, Iterable<OutputMessage> dependencies)
            throws StreamingError {
        try {
            ProcessInputs inputs = createInputs(input, dependencies);
            ProcessOutputs outputs = execute(inputs);

            OutputMessage outMessage = new OutputMessage();
            outMessage.addRelatedMessages(RelationshipType.Used, dependencies);
            outMessage.addRelatedMessage(RelationshipType.Reply, input);
            outMessage.setPayload(outputs);
            this.callback.receive(outMessage);
            return outMessage;
        } catch (StreamingError ex) {
            this.callback.receive(ex.toMessage(input));
            throw ex;
        }
    }

    private ProcessInputs createInputs(InputMessage inputMessage,
                                       Iterable<OutputMessage> dependencies)
            throws StreamingError {
        ProcessInputs inputs = new ProcessInputs();
        Map<MessageID, ProcessOutputs> messages = Maps.newHashMap();
        for (OutputMessage message : dependencies) {
            Set<MessageID> relatedMessages
                    = message.getRelatedMessages(RelationshipType.Reply);
            if (relatedMessages.size() != 1) {
                // this should never happen
                throw new RuntimeException("OutputMessage replied to more than one message!");
            }
            messages.put(relatedMessages.iterator().next(),
                         message.getPayload());
        }

        for (ProcessInput input : inputMessage.getPayload()) {
            if (input instanceof DataInput) {
                inputs.addInput(input);
            } else if (input instanceof ReferenceInput) {
                ReferenceInput referenceInput = (ReferenceInput) input;
                ProcessOutputs outputs = messages.get(referenceInput
                        .getReferencedMessage());
                if (outputs == null) {
                    throw new StreamingError("ReferenceInput " + referenceInput +
                                             " can not be resolved",
                                             StreamingError.UNRESOLVABLE_INPUT);
                }
                List<ProcessOutput> output
                        = outputs.getOutputs(referenceInput
                                .getReferencedOutput());
                if (output.isEmpty()) {
                    throw new StreamingError("ReferenceInput " + referenceInput +
                                             " can not be resolved (no such output)",
                                             StreamingError.UNRESOLVABLE_INPUT);
                }
                inputs.addInputs(toInput(referenceInput.getID(), output));
            }
        }

        return inputs;
    }

    private Iterable<? extends ProcessInput> toInput(
            OwsCodeType id, List<ProcessOutput> outputs) {
        List<ProcessInput> inputs = Lists.newArrayListWithExpectedSize(outputs
                .size());

        for (ProcessOutput output : outputs) {
            inputs.add(new ProcessInput.DataInput(id, output.getData()));
        }
        return inputs;
    }

    protected abstract ProcessOutputs execute(ProcessInputs inputs) throws
            StreamingError;
}
