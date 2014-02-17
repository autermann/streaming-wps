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
package com.github.autermann.wps.streaming.delegate;

import org.n52.wps.io.data.IComplexData;

import com.github.autermann.wps.streaming.data.ProcessInputs;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StaticInputBinding implements IComplexData {

    private final ProcessInputs inputs;

    public StaticInputBinding(ProcessInputs inputs) {
        this.inputs = Preconditions.checkNotNull(inputs);
    }

    @Override
    public ProcessInputs getPayload() {
        return this.inputs;
    }

    @Override
    public Class<ProcessInputs> getSupportedClass() {
        return ProcessInputs.class;
    }

    @Override
    public void dispose() {
    }
}