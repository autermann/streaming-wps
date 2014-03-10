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

import java.io.Closeable;
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.util.XMLBeansHelper;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.commons.description.output.ProcessOutputDescription;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.StreamingError.RemoteException;
import com.github.autermann.wps.streaming.data.input.DataProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.xml.CommonEncoding;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class WPSClient implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(WPSClient.class);
    private static final String WPS_SERVICE_VERSION = "1.0.0";
    private static final String WPS_SERVICE_TYPE = "WPS";
    private static final ContentType CONTENT_TYPE = ContentType
            .create("application/xml");
    private final CommonEncoding encoding = new CommonEncoding();

    private final CloseableHttpClient client;
    private final URI uri;

    public WPSClient(URI uri, CloseableHttpClient client) {
        this.uri = Preconditions.checkNotNull(uri);
        this.client = Preconditions.checkNotNull(client);
    }

    @Override
    public void close()
            throws IOException {
        this.client.close();
    }

    private String executeRemote(String request)
            throws IOException {
        HttpPost httpRequest = new HttpPost(this.uri);
        log.debug("Request: {}", request);
        httpRequest.setEntity(EntityBuilder.create()
                .setContentType(CONTENT_TYPE).setText(request).build());
        try (CloseableHttpResponse httpResponse = client.execute(httpRequest);
             InputStream entity = httpResponse.getEntity().getContent();
             InputStreamReader reader
                = new InputStreamReader(entity, Charsets.UTF_8)) {
            String response = CharStreams.toString(reader);
            log.debug("Response: {}", response);
            return response;
        }
    }

    public ExecuteResponseDocument executeRemote(ExecuteDocument xbRequest)
            throws IOException, ExceptionReport {
        String response = executeRemote(xbRequest.xmlText(XMLBeansHelper.getXmlOptions()));
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

    private ExceptionReport decodeExceptionReport(
            ExceptionReportDocument document) {
        ExceptionReportDocument.ExceptionReport xbExceptionReport = document.getExceptionReport();
        StreamingError ex = new StreamingError("Remote computation failed", StreamingError.REMOTE_COMPUTATION_ERROR);
        for (ExceptionType xbException : xbExceptionReport.getExceptionArray()) {
            ex.addRemoteException(new RemoteException(
                    xbException.getExceptionTextArray(),
                    "RemoteException" + ((xbException.getExceptionCode() != null)
                        ? xbException.getExceptionCode() : ""),
                    xbException.getLocator()));
        }
        return ex;
    }

    public ProcessOutputs execute(ProcessDescription description, ProcessInputs inputs) throws ExceptionReport {
        try {
            ExecuteDocument xbRequest = encodeRequest(description.getID(), inputs);
            includeOutputs(xbRequest, description.getOutputs(), description);
            ExecuteResponseDocument xbResponse = executeRemote(xbRequest);
            return decodeResponse(xbResponse);
        } catch (IOException ex) {
            throw remoteComputationError("Remote computation failed", ex);
        }
    }

     public ProcessOutputs execute(OwsCodeType id, ProcessInputs inputs) throws ExceptionReport {
        try {
            ExecuteDocument xbRequest = encodeRequest(id, inputs);
            ExecuteResponseDocument xbResponse = executeRemote(xbRequest);
            return decodeResponse(xbResponse);
        } catch (IOException ex) {
            throw remoteComputationError("Remote computation failed", ex);
        }
    }

    public ProcessOutputs execute(OwsCodeType id, ProcessInputs inputs, Iterable<OwsCodeType> outputs) throws ExceptionReport {
        try {
            ExecuteDocument xbRequest = encodeRequest(id, inputs);
            ExecuteResponseDocument xbResponse = executeRemote(xbRequest);
            return decodeResponse(xbResponse);
        } catch (IOException ex) {
            throw remoteComputationError("Remote computation failed", ex);
        }
    }

    private void includeOutputs(ExecuteDocument document,
                                Iterable<OwsCodeType> outputs,
                                ProcessDescription description) {
        Execute execute = document.getExecute();
        ResponseFormType responseForm = execute.addNewResponseForm();
        ResponseDocumentType responseDocument = responseForm.addNewResponseDocument();
        for (OwsCodeType id : outputs) {
            DocumentOutputDefinitionType xbOutput = responseDocument.addNewOutput();
            id.encodeTo(xbOutput.addNewIdentifier());
            if (description != null) {
                ProcessOutputDescription output = description.getOutput(id);
                if (output.isComplex()) {
                    if (description.isStoreSupported()) {
                        xbOutput.setAsReference(true);
                    }
                    output.asComplex().getDefaultFormat().encodeTo(xbOutput);
                } else if (output.isBoundingBox()) {
                    if (output.asBoundingBox().getDefaultCRS().isPresent()) {
                        xbOutput.setUom(output.asBoundingBox().getDefaultCRS().get().getValue());
                    }
                } else if (output.isLiteral()) {
                    if (output.asLiteral().getDefaultUOM().isPresent()) {
                        xbOutput.setUom(output.asLiteral().getDefaultUOM().get().getValue());
                    }
                }
            }
        }
    }

    private ExecuteDocument encodeRequest(OwsCodeType processId, ProcessInputs inputs)
        throws ExceptionReport {
        try {
            ExecuteDocument document = ExecuteDocument.Factory.newInstance();
            Execute execute = document.addNewExecute();
            execute.setService(WPS_SERVICE_TYPE);
            execute.setVersion(WPS_SERVICE_VERSION);
            processId.encodeTo(execute.addNewIdentifier());
            DataInputsType xbInputs = execute.addNewDataInputs();
            for (ProcessInput input : inputs.getInputs()) {
                encoding.encodeInput(xbInputs.addNewInput(), (DataProcessInput) input);
            }
            return document;
        } catch (XmlException ex) {
            throw new ExceptionReport("Could not encode execute request",
                    ExceptionReport.NO_APPLICABLE_CODE, ex);
        }
    }

    public ProcessOutputs decodeResponse(ExecuteResponseDocument response) throws ExceptionReport {
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
            throw new ExceptionReport("Can not decode execute response",
                                     ExceptionReport.NO_APPLICABLE_CODE, ex);
        }
    }

    public static StreamingError remoteComputationError(String message,
                                                         Throwable cause) {
        return new StreamingError(message, StreamingError.REMOTE_COMPUTATION_ERROR, cause);
    }

    public static ExceptionReport remoteComputationError(String message) {
        return new ExceptionReport(message, StreamingError.REMOTE_COMPUTATION_ERROR);
    }
}
