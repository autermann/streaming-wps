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

import net.opengis.ows.x11.BoundingBoxType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDataType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.BoundingBoxData;
import com.github.autermann.wps.streaming.data.Data;
import com.github.autermann.wps.streaming.data.BoundingBoxData;
import com.github.autermann.wps.streaming.data.ComplexData;
import com.github.autermann.wps.streaming.data.ComplexData;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.ReferencedData;
import com.github.autermann.wps.streaming.data.ReferencedData;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.output.ProcessOutput;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class CommonEncoding {
    public void encodeInput(InputType xbStreamingInput,
                            DataProcessInput streamingInput)
            throws XmlException {
        Data data = streamingInput.getData();
        if (data instanceof ReferencedData) {
            ReferencedData referencedData = (ReferencedData) data;
            xbStreamingInput.set(referencedData.getXml());
        } else {
            encodeData(xbStreamingInput.addNewData(), data);
        }
        streamingInput.getID().encodeTo(xbStreamingInput.addNewIdentifier());
    }

    public ProcessInput decodeInput(InputType xbStreamingInput)
            throws XmlException {
        OwsCodeType id = OwsCodeType.of(xbStreamingInput.getIdentifier());
        final Data data;
        if (xbStreamingInput.getReference() != null) {
            data = new ReferencedData(xbStreamingInput.getReference());
        } else {
            data = decodeData(xbStreamingInput.getData());
        }
        return new DataProcessInput(id, data);
    }

    public Data decodeData(DataType xbData) throws XmlException {
        if (xbData == null) {
            throw new XmlException("Missing wps:Data");
        } else if (xbData.getLiteralData() != null) {
            LiteralDataType xbLiteralData = xbData.getLiteralData();
            return new LiteralData(xbLiteralData.getDataType(),
                                        xbLiteralData.getStringValue(),
                                        xbLiteralData.getUom());
        } else if (xbData.getComplexData() != null) {
            ComplexDataType xbComplexData = xbData.getComplexData();
            String content = xbComplexData.xmlText();
            content = content.substring(content.indexOf('>') + 1, content.lastIndexOf("</"));
            return new ComplexData(Format.of(xbComplexData), content);
        } else if (xbData.getBoundingBoxData() != null) {
            BoundingBoxType xbBoundingBoxData = xbData.getBoundingBoxData();
            return new BoundingBoxData(xbBoundingBoxData);
        } else {
            throw new XmlException("Missing wps:LiteralData or wps:ComplexData or wps:BoundingBoxData");
        }
    }

    public void encodeData(DataType xbData, Data data)
            throws XmlException {
        if (data instanceof ComplexData) {
            encodeComplexData(xbData, (ComplexData) data);
        } else if (data instanceof LiteralData) {
            encodeLiteralData(xbData, (LiteralData) data);
        } else if (data instanceof BoundingBoxData) {
            BoundingBoxData boundingBoxData = (BoundingBoxData) data;
            xbData.setBoundingBoxData(boundingBoxData.getXml());
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + data);
        }
    }

    private void encodeLiteralData(DataType xbData,
                                   LiteralData literalData) {
        LiteralDataType xbLiteralData = xbData.addNewLiteralData();
        xbLiteralData.setDataType(literalData.getType());
        xbLiteralData.setStringValue(literalData.getValue());
        if (literalData.getUom().isPresent()) {
            xbLiteralData.setUom(literalData.getUom().get());
        }
    }

    private void encodeComplexData(DataType xbData,
                                   ComplexData complexData)
            throws XmlException {
        ComplexDataType xbComplexData = xbData.addNewComplexData();
        xbComplexData.set(asXmlObject(complexData));
        complexData.getFormat().encodeTo(xbComplexData);
    }

    private XmlObject asXmlObject(ComplexData complexData) {
        try {
            return XmlObject.Factory.parse(complexData.getContent());
        } catch (XmlException ex) {
            XmlString s = XmlString.Factory.newInstance();
            s.setStringValue(complexData.getContent());
            return s;
        }
    }

    public void encodeOutput(OutputDataType xbOutput, ProcessOutput output)
            throws XmlException {
        encodeData(xbOutput.addNewData(), output.getData());
        output.getID().encodeTo(xbOutput.addNewIdentifier());
    }

    public ProcessOutput decodeOutput(OutputDataType xbOutput)
            throws XmlException {
        OwsCodeType id = OwsCodeType.of(xbOutput.getIdentifier());
        Data data = decodeData(xbOutput.getData());
        return new ProcessOutput(id, data);
    }
}
