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

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class Format {

    private final String mimeType;
    private final Optional<String> encoding;
    private final Optional<String> schema;

    public Format(String mimeType) {
        this(mimeType, null, null);
    }

    public Format(String mimeType, String encoding) {
        this(mimeType, encoding, null);
    }

    public Format(String mimeType, String encoding, String schema) {
        this.mimeType = checkNotNull(emptyToNull(mimeType));
        this.encoding = fromNullable(emptyToNull(encoding));
        this.schema = fromNullable(emptyToNull(schema));
    }

    public String getMimeType() {
        return mimeType;
    }

    public Optional<String> getEncoding() {
        return encoding;
    }

    public Optional<String> getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("mimeType", this.mimeType)
                .add("encoding", this.encoding)
                .add("schema", this.schema).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.mimeType, this.encoding, this.schema);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Format) {
            final Format that = (Format) obj;
            return Objects.equal(this.mimeType, that.mimeType) &&
                   Objects.equal(this.encoding, that.encoding) &&
                   Objects.equal(this.schema, that.schema);
        }
        return false;
    }

}
