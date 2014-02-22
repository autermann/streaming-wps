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
package com.github.autermann.wps.streaming.message.xml;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import net.opengis.ows.x11.ExceptionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.util.SoapConstants;
import com.github.autermann.wps.streaming.xml.ErrorMessageDocument;
import com.github.autermann.wps.streaming.xml.ErrorMessageType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ErrorMessageEncoding extends AbstractMessageEncoding<ErrorMessage> {
    public static final String JAVA_ROOT_CAUSE_EXCEPTION_CODE = "JAVA_RootCause";
    public static final String JAVA_STACK_TRACE_EXCEPTION_CODE = "JAVA_StackTrace";

    @Override
    protected ErrorMessage create() {
        return new ErrorMessage();
    }

    @Override
    public URI getAction() {
        return SoapConstants.getErrorActionURI();
    }

    @Override
    protected XmlObject createBody(ErrorMessage message) throws XmlException {
        ErrorMessageDocument document = ErrorMessageDocument.Factory
                .newInstance();
        ErrorMessageType xbErrorMessage = document.addNewErrorMessage();
        if (message.getProcessID() != null) {
            xbErrorMessage.addNewProcessID()
                    .setStringValue(message.getProcessID().toString());
        }
        ExceptionType xbException = xbErrorMessage.addNewException();
        StreamingError error = message.getPayload();
        xbException.setExceptionCode(error.getErrorKey());
        xbException.addExceptionText(error.getMessage());
        encodeStackTrace(xbErrorMessage, error);
        encodeRootCause(xbErrorMessage, error);
        if (error.getLocator() != null) {
            xbException.setLocator(error.getLocator());
        }
        return document;
    }

    private void encodeRootCause(ErrorMessageType xbErrorMessage,
                                 StreamingError error) {
        if (error.getCause() != null) {
            ExceptionType xbException = xbErrorMessage.addNewException();
            if (error.getCause().getMessage() != null) {
                xbException.addExceptionText(error.getCause().getMessage());
            }
            xbException.addExceptionText(encodeStackTrace(error.getCause()));
            xbException.setExceptionCode(JAVA_ROOT_CAUSE_EXCEPTION_CODE);
        }

    }

    private void encodeStackTrace(ErrorMessageType xbErrorMessage,
                                  StreamingError error) {
        ExceptionType xbException = xbErrorMessage.addNewException();
        xbException.addExceptionText(encodeStackTrace(error));
        xbException.setExceptionCode(JAVA_STACK_TRACE_EXCEPTION_CODE);
    }

    @Override
    protected void decodeBody(ErrorMessage message, XmlObject body) throws
            XmlException {
        if (!(body instanceof ErrorMessageDocument)) {
            throw new XmlException("Expected stream:ErrorMessage");
        }
        ErrorMessageDocument document = (ErrorMessageDocument) body;
        ErrorMessageType xbMessage = document.getErrorMessage();
        for (ExceptionType xbException : xbMessage.getExceptionArray()) {
            if (!xbException.getExceptionCode().equals(JAVA_ROOT_CAUSE_EXCEPTION_CODE) &&
                !xbException.getExceptionCode().equals(JAVA_STACK_TRACE_EXCEPTION_CODE)) {
                message.setPayload(new StreamingError(
                        xbException.getExceptionTextArray(0),
                        xbException.getExceptionCode(),
                        xbException.getLocator()));
            }
        }
    }

    private String encodeStackTrace(Throwable t) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        t.printStackTrace(p);
        w.flush();
        w.flush();
        return w.toString();
    }
}
