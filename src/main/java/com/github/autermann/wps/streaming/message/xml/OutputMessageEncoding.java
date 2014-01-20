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

import net.opengis.wps.x100.OutputDataType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.streaming.data.ProcessOutput;
import com.github.autermann.wps.streaming.data.ProcessOutputs;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.OutputMessageDocument;
import com.github.autermann.wps.streaming.xml.OutputMessageType;
import com.github.autermann.wps.streaming.xml.OutputsType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class OutputMessageEncoding extends AbstractMessageEncoding<OutputMessage> {

    @Override
    protected OutputMessage create() {
        return new OutputMessage();
    }

    @Override
    public URI getAction() {
        return SoapConstants.getOutputActionURI();
    }

    @Override
    protected XmlObject createBody(OutputMessage message) throws XmlException {
        OutputMessageDocument document = OutputMessageDocument.Factory
                .newInstance();
        OutputMessageType xbOutputMessage = document.addNewOutputMessage();
        xbOutputMessage.addNewProcessID().setStringValue(message.getProcessID()
                .toString());
        encodeOutputs(xbOutputMessage.addNewOutputs(), message.getPayload());
        return document;
    }

    private void encodeOutputs(OutputsType xbOutputs,
                               ProcessOutputs outputs) throws XmlException {
        for (ProcessOutput output : outputs.getOutputs()) {
            getCommonEncoding().encodeOutput(xbOutputs.addNewOutput(), output);
        }
    }

    @Override
    protected void decodeBody(OutputMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof OutputMessageDocument)) {
            throw new XmlException("Expected stream:OutputMessage");
        }

        OutputMessageDocument document = (OutputMessageDocument) body;
        OutputMessageType xbMessage = document.getOutputMessage();
        message.setProcessID(decodeProcessID(xbMessage.getProcessID()));
        message.setPayload(decodeOutputs(xbMessage.getOutputs()));
    }

    private ProcessOutputs decodeOutputs(OutputsType xbOutputs)
            throws XmlException {
        ProcessOutputs outputs = new ProcessOutputs();
        for (OutputDataType xbOutput : xbOutputs.getOutputArray()) {
            outputs.addOutput(getCommonEncoding().decodeOutput(xbOutput));
        }
        return outputs;
    }

}
