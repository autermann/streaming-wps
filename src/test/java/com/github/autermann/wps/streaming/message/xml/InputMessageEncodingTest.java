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


import static com.github.autermann.wps.streaming.message.xml.EncodingTestHelper.createBoundingBox;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.data.BoundingBoxData;
import com.github.autermann.wps.streaming.data.ComplexData;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.ReferencedData;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class InputMessageEncodingTest {
    private static final Format FORMAT_CSV = new Format("text/csv", "UTF-8");
    private MessageEncoding<InputMessage> encoding;

    @Before
    public void setUp() {
        this.encoding = new InputMessageEncoding();
    }

    @Test
    public void writeReadWriteTest() throws XmlException {
        EnvelopeDocument xbEnvelope1 = encode(createMessage());
        EnvelopeDocument xbEnvelope2 = encode(decode(xbEnvelope1));
        assertThat(xbEnvelope1.valueEquals(xbEnvelope2), is(true));
    }

    private InputMessage decode(EnvelopeDocument xbEnvelope)
            throws XmlException {
        return encoding.decode(xbEnvelope.getEnvelope());
    }

    private EnvelopeDocument encode(InputMessage message) throws XmlException {
        String encode = encoding.encode(message);
        System.out.println(encode);
        return EnvelopeDocument.Factory.parse(encode);
    }

    private InputMessage createMessage()
            throws XmlException {
        StreamingProcessID processId = StreamingProcessID.create();
        MessageID ref = MessageID.create();
        InputMessage message = new InputMessage();
        message.setProcessID(processId);
        message.setPayload(new ProcessInputs()
                .addDataInput("input1", new LiteralData("xs:string", "input1"))
                .addDataInput("input2", new ComplexData(FORMAT_CSV, "<hello>w</hello>"))
                .addReferenceInput("input3", ref, "output1")
                .addDataInput("input4", new ReferencedData(URI.create(REFERENCED_DATA_HREF), new Format(null, null, REFERENCED_DATA_SCHEMA)))
                .addDataInput("input5", new BoundingBoxData(createBoundingBox())));
        return message;
    }
    public static final String REFERENCED_DATA_SCHEMA
            = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
    public static final String REFERENCED_DATA_HREF
            = "http://geoprocessing.demo.52north.org:8080/geoserver/wfs?SERVICE=WFS&amp;VERSION=1.0.0&amp;REQUEST=GetFeature&amp;TYPENAME=topp:tasmania_roads&amp;SRS=EPSG:4326&amp;OUTPUTFORMAT=GML3";
}
