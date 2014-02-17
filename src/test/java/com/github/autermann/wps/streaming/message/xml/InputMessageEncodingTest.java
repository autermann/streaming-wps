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

import net.opengis.wps.x100.InputReferenceType;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.data.Data.BoundingBoxData;
import com.github.autermann.wps.streaming.data.Data.ComplexData;
import com.github.autermann.wps.streaming.data.Data.LiteralData;
import com.github.autermann.wps.streaming.data.Data.ReferencedData;
import com.github.autermann.wps.streaming.data.ProcessInput.DataInput;
import com.github.autermann.wps.streaming.data.ProcessInput.ReferenceInput;
import com.github.autermann.wps.streaming.data.ProcessInputs;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class InputMessageEncodingTest {
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

    private InputMessage createMessage() throws XmlException {
        ProcessInputs inputs = new ProcessInputs();
        StreamingProcessID processId = StreamingProcessID.create();
        MessageID ref = MessageID.create();
        inputs.addInput(new DataInput(new OwsCodeType("input1"), new LiteralData("xs:string", "input1")));
        inputs.addInput(new DataInput(new OwsCodeType("input2"), new ComplexData(new Format("text/csv", "UTF-8"), "<hello>w</hello>")));
        inputs.addInput(new ReferenceInput(new OwsCodeType("input3"), ref, new OwsCodeType("output1")));
        inputs.addInput(new DataInput(new OwsCodeType("input4"), new ReferencedData(createInputReference())));
        inputs.addInput(new DataInput(new OwsCodeType("input5"), new BoundingBoxData(createBoundingBox())));
        InputMessage message = new InputMessage();
        message.setProcessID(processId);
        message.setPayload(inputs);
        return message;
    }

    private InputReferenceType createInputReference() throws XmlException {
        return InputReferenceType.Factory
                .parse("<wps:Reference xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" schema=\"http://schemas.opengis.net/gml/3.1.1/base/gml.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/wfs?SERVICE=WFS&amp;VERSION=1.0.0&amp;REQUEST=GetFeature&amp;TYPENAME=topp:tasmania_roads&amp;SRS=EPSG:4326&amp;OUTPUTFORMAT=GML3\" method=\"GET\"/>");
    }



}
