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

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.message.MessageID;

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
}
