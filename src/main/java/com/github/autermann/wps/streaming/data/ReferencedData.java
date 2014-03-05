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
package com.github.autermann.wps.streaming.data;

import java.net.URI;

import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputReferenceType.Header;

import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.commons.Format;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ReferencedData extends Data {

    private final URI bodyReference;
    private final XmlObject body;
    private final Method method;
    private final Multimap<String, String> headers;
    private final URI href;
    private final Format format;

    public ReferencedData(URI href, Multimap<String, String> headers,
                          Format format,
                          XmlObject body) {
        this(href, Method.POST, headers, format, null, body);
    }

    public ReferencedData(URI href, Multimap<String, String> headers,
                          Format format,
                          URI body) {
        this(href, Method.POST, headers, format, body, null);
    }

    public ReferencedData(URI href, Multimap<String, String> headers,
                          Format format) {
        this(href, Method.GET, headers, format, null, null);
    }

    public ReferencedData(URI href, Multimap<String, String> headers,
                          XmlObject body) {
        this(href, Method.POST, headers, null, null, body);
    }

    public ReferencedData(URI href, Multimap<String, String> headers, URI body) {
        this(href, Method.POST, headers, null, body, null);
    }

    public ReferencedData(URI href, Multimap<String, String> headers) {
        this(href, Method.GET, headers, null, null, null);
    }

    public ReferencedData(URI href, Format format, XmlObject body) {
        this(href, Method.POST, null, format, null, body);
    }

    public ReferencedData(URI href, Format format, URI body) {
        this(href, Method.POST, null, format, body, null);
    }

    public ReferencedData(URI href, Format format) {
        this(href, Method.GET, null, format, null, null);
    }

    public ReferencedData(URI href, XmlObject body) {
        this(href, Method.POST, null, null, null, body);
    }

    public ReferencedData(URI href, URI body) {
        this(href, Method.POST, null, null, body, null);
    }

    public ReferencedData(URI href) {
        this(href, Method.GET, null, null, null, null);
    }

    private ReferencedData(URI href, Method method,
                           Multimap<String, String> headers, Format format,
                           URI bodyReference, XmlObject body) {
        this.bodyReference = bodyReference;
        this.body = body;
        this.method = method;
        this.headers = headers == null ? HashMultimap.<String, String>create()
                       : headers;
        this.href = Preconditions.checkNotNull(href);
        this.format = format == null ? new Format(null, null, null) : format;
    }

    public Optional<URI> getBodyReference() {
        return Optional.fromNullable(this.bodyReference);
    }

    public Optional<XmlObject> getBody() {
        return Optional.fromNullable(this.body);
    }

    public Method getMethod() {
        return method;
    }

    public Multimap<String, String> getHeaders() {
        return headers;
    }

    public URI getHref() {
        return href;
    }

    public Format getFormat() {
        return this.format;
    }

    public void encodeTo(InputReferenceType xb) {
        if (getBody().isPresent()) {
            xb.setBody(getBody().get());
        }
        if (getBodyReference().isPresent()) {
            xb.addNewBodyReference()
                    .setHref(getBodyReference().get().toASCIIString());
        }
        getFormat().encodeTo(xb);
        for (String key : getHeaders().keySet()) {
            for (String value : getHeaders().get(key)) {
                Header h = xb.addNewHeader();
                h.setKey(key);
                h.setValue(value);
            }
        }
        xb.setHref(getHref().toASCIIString());
        xb.setMethod(InputReferenceType.Method.Enum.forString(getMethod()
                .toString()));
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public ReferencedData asReference() {
        return this;
    }

    public static ReferencedData of(InputReferenceType xb) {
        XmlObject body = null;
        URI bodyReference = null;
        if (xb.isSetBody()) {
            body = xb.getBody();
        }
        if (xb.isSetBodyReference()) {
            bodyReference = URI.create(xb.getBodyReference().getHref());
        }
        Method method = Method.GET;
        URI href = URI.create(xb.getHref());
        if (xb.isSetMethod()) {
            switch (xb.getMethod().toString()) {
                case "GET":
                    method = Method.GET;
                    break;
                case "POST":
                    method = Method.POST;
                    break;
            }
        }
        Multimap<String, String> headers = HashMultimap.create();
        if (xb.getHeaderArray() != null) {
            for (InputReferenceType.Header header : xb.getHeaderArray()) {
                headers.put(header.getKey(), header.getValue());
            }
        }
        Format format
                = new Format(xb.getMimeType(), xb.getEncoding(), xb.getSchema());
        return new ReferencedData(href, method, headers, format, bodyReference, body);
    }

    public enum Method {
        GET,
        POST
    }

}
