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
package com.github.autermann.wps.streaming.xml;

import java.net.URI;

import net.opengis.ows.x11.CodeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.streaming.data.Data;
import com.github.autermann.wps.streaming.data.Data.ReferencedData;
import com.github.autermann.wps.streaming.data.OwsCodeType;
import com.github.autermann.wps.streaming.data.ProcessInput;
import com.github.autermann.wps.streaming.data.ProcessInput.DataInput;
import com.github.autermann.wps.streaming.data.StreamingIteration;
import com.github.autermann.wps.streaming.data.StreamingIteration.Inputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.SOAPMessage.ID;
import com.github.autermann.wps.streaming.util.SOAPConstants;
import com.github.autermann.wps.streaming.xml.ReferenceInputType.Reference;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class InputMessageEncoding extends AbstractMessageEncoding<InputMessage> {

    @Override
    public URI getAction() {
        return SOAPConstants.getInputActionURI();
    }

    @Override
    protected XmlObject createBody(InputMessage sm) throws XmlException {
        StreamingIteration.Inputs payload = sm.getPayload();
        InputMessageDocument doc = InputMessageDocument.Factory.newInstance();
        InputMessageType execute = doc.addNewInputMessage();
        execute.addNewProcessID().setStringValue(sm.getProcessID().toString());
        InputsType inputs = execute.addNewInputs();
        for (ProcessInput in : payload.getInputs()) {
            if (in instanceof ProcessInput.ReferenceInput) {
                encodeReferenceInput(inputs.addNewReferenceInput(), (ProcessInput.ReferenceInput) in);
            } else if (in instanceof ProcessInput.DataInput) {
                encodeStreamingInput(inputs.addNewStreamingInput(), (ProcessInput.DataInput) in);
            } else if (in instanceof ProcessInput.Static) {
                throw new IllegalArgumentException("Static inputs are not supported for InputMessages");
            }
        }
        return doc;
    }

    private void encodeReferenceInput(ReferenceInputType xbReferenceInput,
                                      ProcessInput.ReferenceInput referenceInput) {
        CodeType xbInputId = xbReferenceInput.addNewIdentifier();
        encodeCodeType(xbInputId, referenceInput.getID());
        Reference xbReference = xbReferenceInput.addNewReference();
        xbReference.addNewMessageID().setStringValue(
                referenceInput.getReferencedIteration().toString());
        CodeType xbOutput = xbReference.addNewOutput();
        encodeCodeType(xbOutput, referenceInput.getReferencedOutput());
    }

    private void encodeStreamingInput(StreamingInputType xbStreamingInput,
                                      ProcessInput.DataInput streamingInput)
            throws XmlException {
        Data data = streamingInput.getData();

        if (data instanceof Data.ReferencedData) {
            ReferencedData referencedData = (Data.ReferencedData) data;
            xbStreamingInput.set(referencedData.getXml());
        } else {
            encodeData(xbStreamingInput.addNewData(), data);
        }
        encodeCodeType(xbStreamingInput.addNewIdentifier(),
                       streamingInput.getID());
    }

    @Override
    protected InputMessage create() {
        return new InputMessage();
    }

    @Override
    protected void decodeBody(InputMessage message, XmlObject body)
            throws XmlException {
        if (!(body instanceof InputMessageDocument)) {
            throw new XmlException("Expected stream:InputMessage");
        }
        InputMessageDocument xbMessageDocument = (InputMessageDocument) body;
        InputMessageType xbInputMessage = xbMessageDocument.getInputMessage();
        message.setProcessID(decodeProcessID(xbInputMessage.getProcessID()));
        message.setPayload(decodeInputs(xbInputMessage.getInputs()));
    }

    private Inputs decodeInputs(InputsType xbInputs)
            throws XmlException {
        Inputs inputs = new Inputs();
        if (xbInputs == null) {
            throw new XmlException("Missing stream:Inputs");
        }
        for (ReferenceInputType xbReferenceInput : xbInputs
                .getReferenceInputArray()) {
            inputs.addInput(decodeReferenceInput(xbReferenceInput));
        }
        for (StreamingInputType xbStreamingInput : xbInputs
                .getStreamingInputArray()) {
            inputs.addInput(decodeStreamingInput(xbStreamingInput));
        }
        return inputs;
    }

    private ProcessInput decodeStreamingInput(
            StreamingInputType xbStreamingInput)
            throws XmlException {
        OwsCodeType id = decodeCodeType(xbStreamingInput.getIdentifier());
        final Data data;
        if (xbStreamingInput.getReference() != null) {
            data = new Data.ReferencedData(xbStreamingInput.getReference());
        } else {
            data = decodeData(xbStreamingInput.getData());
        }
        return new DataInput(id, data);
    }

    private ProcessInput decodeReferenceInput(
            ReferenceInputType xbReferenceInput)
            throws XmlException {
        Reference ref = xbReferenceInput.getReference();
        OwsCodeType inputID = decodeCodeType(xbReferenceInput.getIdentifier());
        Optional<ID> messageID = decodeMessageID(ref.getMessageID());
        OwsCodeType outputID = decodeCodeType(ref.getOutput());
        if (!messageID.isPresent()) {
            throw new XmlException("Missing wsa:MessageID");
        }
        return new ProcessInput.ReferenceInput(
                inputID, messageID.get(), outputID);
    }

}
