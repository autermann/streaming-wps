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
package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import net.opengis.ows.x11.BoundingBoxType;
import net.opengis.wps.x100.InputReferenceType;

import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class Data {

    private Data() {
    }

    public static class BoundingBoxData extends Data {
        private final BoundingBoxType xml;

        public BoundingBoxData(BoundingBoxType xml) {
            this.xml = checkNotNull(xml);
        }

        public BoundingBoxType getXml() {
            return this.xml;
        }

    }

    public static class ReferencedData extends Data {
        private final InputReferenceType xml;

        public ReferencedData(InputReferenceType xml) {
            this.xml = checkNotNull(xml);
        }

        public InputReferenceType getXml() {
            return xml;
        }
    }

    public static class LiteralData extends Data {
        private final String type;
        private final String value;
        private final Optional<String> uom;

        public LiteralData(String type, String value, String uom) {
            this.type = checkNotNull(type);
            this.value = checkNotNull(emptyToNull(value));
            this.uom = Optional.fromNullable(emptyToNull(uom));
        }

        public LiteralData(String type, String value) {
            this(type, value, null);
        }

        public String getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }

        public Optional<String> getUom() {
            return this.uom;
        }
    }

    public static class ComplexData extends Data {
        private final Format format;
        private final String content;

        public ComplexData(String mimeType, String content) {
            this(new Format(mimeType), content);
        }

        public ComplexData(String mimeType, String encoding, String content) {
            this(new Format(mimeType, encoding), content);
        }

        public ComplexData(String mimeType, String encoding,
                           String schema, String content) {
            this(new Format(mimeType, encoding, schema), content);
        }

        public ComplexData(Format format, String content) {
            this.format = checkNotNull(format);
            this.content = checkNotNull(content);
        }

        public String getMimeType() {
            return this.format.getMimeType();
        }

        public Optional<String> getEncoding() {
            return this.format.getEncoding();
        }

        public Optional<String> getSchema() {
            return this.format.getSchema();
        }

        public String getContent() {
            return this.content;
        }

        public Format getFormat() {
            return this.format;
        }
    }
}
