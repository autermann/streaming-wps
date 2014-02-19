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

import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.InputType;

import org.apache.xmlbeans.XmlException;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.github.autermann.wps.streaming.xml.StaticInputsDocument;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StaticInputParser extends AbstractParser {
    private final CommonEncoding commonEncoding = new CommonEncoding();

    public StaticInputParser() {
        super(new Format(MEDIA_TYPE_TEXT_XML, ENCODING_UTF8,
                         SchemaConstants.SCHEMA_LOCATION_STATIC_INPUTS),
              StaticInputBinding.class);
    }

    @Override
    protected StaticInputBinding parse(InputStream input) throws IOException {
        try {
            StaticInputsDocument document = StaticInputsDocument.Factory.parse(input);
            DataInputsType xbStaticInputs = document.getStaticInputs();
            ProcessInputs staticInputs = new ProcessInputs();
            for (InputType xbInput : xbStaticInputs.getInputArray()) {
                staticInputs.addInput(commonEncoding.decodeInput(xbInput));
            }
            return new StaticInputBinding(staticInputs);
        } catch (XmlException ex) {
            throw new RuntimeException(ex);
        }
    }
}
