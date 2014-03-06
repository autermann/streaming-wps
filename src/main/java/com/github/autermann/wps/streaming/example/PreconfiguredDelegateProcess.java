package com.github.autermann.wps.streaming.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.CapabilitiesConfiguration;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.ProcessConfiguration;
import com.github.autermann.wps.streaming.delegate.DelegatingProcessConfiguration;
import com.github.autermann.wps.streaming.delegate.ProcessDescriptionParser;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
@Algorithm(
        identifier = "com.github.autermann.wps.streaming.example.PreconfiguredDelegateProcess",
        statusSupported = true,
        storeSupported = false,
        version = "1.0.0")
public class PreconfiguredDelegateProcess extends AbstractAnnotatedAlgorithm {
    private static final String DELEGATE_ID = "com.github.autermann.wps.streaming.TestAlgorithm";
    private URI socketURI;
    private URI processID;
    @LiteralDataOutput(identifier = "process-id")
    public URI getProcessID() { return this.processID; }
    @LiteralDataOutput(identifier = "socket-uri")
    public URI getSocketURI() { return this.socketURI; }
    @Execute
    public void run() throws IOException, URISyntaxException {
        ProcessConfiguration configuration = buildConfiguration();
        this.processID = configuration.getProcessID().toURI();
        this.socketURI = configuration.getSocketURI();
        MessageBroker.getInstance().addProcess(configuration);
    }

    private ProcessConfiguration buildConfiguration()
            throws IOException {
        DelegatingProcessConfiguration configuration
                = new DelegatingProcessConfiguration();
        URI baseURI = URI.create(CapabilitiesConfiguration.ENDPOINT_URL);
        configuration.setRemoteURL(baseURI);
        ProcessDescription description = fetchDescription(baseURI, DELEGATE_ID);
        configuration.setProcessDescription(description);
        return configuration;
    }

    private ProcessDescription fetchDescription(URI base, String id)
            throws IOException {
        try {
            URI uri = new URIBuilder(base)
                    .addParameter("service", "WPS")
                    .addParameter("version", "1.0.0")
                    .addParameter("request", "DescribeProcess")
                    .addParameter("identifier", id).build();
            HttpGet req = new HttpGet(uri);
            try (CloseableHttpClient c = HttpClients.createDefault();
                 CloseableHttpResponse res = c.execute(req)) {
                try (InputStream in = res.getEntity().getContent()) {
                    ProcessDescriptionParser p = new ProcessDescriptionParser();
                    return (ProcessDescription) p.parse(in, null, null).getPayload();
                }
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
