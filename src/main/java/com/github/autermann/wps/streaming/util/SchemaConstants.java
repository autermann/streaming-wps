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
package com.github.autermann.wps.streaming.util;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface SchemaConstants {
    String AN_XSI_SCHEMA_LOCATION = "schemaLocation";
    String EN_WSA_ACTION = "Action";
    String NS_OWS_11 = "http://www.opengis.net/ows/1.1";
    String NS_OWS_PREFIX = "ows";
    String NS_SOAP_12 = "http://www.w3.org/2003/05/soap-envelope";
    String NS_SOAP_12_PREFIX = "soap";
    String NS_SOAP_12_SCHEMA_LOCATION
            = "http://www.w3.org/2003/05/soap-envelope";
    String NS_STREAM = "https://github.com/autermann/streaming-wps";
    String NS_STREAM_PREFIX = "stream";
    String NS_STREAM_SCHEMA_LOCATION
            = "https://autermann.github.com/streaming-wps/streaming-wps.xsd";
    String NS_WPS_100 = "http://www.opengis.net/wps/1.0.0";
    String NS_WPS_PREFIX = "wps";
    String NS_WSA = "http://www.w3.org/2005/08/addressing";
    String NS_WSA_PREFIX = "wsa";
    String NS_XLINK = "http://www.w3.org/1999/xlink";
    String NS_XLINK_PREFIX = "xlink";
    String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    String NS_XSI_PREFIX = "xsi";

    String SCHEMA_LOCATION_PROCESS_DESCRIPTIONS
            = "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_response.xsd";
    String SCHEMA_LOCATION_STATIC_INPUTS
            = "https://autermann.github.com/streaming-wps/staticInputs.xsd";

    QName QN_SCHEMA_LOCATION = new QName(NS_XSI, AN_XSI_SCHEMA_LOCATION);
    QName QN_WSA_ACTION = new QName(NS_WSA, EN_WSA_ACTION);

    XmlOptions XML_OPTIONS = new XmlOptions()
            .setSaveAggressiveNamespaces()
            .setSaveNamespacesFirst()
            .setSaveSuggestedPrefixes(ImmutableMap.<String, String>builder()
                    .put(NS_OWS_11, NS_OWS_PREFIX)
                    .put(NS_SOAP_12, NS_SOAP_12_PREFIX)
                    .put(NS_STREAM, NS_STREAM_PREFIX)
                    .put(NS_WPS_100, NS_WPS_PREFIX)
                    .put(NS_WSA, NS_WSA_PREFIX)
                    .put(NS_XLINK, NS_XLINK_PREFIX)
                    .put(NS_XSI, NS_XSI_PREFIX)
                    .build())
            .setSavePrettyPrint();

    String SCHEMA_LOCATIONS = Joiner.on(" ").withKeyValueSeparator(" ")
            .join(ImmutableMap.<String, String>builder()
                    .put(NS_SOAP_12, NS_SOAP_12_SCHEMA_LOCATION)
                    .put(NS_STREAM, NS_STREAM_SCHEMA_LOCATION)
                    .build());

}
