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

import com.github.autermann.wps.streaming.message.Message;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class ProcessInput implements Identifiable<OwsCodeType> {
    private final OwsCodeType id;

    private ProcessInput(OwsCodeType id) {
        this.id = checkNotNull(id);
    }

    @Override
    public OwsCodeType getID() {
        return this.id;
    }

    public static class DataInput extends ProcessInput {
        private final Data data;

        public DataInput(OwsCodeType id, Data data) {
            super(id);
            this.data = checkNotNull(data);
        }

        public Data getData() {
            return this.data;
        }
    }

    public static class ReferenceInput extends ProcessInput {
        private final OwsCodeType output;
        private final Message.ID iteration;

        public ReferenceInput(OwsCodeType id, 
                         Message.ID iteration,
                         OwsCodeType output) {
            super(id);
            this.iteration = checkNotNull(iteration);
            this.output = checkNotNull(output);
        }

        public OwsCodeType getReferencedOutput() {
            return this.output;
        }

        public Message.ID getReferencedIteration() {
            return this.iteration;
        }
    }

    public static class Static extends DataInput {

        public Static(OwsCodeType id, Data data) {
            super(id, data);
        }
    }
}
