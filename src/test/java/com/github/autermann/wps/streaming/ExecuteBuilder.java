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
package com.github.autermann.wps.streaming;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.opengis.ows.x11.BoundingBoxType;

import org.apache.http.client.utils.URIBuilder;
import org.apache.xmlbeans.XmlException;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.data.BoundingBoxData;
import com.github.autermann.wps.streaming.data.ComplexData;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.ReferencedData;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.delegate.DelegatingStreamingAlgorithm;
import com.github.autermann.wps.streaming.example.AddAlgorithm;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.github.autermann.wps.streaming.util.SchemaConstants;
import com.github.autermann.wps.streaming.xml.InputsDocument;
import com.github.autermann.wps.streaming.xml.InputsType;

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
            throws URISyntaxException, IOException, ExceptionReport,
                   XmlException {
        OwsCodeType processId
                = new OwsCodeType(DelegatingStreamingAlgorithm.PROCESS_ID);

        ProcessInputs inputs = new ProcessInputs()
                .addDataInput(INPUT_REMOTE_WPS_URL, LiteralData.of(WPS_URL))
                .addDataInput(INPUT_REMOTE_PROCESS_DESCRIPTION,
                              new ReferencedData(new URIBuilder(WPS_URL)
                        .addParameter(PARAMETER_SERVICE, SERVICE_TYPE_WPS)
                        .addParameter(PARAMETER_VERSION, SERVICE_VERSION_100)
                        .addParameter(PARAMETER_REQUEST, OPERATION_DESCRIBE_PROCESS)
                        .addParameter(PARAMETER_IDENTIFIER, AddAlgorithm.class
                                .getName())
                        .build(), PROCESS_DESCRIPTION_FORMAT));

//        StaticInputsDocument doc = StaticInputsDocument.Factory.newInstance();
        InputsDocument doc = InputsDocument.Factory.newInstance();
        InputsType d = doc.addNewInputs();//doc.addNewStaticInputs();

        CommonEncoding ce = new CommonEncoding();
        ce.encodeInput(d.addNewStreamingInput(),
                       new DataProcessInput("intput1", LiteralData.of("input1")));
        ce.encodeInput(d.addNewStreamingInput(),
                       new DataProcessInput("intput2", new ComplexData(new Format("application/xml", "UTF-8"), "<hello>world</hello>")));
        ce.encodeInput(d.addNewStreamingInput(),
                       new DataProcessInput("intput3", new BoundingBoxData(BoundingBoxType.Factory
                .parse("<wps:BoundingBoxData xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" crs=\"EPSG:4326\" dimensions=\"2\">\n" +
                       "        <ows:LowerCorner>52.2 7.0</ows:LowerCorner>\n" +
                       "        <ows:UpperCorner>55.2 15.0</ows:UpperCorner>\n" +
                       "      </wps:BoundingBoxData>"))));
        ce
                .encodeInput(d.addNewStreamingInput(), new DataProcessInput("input4", new ReferencedData(URI
                                        .create("http://geoprocessing.demo.52north.org:8080/geoserver/wfs?service=WFS&version=1.0.0&request=GetFeature&typeName=topp:tasmania_roads&srs=EPSG:4326&outputFormat=GML3"), new Format("application/xml", "UTF-8", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd"))));

        System.out.println(doc.xmlText(SchemaConstants.XML_OPTIONS));
//        try (CloseableHttpClient httpClient = HttpClients.createDefault();
//             WPSClient client = new WPSClient(WPS_URL, httpClient)) {
//            client.execute(processId, inputs,
//                           Lists.newArrayList(OUTPUT_PROCESS_ID,
//                                              OUTPUT_SOCKET_URI));
//        }
    }
}
