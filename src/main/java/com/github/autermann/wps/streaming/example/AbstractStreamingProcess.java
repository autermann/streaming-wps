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
package com.github.autermann.wps.streaming.example;

import java.net.URI;

import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.ProcessConfiguration;

public abstract class AbstractStreamingProcess extends AbstractAnnotatedAlgorithm {

    @LiteralDataOutput(identifier = "process-id")
    public URI getProcessID() {
        return getConfig().getProcessID().toURI();
    }

    @LiteralDataOutput(identifier = "socket-uri")
    public URI getInputSocketURI() {
        return getConfig().getSocketURI();
    }

    @Execute
    public void execute()
            throws ExceptionReport {
        MessageBroker.getInstance().addProcess(getConfig());
    }

    protected abstract ProcessConfiguration getConfig();
}
