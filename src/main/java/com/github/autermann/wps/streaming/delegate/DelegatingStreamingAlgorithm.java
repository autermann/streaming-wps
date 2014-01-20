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

import java.net.URI;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.data.ProcessInputs;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
@Algorithm(
        identifier = "com.github.autermann.wps.DelegatingStreamingAlgorithm",
        title = "Delegating Streaming Algorithm",
        abstrakt = "Generic Streaming Algorithm to convert any existing WPS " +
                   "process into a streaming process.",
        statusSupported = true,
        storeSupported = false,
        version = "1.0.0")
public class DelegatingStreamingAlgorithm extends AbstractAnnotatedAlgorithm {
    private final DelegatingProcessConfiguration configuration
            = new DelegatingProcessConfiguration();

    @ComplexDataInput(
            identifier = "remote-process-description",
            title = "Remote Process Description",
            abstrakt = "Process description of the process to proxy",
            minOccurs = 1, maxOccurs = 1,
            binding = ProcessDescriptionBinding.class)
    public void setProcessDescription(ProcessDescription processDescription) {
        this.configuration.setProcessDescription(processDescription);
    }

    @LiteralDataInput(
            identifier = "remote-wps-url",
            title = "Remote WPS URL",
            abstrakt = "The URL of the WPS server to proxy",
            binding = LiteralAnyURIBinding.class)
    public void setRemoteURI(URI remoteURI) {
        this.configuration.setRemoteURL(remoteURI);
    }

    @ComplexDataInput(
            identifier = "static-inputs",
            title = "Static Inputs",
            abstrakt = "Common inputs for all streaming iterations",
            minOccurs = 0, maxOccurs = 1,
            binding = StaticInputBinding.class)
    public void setStaticInputs(ProcessInputs staticInputs) {
        this.configuration.setStaticInputs(staticInputs);
    }

    @LiteralDataOutput(
            identifier = "process-id",
            title = "The Process ID",
            abstrakt = "The process id of this instance.",
            binding = LiteralAnyURIBinding.class)
    public URI getProcessID() {
        return configuration.getProcessID().toURI();

    }

    @Execute
    public void run() {
        MessageBroker.getInstance().addProcess(configuration);
    }
}
