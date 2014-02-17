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
package com.github.autermann.wps.streaming.message.xml;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Header;
import org.w3.x2005.x08.addressing.ActionDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.FaultToDocument;
import org.w3.x2005.x08.addressing.FromDocument;
import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.RelatesToDocument;
import org.w3.x2005.x08.addressing.RelatesToType;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.github.autermann.wps.streaming.xml.ProcessIDType;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractMessageEncoding<T extends Message>
        implements MessageEncoding<T> {
    private static final Logger log = LoggerFactory
            .getLogger(AbstractMessageEncoding.class);

    private final CommonEncoding commonEncoding = new CommonEncoding();

    @Override
    public String encode(T message) throws XmlException {
        EnvelopeDocument document = EnvelopeDocument.Factory.newInstance();
        Envelope envelope = document.addNewEnvelope();
        encodeBody(message, envelope.addNewBody());
        encodeHeader(message, envelope.addNewHeader());
        XmlCursor cursor = document.newCursor();
        if (cursor.toFirstChild()) {
            cursor.setAttributeText(SchemaConstants.QN_SCHEMA_LOCATION,
                                    SchemaConstants.SCHEMA_LOCATIONS);
        }
        return document.xmlText(SchemaConstants.XML_OPTIONS);
    }

    private void encodeBody(T message, Body body) throws XmlException {
        body.set(createBody(message));
    }

    protected abstract XmlObject createBody(T message) throws XmlException;

    private void encodeHeader(T message, Header header) {
        encodeFaultTo(message, header);
        encodeReplyTo(message, header);
        encodeFrom(message, header);
        encodeRelatedMessages(message, header);
        encodeMessageID(message, header);
        encodeSOAPAction(message, header);
    }

    private void encodeSOAPAction(T message, Header header) {
        ActionDocument actionDocument = ActionDocument.Factory.newInstance();
        actionDocument.addNewAction().setStringValue(message.getSOAPAction()
                .toString());
        append(header, actionDocument);
    }

    private void encodeMessageID(T message, Header header) {
        MessageIDDocument messageIDDocument = MessageIDDocument.Factory
                .newInstance();
        AttributedURIType messageID = messageIDDocument.addNewMessageID();
        messageID.setStringValue(message.getID().toString());
        append(header, messageIDDocument);
    }

    private void encodeRelatedMessages(T message, Header header) {
        for (RelationshipType type : message.getRelatedMessages().keySet()) {
            for (MessageID id : message.getRelatedMessages(type)) {
                RelatesToDocument relatesToDocument
                        = RelatesToDocument.Factory
                        .newInstance();
                RelatesToType relatesTo = relatesToDocument
                        .addNewRelatesTo();
                relatesTo.setRelationshipType(type.getUri().toString());
                relatesTo.setStringValue(id.toString());
                append(header, relatesToDocument);
            }
        }
    }

    private void encodeFrom(T sm, Header header) {
        if (sm.getFrom().isPresent()) {
            FromDocument fromDocument = FromDocument.Factory.newInstance();
            fromDocument.addNewFrom().addNewAddress()
                    .setStringValue(sm.getFrom().get().toString());
            append(header, fromDocument);
        }
    }

    private void encodeReplyTo(T message, Header header) {
        if (message.getReplyTo().isPresent()) {
            ReplyToDocument replyToDocument = ReplyToDocument.Factory
                    .newInstance();
            replyToDocument.addNewReplyTo().addNewAddress()
                    .setStringValue(message.getReplyTo().get().toString());
            append(header, replyToDocument);
        }
    }

    private void encodeFaultTo(T message, Header header) {
        if (message.getFaultTo().isPresent()) {
            FaultToDocument faultToDocument = FaultToDocument.Factory
                    .newInstance();
            faultToDocument.addNewFaultTo().addNewAddress()
                    .setStringValue(message.getFaultTo().get().toString());
            append(header, faultToDocument);
        }
    }

    private void append(XmlObject parent, XmlObject child) {
        XmlCursor childCursor = child.newCursor();
        childCursor.toStartDoc();
        childCursor.toNextToken();
        XmlCursor parentCursor = parent.newCursor();
        parentCursor.toEndToken();
        childCursor.moveXml(parentCursor);
        parentCursor.dispose();
        childCursor.dispose();
    }

    @Override
    public T decode(Envelope envelope) throws XmlException {
        T message = create();
        if (envelope.getHeader() != null) {
            decodeHeader(message, envelope.getHeader().getDomNode());
        }
        if (envelope.getBody() != null) {
            Node b = envelope.getBody().getDomNode();
            NodeList children = b.getChildNodes();
            boolean found = false;
            for (int i = 0; i < children.getLength(); ++i) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    decodeBody(message, XmlObject.Factory
                            .parse(children.item(i)));
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new XmlException("Only a single soap:Body child node is allowed");
            }

        }
        return message;
    }

    protected abstract T create();

    protected abstract void decodeBody(T message, XmlObject body) throws
            XmlException;

    protected void decodeHeader(T message, Node header) throws XmlException {
        NodeList children = header.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                decodeHeader(message, XmlObject.Factory.parse(children.item(i)));
            }
        }
    }

    private void decodeHeader(T message, XmlObject header) {
        if (header instanceof FaultToDocument) {
            decodeFaultTo(message, (FaultToDocument) header);
        } else if (header instanceof ReplyToDocument) {
            decodeReplyTo(message, (ReplyToDocument) header);
        } else if (header instanceof FromDocument) {
            decodeFrom(message, (FromDocument) header);
        } else if (header instanceof RelatesToDocument) {
            decodeRelatesTo(message, (RelatesToDocument) header);
        } else if (header instanceof MessageIDDocument) {
            decodeMessageId(message, (MessageIDDocument) header);
        } else if (header instanceof ActionDocument) {
            decodeAction(message, (ActionDocument) header);
        } else {
            log.warn("Ignoring unsupported header: {}", header.xmlText());
        }

    }

    private void decodeAction(T message, ActionDocument actionDocument) {
        Optional<URI> action = parseURI(actionDocument.getAction()
                .getStringValue());
        if (!action.isPresent() || !action.get().equals(message.getSOAPAction())) {
            log.warn("SOAP action mismatch: {} vs {}",
                     message.getSOAPAction(), action.orNull());
        }
    }

    private void decodeRelatesTo(T message, RelatesToDocument relatesToDocument) {
        Optional<URI> uri = parseURI(relatesToDocument.getRelatesTo()
                .getRelationshipType());
        Optional<URI> id = parseURI(relatesToDocument.getRelatesTo()
                .getStringValue());
        if (uri.isPresent() && id.isPresent()) {
            RelationshipType type = RelationshipType.valueOf(uri.get());
            if (type != null) {
                message.addRelatedMessageID(type, MessageID.create(id.get()));
            } else {
                log.warn("Unknown relationship type: {}", uri);
            }
        }
    }

    private void decodeFrom(T message, FromDocument fromDocument) {
        Optional<URI> from = decodeURI(fromDocument.getFrom());
        if (from.isPresent()) {
            message.setFrom(from.get());
        }
    }

    private void decodeReplyTo(T message, ReplyToDocument replyToDocument) {
        Optional<URI> replyTo = decodeURI(replyToDocument.getReplyTo());
        if (replyTo.isPresent()) {
            message.setReplyTo(replyTo.get());
        }
    }

    private void decodeFaultTo(T message, FaultToDocument faultToDocument) {
        Optional<URI> faultTo = decodeURI(faultToDocument.getFaultTo());
        if (faultTo.isPresent()) {
            message.setFaultTo(faultTo.get());
        }
    }

    private void decodeMessageId(T message, MessageIDDocument messageIDDocument) {
        Optional<MessageID> id = decodeMessageID(decodeURI(messageIDDocument
                .getMessageID()));
        if (id.isPresent()) {
            message.setID(id.get());
        }
    }

    protected Optional<URI> parseURI(String uri) {
        if (uri == null || uri.isEmpty()) {
            return Optional.absent();
        }
        try {
            return Optional.of(new URI(uri));
        } catch (URISyntaxException ex) {
            log.warn("Invalid URI: {}", ex);
            return Optional.absent();
        }
    }

    private Optional<URI> decodeURI(AttributedURIType a) {
        if (a == null) {
            return Optional.absent();
        }
        return parseURI(a.getStringValue());
    }

    private Optional<URI> decodeURI(EndpointReferenceType ert) {
        if (ert == null) {
            return Optional.absent();
        }
        return decodeURI(ert.getAddress());
    }

    protected Optional<MessageID> decodeMessageID(Optional<URI> uri) {
        if (uri == null || !uri.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(MessageID.create(uri.get()));
    }

    protected Optional<MessageID> decodeMessageID(AttributedURIType uri) {
        return decodeMessageID(decodeURI(uri));
    }

    protected StreamingProcessID decodeProcessID(ProcessIDType xbProcessId)
            throws XmlException {
        if (xbProcessId != null) {
            Optional<URI> uri = parseURI(xbProcessId.getStringValue());
            if (uri.isPresent()) {
                return StreamingProcessID.create(uri.get());
            }
        }
        throw new XmlException("Can not decode process id");
    }

    protected CommonEncoding getCommonEncoding() {
        return commonEncoding;
    }
}
