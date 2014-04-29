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

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescriptionBinding implements IComplexData {
    private static final long serialVersionUID = -1780445465464228923L;
    private final ProcessDescription processDescription;

    public ProcessDescriptionBinding(ProcessDescription processDescription) {
        this.processDescription = Preconditions.checkNotNull(processDescription);
    }

    @Override
    public void dispose() {
    }

    @Override
    public ProcessDescription getPayload() {
        return processDescription;
    }

    @Override
    public Class<?> getSupportedClass() {
        return ProcessDescription.class;
    }

}
