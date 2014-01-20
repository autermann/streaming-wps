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

import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.OutputRequestMessageDocument;
import com.github.autermann.wps.streaming.xml.OutputRequestMessageType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class OutputRequestMessageEncoding extends AbstractMessageEncoding<OutputRequestMessage> {

    @Override
    public URI getAction() {
        return SoapConstants.getOutputRequestActionURI();
    }

    @Override
    protected OutputRequestMessage create() {
        return new OutputRequestMessage();
    }


    @Override
    protected XmlObject createBody(OutputRequestMessage message) {
        OutputRequestMessageDocument document = OutputRequestMessageDocument.Factory.newInstance();
        OutputRequestMessageType xbOutputRequestMessage = document.addNewOutputRequestMessage();
        xbOutputRequestMessage.addNewProcessID().setStringValue(message.getProcessID().toString());
        xbOutputRequestMessage.setIncludeInputs(message.isIncludeInputs());
        return document;
    }

    @Override
    protected void decodeBody(OutputRequestMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof OutputRequestMessageDocument)) {
            throw new XmlException("Expected stream:OutputRequestMessage");
        }
        OutputRequestMessageDocument document = (OutputRequestMessageDocument) body;
        message.setProcessID(decodeProcessID(document.getOutputRequestMessage().getProcessID()));
        message.setIncludeInputs(document.getOutputRequestMessage().getIncludeInputs());
    }

}
