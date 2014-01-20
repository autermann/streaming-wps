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

import java.io.IOException;
import java.io.InputStream;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.streaming.CallbackJobExecutor;
import com.github.autermann.wps.streaming.data.OwsCodeType;
import com.github.autermann.wps.streaming.data.ProcessInput;
import com.github.autermann.wps.streaming.data.ProcessInput.DataInput;
import com.github.autermann.wps.streaming.data.ProcessInputs;
import com.github.autermann.wps.streaming.data.ProcessOutputs;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.github.autermann.wps.streaming.message.xml.ErrorMessageEncoding;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class DelegatingExecutor extends CallbackJobExecutor {
    private static final String WPS_SERVICE_VERSION = "1.0.0";
    private static final String WPS_SERVICE_TYPE = "WPS";
    private final OwsCodeType processId;
    private final CommonEncoding encoding = new CommonEncoding();

    public DelegatingExecutor(MessageReceiver callback,
                              OwsCodeType processId) {
        super(callback);
        this.processId = Preconditions.checkNotNull(processId);
    }

    @Override
    protected ProcessOutputs execute(ProcessInputs inputs)
            throws StreamingError {
        try {
            ExecuteDocument request = encodeRequest(inputs);
            ExecuteResponseDocument response = execute(request);
            ProcessOutputs outputs = decodeResponse(response);
            return outputs;
        } catch (StreamingError ex) {
            throw ex; // do not wrap streaming errors
        } catch (ExceptionReport ex) {
            throw new StreamingError("Delegated process failed",
                                     StreamingError.REMOTE_COMPUTATION_ERROR, ex);
        } catch (IOException ex) {
            throw new StreamingError("Delegated process failed",
                                     StreamingError.REMOTE_COMPUTATION_ERROR, ex);
        }
    }

    private ExecuteResponseDocument execute(ExecuteDocument request)
            throws ExceptionReport, IOException {
        InputStream response = null;
        try {
            response = send(request);
            return parseResponse(response);
        } finally {
            Closeables.close(response, true);
        }
    }

    private ExecuteResponseDocument parseResponse(InputStream in)
            throws IOException, ExceptionReport {
        try {
            XmlObject o = XmlObject.Factory.parse(in);
            if (o instanceof ExecuteResponseDocument) {
                return (ExecuteResponseDocument) o;
            } else if (o instanceof ExceptionReportDocument) {
                return parseExceptionReport((ExceptionReportDocument) o);
            } else {
                throw new StreamingError("Can not parse response",
                                         StreamingError.REMOTE_COMPUTATION_ERROR);
            }
        } catch (XmlException ex) {
            throw new StreamingError("Can not parse response",
                                     StreamingError.REMOTE_COMPUTATION_ERROR, ex);
        }
    }

    private <T> T parseExceptionReport(ExceptionReportDocument document)
            throws ExceptionReport, StreamingError {
        ExceptionReportDocument.ExceptionReport xbExceptionReport
                = document.getExceptionReport();
        for (ExceptionType xbException : xbExceptionReport
                .getExceptionArray()) {
            String code = xbException.getExceptionCode();
            if (code == null ||
                code
                    .equals(ErrorMessageEncoding.JAVA_ROOT_CAUSE_EXCEPTION_CODE) ||
                code
                    .equals(ErrorMessageEncoding.JAVA_STACK_TRACE_EXCEPTION_CODE)) {
                continue;
            }
            String locator = xbException.getLocator();
            String message = null;
            if (xbException.getExceptionTextArray().length > 0) {
                xbException.getExceptionTextArray(0);
            }
            throw new ExceptionReport(message, code, locator);
        }
        throw new StreamingError("Can not parse ExceptionReport",
                                 StreamingError.REMOTE_COMPUTATION_ERROR);
    }

    private ExecuteDocument encodeRequest(ProcessInputs inputs)
            throws StreamingError {
        try {
            ExecuteDocument document = ExecuteDocument.Factory.newInstance();
            Execute execute = document.addNewExecute();
            execute.setService(WPS_SERVICE_TYPE);
            execute.setVersion(WPS_SERVICE_VERSION);
            encoding.encodeCodeType(execute.addNewIdentifier(), processId);
            DataInputsType xbDataInputs = execute.addNewDataInputs();
            for (ProcessInput input : inputs.getInputs()) {
                encoding.encodeInput(xbDataInputs.addNewInput(),
                                     (DataInput) input);
            }
            return document;
        } catch (XmlException ex) {
            throw new StreamingError("Could not encode execute request",
                                     StreamingError.NO_APPLICABLE_CODE, ex);

        }
    }

    private ProcessOutputs decodeResponse(ExecuteResponseDocument response)
            throws StreamingError {
        try {
            ProcessOutputs outputs = new ProcessOutputs();
            OutputDataType[] xbOutputs = response.getExecuteResponse()
                    .getProcessOutputs().getOutputArray();
            for (OutputDataType xbOutput : xbOutputs) {
                outputs.addOutput(encoding.decodeOutput(xbOutput));
            }
            return outputs;
        } catch (XmlException ex) {
            throw new StreamingError("Can not decode execute response",
                                     StreamingError.NO_APPLICABLE_CODE, ex);
        }
    }

    protected abstract InputStream send(ExecuteDocument request) throws
            IOException;
}
