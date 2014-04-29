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
package com.github.autermann.wps.streaming.delegate;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.xml.ProcessDescriptionDecoder;
import com.github.autermann.wps.streaming.util.SchemaConstants;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescriptionParser extends AbstractParser {
    private final ProcessDescriptionDecoder decoder
            = new ProcessDescriptionDecoder();

    public ProcessDescriptionParser() {
        super(new Format(MEDIA_TYPE_TEXT_XML, ENCODING_UTF8,
                         SchemaConstants.SCHEMA_LOCATION_PROCESS_DESCRIPTIONS),
              ProcessDescriptionBinding.class);
    }

    @Override
    protected ProcessDescriptionBinding parse(InputStream input)
            throws IOException {
        try {
            ProcessDescriptionsDocument doc
                    = ProcessDescriptionsDocument.Factory.parse(input);
            ProcessDescriptionType xbPd = doc.getProcessDescriptions()
                    .getProcessDescriptionArray(0);
            return new ProcessDescriptionBinding(decoder
                    .decodeProcessDescription(xbPd));
        } catch (XmlException ex) {
            throw new RuntimeException(ex);
        }
    }
}
