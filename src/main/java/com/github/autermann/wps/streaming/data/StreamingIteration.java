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
package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import com.github.autermann.wps.streaming.StreamingProcess;
import com.github.autermann.wps.streaming.message.Message;
import com.google.common.base.Objects;
import com.google.common.collect.LinkedListMultimap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingIteration {
    private final StreamingProcess.ID processID;
    private final Message.ID messageID;
    private final Inputs inputs;
    private final Outputs outputs;

    public StreamingProcess.ID getProcessID() {
        return this.processID;
    }

    public Message.ID getMessageID() {
        return this.messageID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getProcessID(), getMessageID());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StreamingIteration that = (StreamingIteration) obj;
        return Objects.equal(this.getProcessID(), that.getProcessID()) &&
               Objects.equal(this.getMessageID(), that.getMessageID());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("process", getProcessID())
                .add("message", getMessageID())
                .toString();
    }

    public StreamingIteration(StreamingProcess.ID processID,
                              Message.ID messageID,
                              Inputs inputs,
                              Outputs outputs) {
        this.processID = checkNotNull(processID);
        this.messageID = checkNotNull(messageID);
        this.inputs = inputs == null ? new Inputs() : inputs;
        this.outputs = outputs == null ? new Outputs() : outputs;

    }

    public StreamingIteration(StreamingProcess.ID processID,
                              Message.ID messageID) {
        this(processID, messageID, null, null);
    }

    public Outputs getOutputs() {
        return this.outputs;
    }

    public Inputs getInputs() {
        return this.inputs;
    }

    public static class Inputs {
        private final LinkedListMultimap<OwsCodeType, ProcessInput> inputs
                = LinkedListMultimap.create();

        public List<ProcessInput> getInputs() {
            return Collections.unmodifiableList(inputs.values());
        }

        public List<ProcessInput> getInputs(OwsCodeType inputId) {
            return Collections.unmodifiableList(inputs.get(inputId));
        }

        public void addInput(ProcessInput input) {
            this.inputs.put(input.getID(), input);
        }
    }

    public static class Outputs {
        private final LinkedListMultimap<OwsCodeType, ProcessOutput> outputs
                = LinkedListMultimap.create();

        public List<ProcessOutput> getOutputs() {
            return Collections.unmodifiableList(outputs.values());
        }

        public List<ProcessOutput> getOutputs(OwsCodeType outputId) {
            return Collections.unmodifiableList(outputs.get(outputId));
        }

        public void addOutput(ProcessOutput output) {
            this.outputs.put(output.getID(), output);
        }
    }
}
