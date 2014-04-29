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
package com.github.autermann.wps.streaming.message;


import java.net.URI;

import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.util.SoapConstants;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class OutputMessage extends Message {
    private ProcessOutputs payload;

    @Override
    public URI getSOAPAction() {
        return SoapConstants.getOutputActionURI();
    }

    public ProcessOutputs getPayload() {
        return this.payload;
    }

    public void setPayload(ProcessOutputs payload) {
        this.payload = payload;
    }

}
