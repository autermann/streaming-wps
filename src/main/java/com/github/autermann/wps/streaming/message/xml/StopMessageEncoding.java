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
package com.github.autermann.wps.streaming.message.xml;

import java.net.URI;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.streaming.message.StopMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.StopMessageDocument;
import com.github.autermann.wps.streaming.xml.StopMessageType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StopMessageEncoding extends AbstractMessageEncoding<StopMessage> {

    @Override
    public URI getAction() {
        return SoapConstants.getStopActionURI();
    }

    @Override
    protected StopMessage create() {
        return new StopMessage();
    }

    @Override
    protected XmlObject createBody(StopMessage message) {
        StopMessageDocument document = StopMessageDocument.Factory.newInstance();
        StopMessageType xbStopMessage = document.addNewStopMessage();
        xbStopMessage.addNewProcessID().setStringValue(message.getProcessID()
                .toString());
        return document;
    }

    @Override
    protected void decodeBody(StopMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof StopMessageDocument)) {
            throw new XmlException("Expected stream:StopMessage");
        }
        StopMessageDocument document = (StopMessageDocument) body;
        message.setProcessID(decodeProcessID(document.getStopMessage()
                .getProcessID()));
    }

}
