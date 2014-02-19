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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.streaming.CallbackJobExecutor;
import com.github.autermann.wps.streaming.ProcessConfiguration;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingProcessConfiguration extends ProcessConfiguration {
    private URI remoteURL;
    private ProcessInputs staticInputs = new ProcessInputs();
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

    public ProcessInputs getStaticInputs() {
        return staticInputs;
    }

    public void setStaticInputs(ProcessInputs staticInputs) {
        this.staticInputs = checkNotNull(staticInputs);
    }

    @Override
    public CallbackJobExecutor createExecutor(MessageReceiver callback) {
        return new HttpClientExecutor(callback, this);
    }

}
