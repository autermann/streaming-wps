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

import java.net.URI;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public final class SoapConstants {
    private static final String ACTION_URI_PREFIX
            = "https://github.com/autermann/streaming-wps/";

    private static final URI INPUT_ACTION
            = URI.create(ACTION_URI_PREFIX + "input");
    private static final URI REQUEST_OUTPUT_ACTION
            = URI.create(ACTION_URI_PREFIX + "request-output");
    private static final URI STOP_ACTION
            = URI.create(ACTION_URI_PREFIX + "stop");
    private static final URI OUTPUT_ACTION
            = URI.create(ACTION_URI_PREFIX + "output");
    private static final URI ERROR_ACTION
            = URI.create(ACTION_URI_PREFIX + "error");
    private static final URI DESCRIBE_ACTION
            = URI.create(ACTION_URI_PREFIX + "describe");
    private static final URI DESCRIPTION_ACTION
            = URI.create(ACTION_URI_PREFIX + "description");

    private SoapConstants() {
    }

    public static String getInputAction() {
        return getInputAction().toString();
    }

    public static URI getInputActionURI() {
        return INPUT_ACTION;
    }

    public static String getOutputRequestAction() {
        return getOutputRequestActionURI().toString();
    }

    public static URI getOutputRequestActionURI() {
        return REQUEST_OUTPUT_ACTION;
    }

    public static String getStopAction() {
        return getStopActionURI().toString();
    }

    public static URI getStopActionURI() {
        return STOP_ACTION;
    }

    public static String getOutputAction() {
        return getOutputActionURI().toString();
    }

    public static URI getOutputActionURI() {
        return OUTPUT_ACTION;
    }

    public static String getErrorAction() {
        return getErrorActionURI().toString();
    }

    public static URI getErrorActionURI() {
        return ERROR_ACTION;
    }

    public static URI getDescribeActionURI() {
        return DESCRIBE_ACTION;
    }

    public static String getDescribeAction() {
        return getDescribeActionURI().toString();
    }

    public static URI getDescriptonActionURI() {
        return DESCRIPTION_ACTION;
    }

    public static String getDescriptonAction() {
        return getDescriptonAction().toString();
    }
}
