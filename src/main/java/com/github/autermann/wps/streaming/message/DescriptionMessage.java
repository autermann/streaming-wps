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
package com.github.autermann.wps.streaming.message;

import java.net.URI;

import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.util.SoapConstants;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DescriptionMessage extends Message {
    private StreamingProcessDescription payload;

    @Override
    public URI getSOAPAction() {
        return SoapConstants.getDescriptonActionURI();
    }

    public StreamingProcessDescription getPayload() {
        return payload;
    }

    public void setPayload(StreamingProcessDescription payload) {
        this.payload = payload;
    }

}
