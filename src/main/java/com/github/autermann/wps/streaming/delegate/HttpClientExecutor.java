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
import java.io.InputStream;
import java.net.URI;

import net.opengis.wps.x100.ExecuteDocument;

import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class HttpClientExecutor extends DelegatingExecutor {
    private static final ContentType CONTENT_TYPE = ContentType
            .create("application/xml");
    private final URI remoteURL;
    private final CloseableHttpClient client;

    public HttpClientExecutor(MessageReceiver callback,
                              DelegatingProcessConfiguration configuration) {
        super(callback, configuration);
        this.remoteURL = checkNotNull(configuration.getRemoteURL());
        this.client = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    @Override
    protected InputStream send(ExecuteDocument request) throws IOException {
        HttpPost httpRequest = new HttpPost(this.remoteURL);
        httpRequest.setEntity(EntityBuilder.create()
                .setContentType(CONTENT_TYPE)
                .setStream(request.newInputStream()).build());
        CloseableHttpResponse httpResponse = client.execute(httpRequest);
        return httpResponse.getEntity().getContent();
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }
}
