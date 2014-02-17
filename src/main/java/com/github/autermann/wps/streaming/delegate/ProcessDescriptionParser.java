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

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.google.common.net.MediaType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescriptionParser extends AbstractParser {

    public ProcessDescriptionParser() {
        super(new Format(
                MediaType.XML_UTF_8.toString(),
                DEFAULT_ENCODING,
                SchemaConstants.SCHEMA_LOCATION_PROCESS_DESCRIPTIONS),
              ProcessDescriptionBinding.class);
    }

    @Override
    protected ProcessDescriptionBinding parse(InputStream input)
            throws
            IOException {
        try {
            ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(input);
            ProcessDescriptionType xbPd = doc.getProcessDescriptions().getProcessDescriptionArray(0);
            return new ProcessDescriptionBinding(ProcessDescription.of(xbPd));
        } catch (XmlException ex) {
            throw new RuntimeException(ex);
        }
    }
}
