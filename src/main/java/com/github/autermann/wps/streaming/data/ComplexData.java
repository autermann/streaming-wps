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

import com.github.autermann.wps.commons.Format;
import com.google.common.base.Objects;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ComplexData extends Data {

    private final Format format;
    private final String content;

    public ComplexData(Format format, String content) {
        this.format = checkNotNull(format);
        this.content = checkNotNull(content);
    }

    public String getContent() {
        return this.content;
    }

    public Format getFormat() {
        return this.format;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public ComplexData asComplex() {
        return this;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("format", getFormat())
                .add("content", getContent())
                .toString();
    }
}
