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

import com.github.autermann.wps.streaming.StreamingProcess;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

import com.github.autermann.wps.streaming.message.StopMessage;
import com.github.autermann.wps.streaming.xml.MessageEncoding;
import com.github.autermann.wps.streaming.xml.StopMessageEncoding;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StopMessageEncodingTest {

    private MessageEncoding<StopMessage> encoding;

    @Before
    public void setUp() {
        this.encoding = new StopMessageEncoding();
    }

    @Test
    public void writeReadWriteTest() throws XmlException {
        EnvelopeDocument xbEnvelope1 = encode(createMessage());
        EnvelopeDocument xbEnvelope2 = encode(decode(xbEnvelope1));
        assertThat(xbEnvelope1.valueEquals(xbEnvelope2), is(true));
    }

    private StopMessage decode(EnvelopeDocument xbEnvelope)
            throws XmlException {
        return encoding.decode(xbEnvelope.getEnvelope());
    }

    private EnvelopeDocument encode(StopMessage message) throws XmlException {
        return EnvelopeDocument.Factory.parse(encoding.encode(message));
    }

    private StopMessage createMessage() throws XmlException {
        StopMessage message = new StopMessage();
        message.setProcessID(StreamingProcess.ID.create());
        return message;
    }
}
