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
package com.github.autermann.wps.streaming.delegate;

import org.n52.wps.io.data.IComplexData;

import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StaticInputBinding implements IComplexData {
    private static final long serialVersionUID = -2403736388760012220L;

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
