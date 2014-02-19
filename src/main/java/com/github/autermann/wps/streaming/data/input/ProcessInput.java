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

import com.github.autermann.wps.commons.Identifiable;
import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.Data;
import com.github.autermann.wps.streaming.message.MessageID;

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
}