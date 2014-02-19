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

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingExecutor extends StreamingExecutor {
    private final WPSClient client;
    private final ProcessDescription description;

    public DelegatingExecutor(MessageReceiver callback,
                              DelegatingProcessConfiguration configuration) {
        super(callback, configuration);
        this.description = checkNotNull(configuration.getProcessDescription());
        this.client = new WPSClient(configuration.getRemoteURL(), createHttpClient());
    }

    private CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

    @Override
    protected ProcessOutputs execute(ProcessInputs inputs) throws StreamingError {
        try {
            return client.execute(description, inputs);
        } catch (ExceptionReport ex) {
            throw new StreamingError("Delegated process failed",
                    StreamingError.REMOTE_COMPUTATION_ERROR, ex);
        }
    }
}
