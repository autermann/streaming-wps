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
import java.io.InputStreamReader;
import java.net.URI;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.ResponseFormType;

import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.commons.description.ProcessOutputDescription;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.github.autermann.wps.streaming.message.xml.ErrorMessageEncoding;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingExecutor extends StreamingExecutor {
    private static final ContentType CONTENT_TYPE = ContentType
            .create("application/xml");
    private static final String WPS_SERVICE_VERSION = "1.0.0";
    private static final String WPS_SERVICE_TYPE = "WPS";
    private final ProcessDescription description;
    private final CommonEncoding encoding = new CommonEncoding();
    private final URI remoteURL;
    private final CloseableHttpClient client;

    public DelegatingExecutor(MessageReceiver callback,
                              DelegatingProcessConfiguration configuration) {
        super(callback, configuration);
        this.description = checkNotNull(configuration.getProcessDescription());
        this.remoteURL = checkNotNull(configuration.getRemoteURL());
        this.client = createHttpClient();
    }

    private CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    private String executeRemote(String request) throws IOException {
        HttpPost httpRequest = new HttpPost(this.remoteURL);
        httpRequest.setEntity(EntityBuilder.create()
                .setContentType(CONTENT_TYPE).setText(request).build());
        try (CloseableHttpResponse httpResponse = client.execute(httpRequest);
             InputStream entity = httpResponse.getEntity().getContent();
             InputStreamReader reader = new InputStreamReader(entity, Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

    private ExecuteResponseDocument executeRemote(ExecuteDocument xbRequest)
            throws IOException, StreamingError {
        String response = executeRemote(xbRequest.xmlText());
        try {
            XmlObject o = XmlObject.Factory.parse(response);
            if (o instanceof ExecuteResponseDocument) {
                return (ExecuteResponseDocument) o;
            } else if (o instanceof ExceptionReportDocument) {
                throw decodeExceptionReport((ExceptionReportDocument) o);
            } else {
                throw remoteComputationError("Can not parse response");
            }
        } catch (XmlException ex) {
            throw remoteComputationError("Can not parse response", ex);
        }
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

    protected ProcessDescription getDescription() {
        return description;
    }

    @Override
    protected ProcessOutputs execute(ProcessInputs inputs) throws StreamingError {
        try {
            ExecuteDocument xbRequest = encodeRequest(inputs);
            ExecuteResponseDocument xbResponse = executeRemote(xbRequest);
            ProcessOutputs outputs = decodeResponse(xbResponse);
            return outputs;
        } catch (IOException ex) {
            throw remoteComputationError("Delegated process failed", ex);
        }
    }


    private ExecuteDocument encodeRequest(ProcessInputs inputs)
        throws StreamingError {
        try {
            ExecuteDocument document = ExecuteDocument.Factory.newInstance();
            Execute execute = document.addNewExecute();
            execute.setService(WPS_SERVICE_TYPE);
            execute.setVersion(WPS_SERVICE_VERSION);
            getDescription().getID().encodeTo(execute.addNewIdentifier());
            DataInputsType xbInputs = execute.addNewDataInputs();
            for (ProcessInput input : inputs.getInputs()) {
                encoding.encodeInput(xbInputs.addNewInput(), (DataProcessInput) input);
            }
            ResponseFormType responseForm = execute.addNewResponseForm();
            ResponseDocumentType responseDocument = responseForm.addNewResponseDocument();
            for (OwsCodeType id : getDescription().getOutputs()) {
                ProcessOutputDescription output = getDescription().getOutput(id);
                DocumentOutputDefinitionType xbOutput= responseDocument.addNewOutput();
                output.getID().encodeTo(xbOutput.addNewIdentifier());
                if (output.isComplex()) {
                    if (getDescription().isStoreSupported()) {
                        xbOutput.setAsReference(true);
                    }
                    output.asComplex().getDefaultFormat().encodeTo(xbOutput);
                } else if (output.isBoundingBox()) {
                    if (output.asBoundingBox().getDefaultCRS().isPresent()) {
                        xbOutput.setUom(output.asBoundingBox().getDefaultCRS().get());
                    }
                } else if (output.isLiteral()) {
                    if (output.asLiteral().getDefaultUOM().isPresent()) {
                        xbOutput.setUom(output.asLiteral().getDefaultUOM().get().getValue());
                    }
                }
            }
            return document;
        } catch (XmlException ex) {
            throw new StreamingError("Could not encode execute request",
                    StreamingError.NO_APPLICABLE_CODE, ex);
        }
    }

    private ProcessOutputs decodeResponse(ExecuteResponseDocument response) throws StreamingError {
        try {
            ExecuteResponse executeResponse = response.getExecuteResponse();
            ExecuteResponse.ProcessOutputs xbProcessOutputs = executeResponse.getProcessOutputs();
            ProcessOutputs outputs = new ProcessOutputs();
            OutputDataType[] xbOutputs = xbProcessOutputs.getOutputArray();
            for (OutputDataType xbOutput : xbOutputs) {
                outputs.addOutput(encoding.decodeOutput(xbOutput));
            }
            return outputs;
        } catch (XmlException ex) {
            throw new StreamingError("Can not decode execute response",
                                     StreamingError.NO_APPLICABLE_CODE, ex);
        }
    }

    private StreamingError decodeExceptionReport(ExceptionReportDocument document) {
        ExceptionReportDocument.ExceptionReport xbExceptionReport = document.getExceptionReport();
        for (ExceptionType xbException : xbExceptionReport.getExceptionArray()) {
            String code = xbException.getExceptionCode();
            if (code == null ||
                        code.equals(ErrorMessageEncoding.JAVA_ROOT_CAUSE_EXCEPTION_CODE) ||
                        code.equals(ErrorMessageEncoding.JAVA_STACK_TRACE_EXCEPTION_CODE)) {
                continue;
            }
            String message = null;
            if (xbException.getExceptionTextArray().length > 0) {
                message = xbException.getExceptionTextArray(0);
            }
            ExceptionReport ex = new ExceptionReport(message, code, xbException.getLocator());
            return remoteComputationError("Remote computation failed", ex);
        }
        return remoteComputationError("Can not parse ExceptionReport");
    }

    private static StreamingError remoteComputationError(String message, Throwable cause) {
        return new StreamingError(message, StreamingError.REMOTE_COMPUTATION_ERROR, cause);
    }

    private static StreamingError remoteComputationError(String message) {
        return new StreamingError(message, StreamingError.REMOTE_COMPUTATION_ERROR);
    }
}
