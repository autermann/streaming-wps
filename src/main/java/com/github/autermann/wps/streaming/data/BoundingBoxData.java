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

import net.opengis.ows.x11.BoundingBoxType;

import org.n52.wps.util.XMLBeansHelper;

import com.google.common.base.Objects;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class BoundingBoxData extends Data {

    private final BoundingBoxType xml;

    public BoundingBoxData(BoundingBoxType xml) {
        this.xml = checkNotNull(xml);
    }

    public BoundingBoxType getXml() {
        return this.xml;
    }

    @Override
    public BoundingBoxData asBoundingBox() {
        return this;
    }

    @Override
    public boolean isBoundingBox() {
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(getXml().xmlText(XMLBeansHelper.getXmlOptions()))
                .toString();
    }

}
