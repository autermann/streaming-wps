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
import java.io.InputStreamReader;

import org.n52.wps.FormatDocument;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;

import com.github.autermann.wps.commons.Format;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractParser implements IParser {

    private final Format format;
    private final Class<? extends IData> bindingClass;

    public AbstractParser(Format format, Class<? extends IData> bindingClass) {
        this.format = Preconditions.checkNotNull(format);
        this.bindingClass = Preconditions.checkNotNull(bindingClass);
    }

    @Override
    public boolean isSupportedSchema(String schema) {
        return this.format.getSchema().equals(schema);
    }

    @Override
    public boolean isSupportedFormat(String format) {
        return this.format.getMimeType().equals(format);
    }

    @Override
    public boolean isSupportedEncoding(String encoding) {
        return this.format.getEncoding().equals(encoding);
    }

    @Override
    public String[] getSupportedSchemas() {
        return this.format.getSchema().isPresent()
               ? new String[] { this.format.getSchema().get() }
               : new String[] {};
    }

    @Override
    public String[] getSupportedEncodings() {
        return this.format.getEncoding().isPresent()
               ? new String[] { this.format.getEncoding().get() }
               : new String[] {};
    }

    @Override
    public String[] getSupportedFormats() {
        return new String[] { this.format.getMimeType() };
    }

    @Override
    public FormatDocument.Format[] getSupportedFullFormats() {
        FormatDocument.Format f = FormatDocument.Format.Factory.newInstance();
        f.setMimetype(this.format.getMimeType());
        if (this.format.getEncoding().isPresent()) {
            f.setEncoding(this.format.getEncoding().get());
        }
        if (this.format.getSchema().isPresent()) {
            f.setSchema(this.format.getSchema().get());
        }
        return new FormatDocument.Format[] { f };
    }

    @Override
    public boolean isSupportedDataBinding(Class<?> clazz) {
        return this.bindingClass.equals(clazz);
    }

    @Override
    public Class<?>[] getSupportedDataBindings() {
        return new Class<?>[] { this.bindingClass };
    }

    @Override
    public IData parseBase64(InputStream input, String mimeType, String schema) {
        return parse(BaseEncoding.base64()
                .decodingStream(new InputStreamReader(input)), mimeType, schema);
    }

    @Override
    public IData parse(InputStream input, String mimeType, String schema) {
        try {
            return parse(input);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract IData parse(InputStream input) throws IOException;

}
