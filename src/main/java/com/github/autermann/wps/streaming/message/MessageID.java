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
package com.github.autermann.wps.streaming.message;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class MessageID {

    private final URI id;

    private MessageID(URI id) {
        this.id = Preconditions.checkNotNull(id);
    }

    private URI getId() {
        return this.id;
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
        final MessageID that = (MessageID) obj;
        return Objects.equal(this.getId(), that.getId());
    }

    public static MessageID create(URI uri) {
        return new MessageID(uri);
    }

    public static MessageID create(UUID id) {
        String uri = "uuid:" + id.toString();
        return create(URI.create(uri));
    }

    public static MessageID create() {
        return create(UUID.randomUUID());
    }

}
