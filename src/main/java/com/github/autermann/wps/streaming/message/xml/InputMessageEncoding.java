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

import net.opengis.ows.x11.CodeType;
import net.opengis.wps.x100.InputType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.streaming.data.OwsCodeType;
import com.github.autermann.wps.streaming.data.ProcessInput;
import com.github.autermann.wps.streaming.data.ProcessInput.ReferenceInput;
import com.github.autermann.wps.streaming.data.ProcessInputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.RelationshipType;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.InputMessageDocument;
import com.github.autermann.wps.streaming.xml.InputMessageType;
import com.github.autermann.wps.streaming.xml.InputsType;
import com.github.autermann.wps.streaming.xml.ReferenceInputType;
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
        return SoapConstants.getInputActionURI();
    }

    @Override
    protected XmlObject createBody(InputMessage sm) throws XmlException {
        ProcessInputs payload = sm.getPayload();
        InputMessageDocument doc = InputMessageDocument.Factory.newInstance();
        InputMessageType execute = doc.addNewInputMessage();
        execute.addNewProcessID().setStringValue(sm.getProcessID().toString());
        InputsType inputs = execute.addNewInputs();
        for (ProcessInput in : payload.getInputs()) {
            if (in instanceof ProcessInput.ReferenceInput) {
                ReferenceInput input = (ProcessInput.ReferenceInput) in;
                MessageID messageId
                        = encodeReferenceInput(inputs.addNewReferenceInput(), input);
                sm.addRelatedMessageID(RelationshipType.Needs, messageId);
            } else if (in instanceof ProcessInput.DataInput) {
                getCommonEncoding().encodeInput(inputs.addNewStreamingInput(), (ProcessInput.DataInput) in);
            } else if (in instanceof ProcessInput.Static) {
                throw new IllegalArgumentException("Static inputs are not supported for InputMessages");
            }
        }
        return doc;
    }

    private MessageID encodeReferenceInput(ReferenceInputType xbReferenceInput,
                                           ProcessInput.ReferenceInput referenceInput) {
        CodeType xbInputId = xbReferenceInput.addNewIdentifier();
        getCommonEncoding().encodeCodeType(xbInputId, referenceInput.getID());
        Reference xbReference = xbReferenceInput.addNewReference();
        MessageID referenced = referenceInput.getReferencedMessage();
        xbReference.addNewMessageID().setStringValue(
                referenced.toString());
        CodeType xbOutput = xbReference.addNewOutput();
        getCommonEncoding().encodeCodeType(xbOutput, referenceInput
                .getReferencedOutput());
        return referenced;
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

    private ProcessInputs decodeInputs(InputsType xbInputs)
            throws XmlException {
        ProcessInputs inputs = new ProcessInputs();
        if (xbInputs == null) {
            throw new XmlException("Missing stream:Inputs");
        }
        for (ReferenceInputType xbReferenceInput : xbInputs
                .getReferenceInputArray()) {
            inputs.addInput(decodeReferenceInput(xbReferenceInput));
        }
        for (InputType xbStreamingInput : xbInputs.getStreamingInputArray()) {
            inputs.addInput(getCommonEncoding().decodeInput(xbStreamingInput));
        }
        return inputs;
    }

    private ProcessInput decodeReferenceInput(
            ReferenceInputType xbReferenceInput) throws XmlException {
        Reference ref = xbReferenceInput.getReference();
        OwsCodeType inputID = getCommonEncoding()
                .decodeCodeType(xbReferenceInput.getIdentifier());
        Optional<MessageID> messageID = decodeMessageID(ref.getMessageID());
        OwsCodeType outputID = getCommonEncoding().decodeCodeType(ref
                .getOutput());
        if (!messageID.isPresent()) {
            throw new XmlException("Missing wsa:MessageID");
        }
        return new ProcessInput.ReferenceInput(
                inputID, messageID.get(), outputID);
    }

}
