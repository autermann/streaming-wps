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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class ProcessOutputs {

    private final LinkedListMultimap<OwsCodeType, ProcessOutput> outputs = LinkedListMultimap.create();

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
