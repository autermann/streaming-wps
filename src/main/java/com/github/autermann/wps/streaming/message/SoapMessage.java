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

import com.github.autermann.wps.streaming.data.Identifiable;
import com.github.autermann.wps.streaming.util.WSAConstants;
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
public abstract class SoapMessage implements Identifiable<MessageID> {
    private final SetMultimap<RelationshipType, MessageID> relatedMessages
            = HashMultimap.create();
    private MessageID messageId = MessageID.create();
    private URI from = WSAConstants.ANONYMOUS_ADDRESS;
    private URI replyTo = WSAConstants.NONE_ADDRESS;
    private URI faultTo = WSAConstants.NONE_ADDRESS;

    public MessageID getID() {
        return messageId;
    }

    public void setID(MessageID id) {
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

    public Multimap<RelationshipType, MessageID> getRelatedMessages() {
        return Multimaps.unmodifiableMultimap(relatedMessages);
    }

    public Set<MessageID> getRelatedMessages(RelationshipType type) {
        return (Set<MessageID>) getRelatedMessages().get(checkNotNull(type));
    }

    public void addRelatedMessageID(RelationshipType type, MessageID id) {
        this.relatedMessages.put(checkNotNull(type), checkNotNull(id));
    }

    public void addRelatedMessage(RelationshipType type,
                                  SoapMessage message) {
        addRelatedMessageID(type, message.getID());
    }

    public void addRelatedMessageIDs(RelationshipType type,
                                     Iterable<MessageID> ids) {
        for (MessageID id : ids) {
            addRelatedMessageID(type, id);
        }
    }

    public void addRelatedMessages(RelationshipType type,
                                   Iterable<?  extends SoapMessage> messages) {
        for (SoapMessage message : messages) {
            addRelatedMessageID(type, message.getID());
        }
    }

    public void setRelatedMessageID(RelationshipType type, MessageID id) {
        this.relatedMessages.removeAll(checkNotNull(type));
        addRelatedMessageID(type, id);
    }

    public void setRelatedMessage(RelationshipType type,
                                  SoapMessage message) {
        setRelatedMessageID(type, message.getID());
    }

    public void setRelatedMessageIDs(RelationshipType type,
                                     Iterable<MessageID> ids) {
        this.relatedMessages.removeAll(checkNotNull(type));
        addRelatedMessageIDs(type, ids);
    }

    public void setRelatedMessages(RelationshipType type,
                                   Iterable<?  extends SoapMessage> ids) {
        this.relatedMessages.removeAll(checkNotNull(type));
        addRelatedMessages(type, ids);
    }

    public abstract URI getSOAPAction();

}
