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
package com.github.autermann.wps.streaming.data.input;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.message.MessageID;
import com.google.common.base.Objects;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ReferenceProcessInput extends ProcessInput {

    private final OwsCodeType output;
    private final MessageID iteration;

    public ReferenceProcessInput(OwsCodeType id,
                                 MessageID iteration,
                                 OwsCodeType output) {
        super(id);
        this.iteration = checkNotNull(iteration);
        this.output = checkNotNull(output);
    }

    public ReferenceProcessInput(String id,
                                 MessageID iteration,
                                 String output) {
        this(new OwsCodeType(id), iteration, new OwsCodeType(output));
    }

    public ReferenceProcessInput(OwsCodeType id,
                                 MessageID iteration,
                                 String output) {
        this(id, iteration, new OwsCodeType(output));
    }

    public ReferenceProcessInput(String id,
                                 MessageID iteration,
                                 OwsCodeType output) {
        this(new OwsCodeType(id), iteration, output);

    }

    public OwsCodeType getReferencedOutput() {
        return this.output;
    }

    public MessageID getReferencedMessage() {
        return this.iteration;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public ReferenceProcessInput asReference() {
        return this;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getID())
                .add("referencedOutput", getReferencedOutput())
                .add("referencedMessage", getReferencedMessage())
                .toString();
    }
}
