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
package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class LiteralData extends Data {
    public static final String XS_BOOLEAN = "xs:boolean";
    public static final String XS_BYTE = "xs:byte";
    public static final String XS_SHORT = "xs:short";
    public static final String XS_INTEGER = "xs:integer";
    public static final String XS_INT = "xs:int";
    public static final String XS_LONG = "xs:long";
    public static final String XS_DOUBLE = "xs:double";
    public static final String XS_FLOAT = "xs:float";
    public static final String XS_STRING = "xs:string";
    public static final String XS_ANY_URI = "xs:anyURI";
    public static final String XS_BASE64_BINARY = "xs:base64Binary";
    public static final String XS_DECIMAL = "xs:decimal";

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

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public LiteralData asLiteral() {
        return this;
    }

    public boolean isBoolean() {
        return getType().equalsIgnoreCase(XS_BOOLEAN);
    }

    public boolean isByte() {
        return getType().equalsIgnoreCase(XS_BYTE);
    }

    public boolean isShort() {
        return getType().equalsIgnoreCase(XS_SHORT);
    }

    public boolean isInteger() {
        return getType().equalsIgnoreCase(XS_INTEGER);
    }

    public boolean isInt() {
        return getType().equalsIgnoreCase(XS_INT);
    }

    public boolean isLong() {
        return getType().equalsIgnoreCase(XS_LONG);
    }

    public boolean isDouble() {
        return getType().equalsIgnoreCase(XS_DOUBLE);
    }

    public boolean isFloat() {
        return getType().equalsIgnoreCase(XS_FLOAT);
    }

    public boolean isString() {
        return getType().equalsIgnoreCase(XS_STRING);
    }

    public boolean isAnyURI() {
        return getType().equalsIgnoreCase(XS_ANY_URI);
    }

    public boolean isBase64Binary() {
        return getType().equalsIgnoreCase(XS_BASE64_BINARY);
    }

    public boolean isDecimal() {
        return getType().equalsIgnoreCase(XS_DECIMAL);
    }

    public boolean isNumeric() {
        return isByte() ||
               isShort() ||
               isInt() ||
               isLong() ||
               isInteger() ||
               isFloat() ||
               isDouble() ||
               isDecimal();
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(getValue());
    }

    public byte asByte() {
        return Byte.parseByte(getValue());
    }

    public short asShort() {
        return Short.parseShort(getValue());
    }

    public BigInteger asInteger() {
        return new BigInteger(getValue());
    }

    public int asInt() {
        return Integer.parseInt(getValue());
    }

    public long asLong() {
        return Long.parseLong(getValue());
    }

    public double asDouble() {
        return Double.parseDouble(getValue());
    }

    public float asFloat() {
        return Float.parseFloat(getValue());
    }

    public String asString() {
        return getValue();
    }

    public URI asAnyURI() {
        return URI.create(getValue());
    }

    public byte[] asBase64Binary() {
        return BaseEncoding.base64().decode(getValue());
    }

    public BigDecimal asDecimal() {
        return new BigDecimal(getValue());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .add("type", getType())
                .add("value", getValue())
                .add("uom", getUom())
                .toString();
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
