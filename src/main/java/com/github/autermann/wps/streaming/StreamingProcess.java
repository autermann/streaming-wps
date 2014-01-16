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
public class StreamingProcess {

    public static final class ID implements Serializable {
        private static final long serialVersionUID = 1L;
        private final URI id;

        private ID(URI id) {
            this.id = Preconditions.checkNotNull(id);
        }

        @Override
        public String toString() {
            return this.id.toString();
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
            final ID that = (ID) obj;
            return Objects.equal(this.id, that.id);
        }

        public static ID create(URI id) {
            return new ID(id);
        }

        public static ID create() {
            return create(UUID.randomUUID());
        }

        public static ID create(UUID uuid) {
            return create(URI.create("uuid:" + uuid.toString()));
        }
    }

}
