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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class OutputRequestMessageEncodingTest {

    private MessageEncoding<OutputRequestMessage> encoding;

    @Before
    public void setUp() {
        this.encoding = new OutputRequestMessageEncoding();
    }

    @Test
    public void writeReadWriteTest() throws XmlException {
        EnvelopeDocument xbEnvelope1 = encode(createMessage());
        EnvelopeDocument xbEnvelope2 = encode(decode(xbEnvelope1));
        assertThat(xbEnvelope1.valueEquals(xbEnvelope2), is(true));
    }

    private OutputRequestMessage decode(EnvelopeDocument xbEnvelope)
            throws XmlException {
        return encoding.decode(xbEnvelope.getEnvelope());
    }

    private EnvelopeDocument encode(OutputRequestMessage message) throws
            XmlException {
        String encode = encoding.encode(message);
        System.out.println(encode);
        return EnvelopeDocument.Factory.parse(encode);
    }

    private OutputRequestMessage createMessage() throws XmlException {
        OutputRequestMessage message = new OutputRequestMessage();
        message.setProcessID(StreamingProcessID.create());
        message.setIncludeInputs(true);
        return message;
    }
}
