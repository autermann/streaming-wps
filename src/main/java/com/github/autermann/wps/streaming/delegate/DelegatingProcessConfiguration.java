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

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.net.URI;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.commons.description.input.BoundingBoxInputDescription;
import com.github.autermann.wps.commons.description.input.ComplexInputDescription;
import com.github.autermann.wps.commons.description.input.InputOccurence;
import com.github.autermann.wps.commons.description.input.LiteralInputDescription;
import com.github.autermann.wps.commons.description.input.ProcessInputDescription;
import com.github.autermann.wps.commons.description.output.BoundingBoxOutputDescription;
import com.github.autermann.wps.commons.description.output.ComplexOutputDescription;
import com.github.autermann.wps.commons.description.output.LiteralOutputDescription;
import com.github.autermann.wps.commons.description.output.ProcessOutputDescription;
import com.github.autermann.wps.streaming.ProcessConfiguration;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingProcessConfiguration extends ProcessConfiguration {
    private URI remoteURL;
    private ProcessDescription processDescription;

    public URI getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(URI remoteURL) {
        this.remoteURL = checkNotNull(remoteURL);
    }

    public ProcessDescription getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(ProcessDescription processDescription) {
        this.processDescription = checkNotNull(processDescription);
    }

    @Override
    public StreamingExecutor createStreamingExecutor(MessageReceiver callback) {
        return new DelegatingExecutor(callback, this);
    }

    @Override
    public StreamingProcessDescription describe()
            throws ExceptionReport {
        return new ProcessDescriptionTransformer()
                .setStaticInputs(getStaticInputs())
                .setProcessID(getProcessID())
                .setDescription(getProcessDescription())
                .transform();

    }

}
