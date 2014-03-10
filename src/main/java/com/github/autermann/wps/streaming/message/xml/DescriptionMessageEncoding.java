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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.commons.description.ows.OwsLanguageString;
import com.github.autermann.wps.commons.description.xml.ProcessDescriptionDecoder;
import com.github.autermann.wps.commons.description.xml.ProcessDescriptionEncoder;
import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.message.DescriptionMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.DescriptionMessageDocument;
import com.github.autermann.wps.streaming.xml.DescriptionMessageType;
import com.github.autermann.wps.streaming.xml.StreamingProcessDescriptionType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DescriptionMessageEncoding extends AbstractMessageEncoding<DescriptionMessage> {
    private final StreamingProcessDescriptionEncoder encoder
            = new StreamingProcessDescriptionEncoder();
    private final StreamingProcessDescriptionDecoder decoder
            = new StreamingProcessDescriptionDecoder();

    @Override
    protected DescriptionMessage create() {
        return new DescriptionMessage();
    }

    @Override
    public URI getAction() {
        return SoapConstants.getDescriptonActionURI();
    }

    @Override
    protected XmlObject createBody(DescriptionMessage message) {
        DescriptionMessageDocument document = DescriptionMessageDocument.Factory
                .newInstance();
        DescriptionMessageType xbDescriptionMessage = document
                .addNewDescriptionMessage();
        xbDescriptionMessage.addNewProcessID().setStringValue(message
                .getProcessID().toString());
        StreamingProcessDescriptionType xbDescription
                = xbDescriptionMessage.addNewStreamingProcessDescription();
        encoder
                .encodeStreamingProcessDescription(message.getPayload(), xbDescription);
        return document;
    }

    @Override
    protected void decodeBody(DescriptionMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof DescriptionMessageDocument)) {
            throw new XmlException("Expected stream:StopMessage");
        }
        DescriptionMessageDocument document = (DescriptionMessageDocument) body;
        DescriptionMessageType xbMessage = document.getDescriptionMessage();
        message.setProcessID(decodeProcessID(xbMessage.getProcessID()));
        message.setPayload(decoder.decodeStreamingProcessDescription(xbMessage
                .getStreamingProcessDescription()));
    }

    private static class StreamingProcessDescriptionEncoder extends ProcessDescriptionEncoder {

        public void encodeStreamingProcessDescription(
                StreamingProcessDescription description,
                StreamingProcessDescriptionType xbDescription) {
            xbDescription.setFinalResult(description.isFinalResult());
            xbDescription.setIntermediateResults(description
                    .isIntermediateResults());
            super.encodeProcessDescription(description, xbDescription);
        }

    }

    private static class StreamingProcessDescriptionDecoder extends ProcessDescriptionDecoder {

        public StreamingProcessDescription decodeStreamingProcessDescription(
                StreamingProcessDescriptionType xb) {
            StreamingProcessDescription pd
                    = new StreamingProcessDescription(
                            OwsCodeType.of(xb.getIdentifier()),
                            OwsLanguageString.of(xb.getTitle()),
                            OwsLanguageString.of(xb.getAbstract()),
                            xb.getProcessVersion(),
                            xb.getFinalResult(),
                            xb.getIntermediateResults());
            pd.addInputs(decodeProcessInputs(xb.getDataInputs()));
            pd.addOutputs(decodeProcessOutputs(xb.getProcessOutputs()));
            return pd;
        }

    }

}
