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
package com.github.autermann.wps.streaming.ws;

import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Header;
import org.w3.x2005.x08.addressing.ActionDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;

import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.xml.AbstractMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.DescribeMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.DescriptionMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.ErrorMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.InputMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.MessageEncoding;
import com.github.autermann.wps.streaming.message.xml.OutputMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.OutputRequestMessageEncoding;
import com.github.autermann.wps.streaming.message.xml.StopMessageEncoding;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.google.common.collect.ImmutableMap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class SocketMessageEncoding
        implements Encoder.Text<Message>,
                   Decoder.Text<Message> {

    private static final ImmutableMap<URI, MessageEncoding<?>> ENCODINGS;

    static {
        AbstractMessageEncoding<?> eme = new ErrorMessageEncoding();
        AbstractMessageEncoding<?> ime = new InputMessageEncoding();
        AbstractMessageEncoding<?> ome = new OutputMessageEncoding();
        AbstractMessageEncoding<?> rme = new OutputRequestMessageEncoding();
        AbstractMessageEncoding<?> sme = new StopMessageEncoding();
        AbstractMessageEncoding<?> dme = new DescriptionMessageEncoding();
        AbstractMessageEncoding<?> dmd = new DescribeMessageEncoding();
        ENCODINGS = ImmutableMap.<URI, MessageEncoding<?>>builder()
                .put(eme.getAction(), eme)
                .put(ime.getAction(), ime)
                .put(ome.getAction(), ome)
                .put(rme.getAction(), rme)
                .put(sme.getAction(), sme)
                .put(dmd.getAction(), dmd)
                .put(dme.getAction(), dme)
                .build();
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public Message decode(String string)
            throws DecodeException {
        try {
            XmlObject xml = XmlObject.Factory.parse(string);
            if (!(xml instanceof EnvelopeDocument)) {
                throw new DecodeException(string, "Message is not a SOAP envelope");
            }
            EnvelopeDocument envelopeDocument = (EnvelopeDocument) xml;
            Envelope envelope = envelopeDocument.getEnvelope();
            Header header = envelope.getHeader();
            if (header == null) {
                throw new DecodeException(string, "Missing SOAP header");
            }
            XmlObject[] actions = header.selectChildren(SchemaConstants.QN_WSA_ACTION);
            if (actions == null || actions.length == 0) {
                throw new DecodeException(string, "Message has no action header");
            }
            if (actions.length > 1) {
                throw new DecodeException(string, "Message has no multiple action headers");
            }
            if (actions[0] instanceof ActionDocument) {
                ActionDocument actionDocument = (ActionDocument) actions[0];
                AttributedURIType action = actionDocument.getAction();
                return decode(action.getStringValue(), envelope);
            } else if (actions[0] instanceof AttributedURIType) {
                AttributedURIType action = (AttributedURIType) actions[0];
                return decode(action.getStringValue(), envelope);
            } else {
                throw new DecodeException(string, "Can not identify action header");
            }
        } catch (XmlException ex) {
            throw new DecodeException(string, "Unable to parse message", ex);
        }
    }

    private Message decode(String action, Envelope message)
            throws DecodeException, XmlException {
        return getDecoderForAction(action).decode(message);
    }

    private MessageEncoding<?> getDecoderForAction(String action)
            throws DecodeException {
        try {
            return getEncodingForAction(new URI(action));
        } catch (URISyntaxException ex) {
            throw new DecodeException(action, "Can not parse action URI", ex);
        }
    }

    private MessageEncoding<?> getEncodingForAction(URI uri)
            throws DecodeException {
        MessageEncoding<?> decoder = ENCODINGS.get(uri);
        if (decoder == null) {
            throw new DecodeException(uri.toString(), "Unknown action");
        }
        return decoder;
    }

    @Override
    public String encode(Message message)
            throws EncodeException {
        try {
            @SuppressWarnings("unchecked")
            MessageEncoding<Message> encoder
                    = (MessageEncoding<Message>) ENCODINGS
                    .get(message.getSOAPAction());
            if (encoder == null) {
                throw new EncodeException(message, "Unknown action");
            }
            return encoder.encode(message);
        } catch (XmlException ex) {
            throw new EncodeException(message, "Unable to encode message", ex);
        }

    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
