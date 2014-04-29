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

import com.github.autermann.wps.commons.Identifiable;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class ProcessInput implements Identifiable<OwsCodeType> {
    private final OwsCodeType id;

    ProcessInput(OwsCodeType id) {
        this.id = checkNotNull(id);
    }

    @Override
    public OwsCodeType getID() {
        return this.id;
    }

    public boolean isData() {
        return false;
    }

    public boolean isReference() {
        return false;
    }

    public DataProcessInput asData() {
        throw new UnsupportedOperationException();
    }

    public ReferenceProcessInput asReference() {
        throw new UnsupportedOperationException();
    }
}
