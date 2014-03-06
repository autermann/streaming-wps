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
package com.github.autermann.wps.streaming.data.input;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.Data;
import com.github.autermann.wps.streaming.message.MessageID;
import com.google.common.base.Objects;
import com.google.common.collect.LinkedListMultimap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessInputs implements Iterable<ProcessInput> {

    private final LinkedListMultimap<OwsCodeType, ProcessInput> inputs
            = LinkedListMultimap.create();

    public List<ProcessInput> getInputs() {
        return Collections.unmodifiableList(inputs.values());
    }

    public List<ProcessInput> getInputs(OwsCodeType inputId) {
        return Collections.unmodifiableList(inputs.get(inputId));
    }

    public ProcessInputs addInput(ProcessInput input) {
        this.inputs.put(input.getID(), input);
        return this;
    }

    public ProcessInputs addDataInput(OwsCodeType id, Data data) {
        return addInput(new DataProcessInput(id, data));
    }

    public ProcessInputs addDataInput(String id, Data data) {
        return addDataInput(new OwsCodeType(id), data);
    }

    public ProcessInputs addReferenceInput(OwsCodeType id, MessageID iteration, OwsCodeType output) {
        return addInput(new ReferenceProcessInput(id, iteration, output));
    }

    public ProcessInputs addReferenceInput(String id, MessageID iteration, OwsCodeType output) {
        return addInput(new ReferenceProcessInput(id, iteration, output));
    }

    public ProcessInputs addReferenceInput(OwsCodeType id, MessageID iteration, String output) {
        return addInput(new ReferenceProcessInput(id, iteration, output));
    }

    public ProcessInputs addReferenceInput(String id, MessageID iteration, String output) {
        return addInput(new ReferenceProcessInput(id, iteration, output));
    }

    public ProcessInputs addInputs(ProcessInputs inputs) {
        if (inputs != this) {
            addInputs(inputs.getInputs());
        }
        return this;
    }

    public ProcessInputs addInputs(Iterable<? extends ProcessInput> inputs) {
        for (ProcessInput input : inputs) {
            addInput(input);
        }
        return this;
    }

    @Override
    public Iterator<ProcessInput> iterator() {
        return getInputs().iterator();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("inputs", this.inputs)
                .toString();
    }

}
