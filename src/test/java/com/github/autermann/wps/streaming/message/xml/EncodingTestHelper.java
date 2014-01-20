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
package com.github.autermann.wps.streaming.message.xml;

import java.math.BigInteger;
import java.util.Arrays;

import net.opengis.ows.x11.BoundingBoxType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class EncodingTestHelper {

    static BoundingBoxType createBoundingBox() {
        BoundingBoxType boundingBox = BoundingBoxType.Factory.newInstance();
        boundingBox.setCrs("EPSG:4326");
        boundingBox.setDimensions(BigInteger.valueOf(2l));
        boundingBox.setLowerCorner(Arrays.asList("52.2", "7"));
        boundingBox.setUpperCorner(Arrays.asList("55.2", "15"));
        return boundingBox;
    }
}
