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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class LiteralData extends Data {
    private static final String XS_BOOLEAN = "xs:boolean";
    private static final String XS_BYTE = "xs:byte";
    private static final String XS_SHORT = "xs:short";
    private static final String XS_INTEGER = "xs:integer";
    private static final String XS_INT = "xs:int";
    private static final String XS_LONG = "xs:long";
    private static final String XS_DOUBLE = "xs:double";
    private static final String XS_FLOAT = "xs:float";
    private static final String XS_STRING = "xs:string";
    private static final String XS_ANY_URI = "xs:anyURI";
    private static final String XS_BASE64_BINARY = "xs:base64Binary";
    private static final String XS_DECIMAL = "xs:decimal";

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

    public static LiteralData of(boolean value) {
        return new LiteralData(XS_BOOLEAN, String.valueOf(value));
    }

    public static LiteralData of(boolean value, String uom) {
        return new LiteralData(XS_BOOLEAN, String.valueOf(value), uom);
    }

    public static LiteralData of(byte value) {
        return new LiteralData(XS_BYTE, String.valueOf(value));
    }

    public static LiteralData of(byte value, String uom) {
        return new LiteralData(XS_BYTE, String.valueOf(value), uom);
    }

    public static LiteralData of(short value) {
        return new LiteralData(XS_SHORT, String.valueOf(value));
    }

    public static LiteralData of(short value, String uom) {
        return new LiteralData(XS_SHORT, String.valueOf(value), uom);
    }

    public static LiteralData of(BigInteger value) {
        return new LiteralData(XS_INTEGER, String.valueOf(value));
    }

    public static LiteralData of(BigInteger value, String uom) {
        return new LiteralData(XS_INTEGER, String.valueOf(value), uom);
    }

    public static LiteralData of(int value) {
        return new LiteralData(XS_INT, String.valueOf(value));
    }

    public static LiteralData of(int value, String uom) {
        return new LiteralData(XS_INT, String.valueOf(value), uom);
    }

    public static LiteralData of(long value) {
        return new LiteralData(XS_LONG, String.valueOf(value));
    }

    public static LiteralData of(long value, String uom) {
        return new LiteralData(XS_LONG, String.valueOf(value), uom);
    }

    public static LiteralData of(double value) {
        return new LiteralData(XS_DOUBLE, String.valueOf(value));
    }

    public static LiteralData of(double value, String uom) {
        return new LiteralData(XS_DOUBLE, String.valueOf(value), uom);
    }

    public static LiteralData of(float value) {
        return new LiteralData(XS_FLOAT, String.valueOf(value));
    }

    public static LiteralData of(float value, String uom) {
        return new LiteralData(XS_FLOAT, String.valueOf(value), uom);
    }

    public static LiteralData of(String value) {
        return new LiteralData(XS_STRING, String.valueOf(value));
    }

    public static LiteralData of(String value, String uom) {
        return new LiteralData(XS_STRING, String.valueOf(value), uom);
    }

    public static LiteralData of(URI value) {
        return new LiteralData(XS_ANY_URI, String.valueOf(value));
    }

    public static LiteralData of(URI value, String uom) {
        return new LiteralData(XS_ANY_URI, String.valueOf(value), uom);
    }

    public static LiteralData of(byte[] value) {
        return new LiteralData(XS_BASE64_BINARY, BaseEncoding.base64()
                .encode(value));
    }

    public static LiteralData of(byte[] value, String uom) {
        return new LiteralData(XS_BASE64_BINARY, BaseEncoding.base64()
                .encode(value), uom);
    }

    public static LiteralData of(BigDecimal value) {
        return new LiteralData(XS_DECIMAL, String.valueOf(value));
    }

    public static LiteralData of(BigDecimal value, String uom) {
        return new LiteralData(XS_DECIMAL, String.valueOf(value), uom);
    }

}
