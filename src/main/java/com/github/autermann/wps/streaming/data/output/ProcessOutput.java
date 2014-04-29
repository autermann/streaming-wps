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

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.autermann.wps.commons.Identifiable;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.data.Data;


/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessOutput implements Identifiable<OwsCodeType> {
    private final Data data;
    private final OwsCodeType id;

    public ProcessOutput(OwsCodeType id, Data data) {
        this.id = checkNotNull(id);
        this.data = checkNotNull(data);
    }

    @Override
    public OwsCodeType getID() {
        return this.id;
    }

    public Data getData() {
        return data;
    }
}
