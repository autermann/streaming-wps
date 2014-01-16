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

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class OwsCodeType {

    private final String codeSpace;
    private final String value;

    public OwsCodeType(String codeSpace, String value) {
        this.codeSpace = emptyToNull(codeSpace);
        this.value = checkNotNull(emptyToNull(value));
    }

    public OwsCodeType(String value) {
        this(null, value);
    }

    public Optional<String> getCodeSpace() {
        return Optional.fromNullable(this.codeSpace);
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.codeSpace, this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OwsCodeType that = (OwsCodeType) obj;
        return Objects.equal(this.value, that.getValue()) &&
               Objects.equal(this.codeSpace, that.getCodeSpace());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("codeSpace", getCodeSpace().orNull())
                .add("value", getValue())
                .toString();
    }

}
