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
package com.github.autermann.wps.streaming.delegate;

import static org.n52.wps.io.IOHandler.DEFAULT_ENCODING;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.ValueType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.SupportedCRSsType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;
import net.opengis.wps.x100.SupportedUOMsType;

import org.apache.xmlbeans.XmlException;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.streaming.data.OwsCodeType;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.BoundingBoxInputDescription;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.BoundingBoxOutputDescription;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.ComplexInputDescription;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.ComplexOutputDescription;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.LiteralInputDescription;
import com.github.autermann.wps.streaming.delegate.ProcessDescription.LiteralOutputDescription;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescriptionParser extends AbstractParser {
    private final CommonEncoding enc = new CommonEncoding();

    public ProcessDescriptionParser() {
        super(new Format(
                MediaType.XML_UTF_8.toString(),
                DEFAULT_ENCODING,
                SchemaConstants.SCHEMA_LOCATION_PROCESS_DESCRIPTIONS),
              ProcessDescriptionBinding.class);
    }

    @Override
    protected ProcessDescriptionBinding parse(InputStream input) throws
            IOException {
        try {
            ProcessDescriptionsDocument document
                    = ProcessDescriptionsDocument.Factory.parse(input);
            ProcessDescriptionType xbProcessDescription = document
                    .getProcessDescriptions().getProcessDescriptionArray(0);

            OwsCodeType identifier = enc.decodeCodeType(xbProcessDescription
                    .getIdentifier());
            ProcessDescription processDescription
                    = new ProcessDescription(identifier);
            parseInputs(xbProcessDescription, processDescription);
            parseOutputs(xbProcessDescription, processDescription);
            return new ProcessDescriptionBinding(processDescription);
        } catch (XmlException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void parseInputs(ProcessDescriptionType xbProcessDescription,
                             ProcessDescription processDescription)
            throws XmlException {
        ProcessDescriptionType.DataInputs xbDataInputs = xbProcessDescription.getDataInputs();
        for (InputDescriptionType xbInputDescription : xbDataInputs.getInputArray()) {
            parseInput(xbInputDescription, processDescription);
        }
    }

    private void parseInput(InputDescriptionType xbInputDescription,
                            ProcessDescription processDescription)
            throws XmlException {
        OwsCodeType id = enc
                .decodeCodeType(xbInputDescription.getIdentifier());
        BigInteger minOccurs = xbInputDescription.getMinOccurs();
        BigInteger maxOccurs = xbInputDescription.getMaxOccurs();
        LiteralInputType literalData = xbInputDescription.getLiteralData();
        SupportedCRSsType boundingBoxData = xbInputDescription
                .getBoundingBoxData();
        SupportedComplexDataInputType complexData = xbInputDescription
                .getComplexData();
        if (boundingBoxData != null) {
            final Set<String> supported = decode(boundingBoxData);
            processDescription.addProcessInputDescription(
                    new BoundingBoxInputDescription(
                            id, minOccurs, maxOccurs,
                            boundingBoxData.getDefault().getCRS(), supported));
        } else if (literalData != null) {
            String dataType = literalData.getDataType().getStringValue();
            final Set<String> uoms = decode(literalData.getUOMs());
            if (literalData.getAllowedValues() != null) {
                Set<String> allowedValues = Sets.newHashSet();
                ValueType[] xbAllowedValues
                        = literalData.getAllowedValues().getValueArray();

                // TODO inputDescription.getLiteralData().getAllowedValues().getRangeArray()
                for (ValueType allowedValue : xbAllowedValues) {
                    allowedValues.add(allowedValue.getStringValue());
                }

                processDescription.addProcessInputDescription(
                        new LiteralInputDescription(id, minOccurs, maxOccurs, dataType, allowedValues, uoms));
            } else {
                processDescription.addProcessInputDescription(
                        new LiteralInputDescription(id, minOccurs, maxOccurs, dataType, uoms));
            }

            // TODO inputDescription.getLiteralData().getValuesReference()
        } else if (complexData != null) {
            Format format = decode(complexData.getDefault().getFormat());
            Set<Format> formats = decode(complexData.getSupported()
                    .getFormatArray());
            processDescription.addProcessInputDescription(
                    new ComplexInputDescription(id, minOccurs, maxOccurs, format, formats));
        }
    }

    private Set<Format> decode(ComplexDataDescriptionType[] xbFormats) {
        Set<Format> formats = Sets.newHashSet();
        for (ComplexDataDescriptionType xbFormat : xbFormats) {
            formats.add(decode(xbFormat));
        }
        return formats;
    }

    private Format decode(ComplexDataDescriptionType xbFormat) {
        String encoding = xbFormat.getEncoding();
        String mimeType = xbFormat.getMimeType();
        String schema = xbFormat.getSchema();
        return new Format(mimeType, encoding, schema);
    }

    private Set<String> decode(SupportedCRSsType s) {
        if (s.getSupported() != null) {
            return Sets.newHashSet(s.getSupported().getCRSArray());
        } else {
            return Collections.emptySet();
        }
    }

    private Set<String> decode(SupportedUOMsType s) {
        if (s != null) {
            HashSet<String> uoms = Sets.newHashSet();
            for (DomainMetadataType xbUom : s.getSupported().getUOMArray()) {
                uoms.add(xbUom.getStringValue());
            }
            return uoms;
        } else {
            return Collections.emptySet();
        }
    }

    private void parseOutputs(ProcessDescriptionType xbProcessDescription,
                              ProcessDescription processDescription)
            throws XmlException {
        ProcessDescriptionType.ProcessOutputs xbProcessOutputs
                = xbProcessDescription.getProcessOutputs();
        for (OutputDescriptionType xbOutputDescription : xbProcessOutputs
                .getOutputArray()) {
            parseOutput(xbOutputDescription, processDescription);
        }
    }

    private void parseOutput(OutputDescriptionType xbOutputDescription,
                             ProcessDescription processDescription)
            throws XmlException {
        OwsCodeType id = enc.decodeCodeType(xbOutputDescription
                .getIdentifier());
        SupportedCRSsType boundingBoxOutput
                = xbOutputDescription.getBoundingBoxOutput();
        LiteralOutputType literalOutput
                = xbOutputDescription.getLiteralOutput();
        SupportedComplexDataType complexOutput
                = xbOutputDescription.getComplexOutput();

        if (boundingBoxOutput != null) {
            Set<String> supported = decode(boundingBoxOutput);
            processDescription.addProcessOutputDescription(
                    new BoundingBoxOutputDescription(id, boundingBoxOutput
                            .getDefault().getCRS(), supported));
        } else if (complexOutput != null) {
            Format defaultFormat
                    = decode(complexOutput.getDefault().getFormat());
            Set<Format> supported
                    = decode(complexOutput.getSupported().getFormatArray());
            processDescription.addProcessOutputDescription(
                    new ComplexOutputDescription(id, defaultFormat, supported));

        } else if (literalOutput != null) {
            String dataType = literalOutput.getDataType().getStringValue();
            Set<String> uoms = decode(literalOutput.getUOMs());
            processDescription.addProcessOutputDescription(
                    new LiteralOutputDescription(id, dataType, uoms));
        }
    }

}
