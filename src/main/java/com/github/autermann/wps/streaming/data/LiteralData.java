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
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class LiteralData extends Data {

    private final String type;
    private final String value;
    private final Optional<String> uom;

    public LiteralData(String type, String value, String uom) {
        this.type = checkNotNull(type);
        this.value = checkNotNull(emptyToNull(value));
        this.uom = Optional.fromNullable(emptyToNull(uom));
    }

    public LiteralData(String type, String value) {
        this(type, value, null);
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public Optional<String> getUom() {
        return this.uom;
    }

}
