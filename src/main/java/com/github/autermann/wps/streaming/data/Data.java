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

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class Data {

    Data() {
    }

    public boolean isBoundingBox() {
        return false;
    }

    public boolean isComplex() {
        return false;
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isReference() {
        return false;
    }

    public ReferencedData asReference() {
        throw new UnsupportedOperationException();
    }

    public LiteralData asLiteral() {
        throw new UnsupportedOperationException();
    }

    public ComplexData asComplex() {
        throw new UnsupportedOperationException();
    }

    public BoundingBoxData asBoundingBox() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract String toString();
}
