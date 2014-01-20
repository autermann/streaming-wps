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
package com.github.autermann.wps.streaming;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public final class StreamingProcessID implements Serializable {

    private static final long serialVersionUID = 1L;
    private final URI id;

    private StreamingProcessID(URI id) {
        this.id = Preconditions.checkNotNull(id);
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    public URI toURI() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StreamingProcessID that = (StreamingProcessID) obj;
        return Objects.equal(this.id, that.id);
    }

    public static StreamingProcessID create(URI id) {
        return new StreamingProcessID(id);
    }

    public static StreamingProcessID create() {
        return create(UUID.randomUUID());
    }

    public static StreamingProcessID create(UUID uuid) {
        return create(URI.create("uuid:" + uuid.toString()));
    }

}
