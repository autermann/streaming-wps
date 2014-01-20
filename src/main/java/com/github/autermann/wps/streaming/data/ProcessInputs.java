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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    public void addInput(ProcessInput input) {
        this.inputs.put(input.getID(), input);
    }

    public void addInputs(Iterable<? extends ProcessInput> inputs) {
        for (ProcessInput input : inputs) {
            addInput(input);
        }
    }

    @Override
    public Iterator<ProcessInput> iterator() {
        return getInputs().iterator();
    }

}
