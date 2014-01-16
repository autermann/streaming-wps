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
package com.github.autermann.wps.streaming.message;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import com.github.autermann.wps.streaming.data.Identifiable;
import com.github.autermann.wps.streaming.util.WSAConstants;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class SOAPMessage implements Identifiable<SOAPMessage.ID> {
    private final SetMultimap<RelationshipType, Message.ID> relatedMessages
            = HashMultimap.create();
    private Message.ID messageId = ID.create();
    private URI from = WSAConstants.ANONYMOUS_ADDRESS;
    private URI replyTo = WSAConstants.NONE_ADDRESS;
    private URI faultTo = WSAConstants.NONE_ADDRESS;

    public Message.ID getID() {
        return messageId;
    }

    public void setID(Message.ID id) {
        this.messageId = Preconditions.checkNotNull(id);
    }

    public Optional<URI> getFrom() {
        return Optional.fromNullable(this.from);
    }

    public void setFrom(URI from) {
        this.from = from;
    }

    public Optional<URI> getReplyTo() {
        return Optional.fromNullable(this.replyTo);
    }

    public void setReplyTo(URI replyTo) {
        this.replyTo = Preconditions.checkNotNull(replyTo);
    }

    public Optional<URI> getFaultTo() {
        return Optional.fromNullable(this.faultTo);
    }

    public void setFaultTo(URI faultTo) {
        this.faultTo = Preconditions.checkNotNull(faultTo);
    }

    public Multimap<RelationshipType, Message.ID> getRelatedMessages() {
        return Multimaps.unmodifiableMultimap(relatedMessages);
    }

    public Set<Message.ID> getRelatedMessages(RelationshipType type) {
        return (Set<Message.ID>) getRelatedMessages().get(checkNotNull(type));
    }

    public void addRelatedMessages(RelationshipType type, Message.ID id) {
        this.relatedMessages.put(checkNotNull(type), checkNotNull(id));
    }

    public void addRelatedMessages(RelationshipType type,
                                   Message message) {
        addRelatedMessages(type, message.getID());
    }

    public void addRelatedMessages(RelationshipType type,
                                   Iterable<Message.ID> ids) {
        for (Message.ID id : ids) {
            addRelatedMessages(type, id);
        }
    }

    public void setRelatedMessages(RelationshipType type, Message.ID id) {
        this.relatedMessages.removeAll(checkNotNull(type));
        addRelatedMessages(type, id);
    }

    public void setRelatedMessages(RelationshipType type,
                                   Message message) {
        setRelatedMessages(type, message.getID());
    }

    public void setRelatedMessages(RelationshipType type,
                                   Iterable<Message.ID> ids) {
        this.relatedMessages.removeAll(checkNotNull(type));
        addRelatedMessages(type, ids);
    }

    public void setRelatedMessages(
            Multimap<RelationshipType, Message.ID> relatedMessages) {
        checkNotNull(relatedMessages);
        this.relatedMessages.clear();
        for (RelationshipType type : relatedMessages.keySet()) {
            addRelatedMessages(type, relatedMessages.get(type));
        }
    }

    public abstract URI getSOAPAction();

    public static enum RelationshipType {
        Reply("http://www.w3.org/2005/08/addressing/reply"),
        Unspecified("http://www.w3.org/2005/08/addressing/unspecified"),
        Needs("https://github.com/autermann/streaming-wps/needs");

        private final URI uri;

        private RelationshipType(String uri) {
            this.uri = URI.create(uri);
        }

        public URI getUri() {
            return uri;
        }

        public static RelationshipType valueOf(URI uri) {
            for (RelationshipType type : values()) {
                if (type.getUri().equals(uri)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static class ID {
        private final URI id;

        private ID(URI id) {
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
            final ID that = (ID) obj;
            return Objects.equal(this.getId(), that.getId());
        }

        public static ID create(URI uri) {
            return new ID(uri);
        }

        public static ID create(UUID id) {
            String uri = "uuid:" + id.toString();
            return create(URI.create(uri));
        }

        public static ID create() {
            return create(UUID.randomUUID());
        }

    }

}
