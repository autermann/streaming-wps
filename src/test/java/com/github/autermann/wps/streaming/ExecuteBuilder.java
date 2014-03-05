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
package com.github.autermann.wps.streaming;

import com.github.autermann.wps.streaming.example.AddAlgorithm;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.ReferencedData;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.delegate.DelegatingStreamingAlgorithm;
import com.github.autermann.wps.streaming.delegate.WPSClient;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.google.common.collect.Lists;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ExecuteBuilder {
    public static final URI WPS_URL
            = URI.create("http://localhost:12121/WebProcessingService");
    public static final Format PROCESS_DESCRIPTION_FORMAT
            = new Format("text/xml", "UTF-8", SchemaConstants.SCHEMA_LOCATION_PROCESS_DESCRIPTIONS);
    public static final OwsCodeType INPUT_REMOTE_WPS_URL
            = new OwsCodeType(DelegatingStreamingAlgorithm.INPUT_REMOTE_WPS_URL);
    public static final OwsCodeType INPUT_REMOTE_PROCESS_DESCRIPTION
            = new OwsCodeType(DelegatingStreamingAlgorithm.INPUT_REMOTE_PROCESS_DESCRIPTION);
    public static final OwsCodeType OUTPUT_SOCKET_URI
            = new OwsCodeType(DelegatingStreamingAlgorithm.OUTPUT_SOCKET_URI);
    public static final OwsCodeType OUTPUT_PROCESS_ID
            = new OwsCodeType(DelegatingStreamingAlgorithm.OUTPUT_PROCESS_ID);
    public static final String PARAMETER_IDENTIFIER = "identifier";
    public static final String PARAMETER_REQUEST = "request";
    public static final String PARAMETER_VERSION = "version";
    public static final String PARAMETER_SERVICE = "service";
    public static final String OPERATION_DESCRIBE_PROCESS = "DescribeProcess";
    public static final String SERVICE_VERSION_100 = "1.0.0";
    public static final String SERVICE_TYPE_WPS = "WPS";

    public static void main(String[] args)
            throws URISyntaxException, IOException, ExceptionReport {
        OwsCodeType processId
                = new OwsCodeType(DelegatingStreamingAlgorithm.PROCESS_ID);

        ProcessInputs inputs = new ProcessInputs()
                .addDataInput(INPUT_REMOTE_WPS_URL, LiteralData.of(WPS_URL))
                .addDataInput(INPUT_REMOTE_PROCESS_DESCRIPTION,
                              new ReferencedData(new URIBuilder(WPS_URL)
                .addParameter(PARAMETER_SERVICE, SERVICE_TYPE_WPS)
                .addParameter(PARAMETER_VERSION, SERVICE_VERSION_100)
                .addParameter(PARAMETER_REQUEST, OPERATION_DESCRIBE_PROCESS)
                .addParameter(PARAMETER_IDENTIFIER, AddAlgorithm.class.getName())
                .build(), PROCESS_DESCRIPTION_FORMAT));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             WPSClient client = new WPSClient(WPS_URL, httpClient)) {
            client.execute(processId, inputs,
                           Lists.newArrayList(OUTPUT_PROCESS_ID,
                                              OUTPUT_SOCKET_URI));
        }
    }
}
