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

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
@Algorithm(
        //this one seems be be ignored...
        identifier = DelegatingStreamingAlgorithm.PROCESS_ID,
        title = "Delegating Streaming Algorithm",
        abstrakt = "Generic Streaming Algorithm to convert any existing WPS " +
                   "process into a streaming process.",
        statusSupported = true,
        storeSupported = false,
        version = "1.0.0")
public class DelegatingStreamingAlgorithm extends AbstractAnnotatedAlgorithm {
    public static final String PROCESS_ID
            = "com.github.autermann.wps.streaming.delegate.DelegatingStreamingAlgorithm";
    public static final String INPUT_REMOTE_PROCESS_DESCRIPTION
            = "remote-process-description";
    public static final String INPUT_REMOTE_WPS_URL = "remote-wps-url";
    public static final String INPUT_STATIC_INPUTS = "static-inputs";
    public static final String OUTPUT_PROCESS_ID = "process-id";
    public static final String OUTPUT_SOCKET_URI = "socket-uri";

    private final DelegatingProcessConfiguration configuration
            = new DelegatingProcessConfiguration();

    @ComplexDataInput(
            identifier = INPUT_REMOTE_PROCESS_DESCRIPTION,
            title = "Remote Process Description",
            abstrakt = "Process description of the process to proxy",
            minOccurs = 1, maxOccurs = 1,
            binding = ProcessDescriptionBinding.class)
    public void setProcessDescription(ProcessDescription processDescription) {
        if (processDescription != null) {
            this.configuration.setProcessDescription(processDescription);
        }
    }

    @LiteralDataInput(
            identifier = INPUT_REMOTE_WPS_URL,
            title = "Remote WPS URL",
            abstrakt = "The URL of the WPS server to proxy",
            binding = LiteralAnyURIBinding.class)
    public void setRemoteURI(URI remoteURI) {
        if (remoteURI != null) {
            this.configuration.setRemoteURL(remoteURI);
        }
    }

    @ComplexDataInput(
            identifier = INPUT_STATIC_INPUTS,
            title = "Static Inputs",
            abstrakt = "Common inputs for all streaming iterations",
            minOccurs = 0, maxOccurs = 1,
            binding = StaticInputBinding.class)
    public void setStaticInputs(ProcessInputs staticInputs) {
        if (staticInputs != null) {
            this.configuration.setStaticInputs(staticInputs);
        }
    }

    @LiteralDataOutput(
            identifier = OUTPUT_PROCESS_ID,
            title = "The Process ID",
            abstrakt = "The process id of this instance.",
            binding = LiteralAnyURIBinding.class)
    public URI getProcessID() {
        return configuration.getProcessID().toURI();
    }

    @LiteralDataOutput(
            identifier = OUTPUT_SOCKET_URI,
            title = "The WebSocket URI",
            abstrakt = "The WebSocket URI to supply subsequent inputs " +
                       "and request intermediate outputs.",
            binding = LiteralAnyURIBinding.class)
    public URI getInputSocketURI() {
        return this.configuration.getSocketURI();
    }

    @Execute
    public void run() {
        MessageBroker.getInstance().addProcess(configuration);
    }
}
