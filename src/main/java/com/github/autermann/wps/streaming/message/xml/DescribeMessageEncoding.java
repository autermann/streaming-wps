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

import com.github.autermann.wps.streaming.message.DescribeMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.DescribeMessageDocument;
import com.github.autermann.wps.streaming.xml.DescribeMessageType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DescribeMessageEncoding extends AbstractMessageEncoding<DescribeMessage> {

    @Override
    protected DescribeMessage create() {
        return new DescribeMessage();
    }

    @Override
    public URI getAction() {
        return SoapConstants.getDescribeActionURI();
    }

    @Override
    protected XmlObject createBody(DescribeMessage message) {
        DescribeMessageDocument document = DescribeMessageDocument.Factory
                .newInstance();
        DescribeMessageType xbStopMessage = document.addNewDescribeMessage();
        xbStopMessage.addNewProcessID().setStringValue(message.getProcessID()
                .toString());
        return document;
    }

    @Override
    protected void decodeBody(DescribeMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof DescribeMessageDocument)) {
            throw new XmlException("Expected stream:StopMessage");
        }
        DescribeMessageDocument document = (DescribeMessageDocument) body;
        message.setProcessID(decodeProcessID(document.getDescribeMessage()
                .getProcessID()));
    }

}
