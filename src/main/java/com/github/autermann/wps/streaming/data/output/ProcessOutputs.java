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
package com.github.autermann.wps.streaming.data.output;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.data.Data;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessOutputs implements Iterable<ProcessOutput> {
    private static final ProcessOutputs NONE
            = new ProcessOutputs(ImmutableListMultimap.<OwsCodeType, ProcessOutput>of());

    private final ListMultimap<OwsCodeType, ProcessOutput> outputs;

    public ProcessOutputs() {
        this(LinkedListMultimap.<OwsCodeType, ProcessOutput>create());
    }

    private ProcessOutputs(ListMultimap<OwsCodeType, ProcessOutput> outputs) {
        this.outputs = outputs;
    }

    public List<ProcessOutput> getOutputs() {
        return Collections.unmodifiableList((List<ProcessOutput>) outputs.values());
    }

    public List<ProcessOutput> getOutputs(OwsCodeType outputId) {
        return Collections.unmodifiableList(outputs.get(outputId));
    }

    public ProcessOutputs addOutput(ProcessOutput output) {
        this.outputs.put(output.getID(), output);
        return this;
    }

    public ProcessOutputs addOutput(OwsCodeType id, Data data) {
        return addOutput(new ProcessOutput(id, data));
    }

    public ProcessOutputs addOutput(String id, Data data) {
        return addOutput(new OwsCodeType(id), data);
    }

        public ProcessOutputs addOutputs(ProcessOutputs outputs) {
            if (outputs != this) {
                addOutputs(outputs.getOutputs());
            }
            return this;
        }

    public ProcessOutputs addOutputs(Iterable<? extends ProcessOutput> outputs) {
        for (ProcessOutput output : outputs) {
            addOutput(output);
        }
        return this;
    }

    @Override
    public Iterator<ProcessOutput> iterator() {
        return getOutputs().iterator();
    }

    public static ProcessOutputs none() {
        return NONE;
    }
}
