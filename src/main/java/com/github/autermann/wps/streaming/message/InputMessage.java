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

import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class InputMessage extends Message {

    private ProcessInputs payload;

    @Override
    public URI getSOAPAction() {
        return SoapConstants.getInputActionURI();
    }

    public ProcessInputs getPayload() {
        return this.payload;
    }

    public void setPayload(ProcessInputs inputs) {
        this.payload = Preconditions.checkNotNull(inputs);
    }

}
