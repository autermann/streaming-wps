package com.github.autermann.wps.streaming.example;

import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;


import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.google.common.base.Optional;

@Algorithm(identifier
        = "com.github.autermann.wps.streaming.example.StatefulSummingProcess",
           version = "1.0.0")
public class StatefulSummingProcess extends AbstractAnnotatedAlgorithm {
    private final Configuration config = new Configuration();
    @Execute
    public void execute() { MessageBroker.getInstance().addProcess(config); }
    @LiteralDataOutput(identifier = "process-id")
    public URI getProcessID() { return config.getProcessID().toURI(); }
    @LiteralDataOutput(identifier = "socket-uri")
    public URI getInputSocketURI() { return config.getSocketURI(); }
    @LiteralDataInput(identifier = "initialValue")
    public void setInitialValue(double initialValue) {
        config.setInitialValue(BigDecimal.valueOf(initialValue));
    }

    private static class Configuration extends InitalValueProcessConfiguration<BigDecimal> {
        private static final OwsCodeType SUMMAND = new OwsCodeType("summand");
        private static final OwsCodeType SUM = new OwsCodeType("sum");
        @Override public StreamingExecutor createStreamingExecutor(MessageReceiver callback) {
            return new StreamingExecutor(callback, this) {
                private final AtomicReference<BigDecimal> state
                        = new AtomicReference<>(getInitialValue());

                void add(BigDecimal amount) {
                    for (;;) {
                        BigDecimal old = state.get();
                        BigDecimal sum = old.add(amount);
                        if (this.state.compareAndSet(old, sum)) {
                            return;
                        }
                    }
                }

                @Override protected Optional<ProcessOutputs> execute(ProcessInputs inputs) throws StreamingError {
                    for (ProcessInput in : inputs.getInputs(SUMMAND)) {
                        if (in.isData() && in.asData().getData().isLiteral() &&
                            in.asData().getData().asLiteral().isNumeric()) {
                            add(in.asData().getData().asLiteral().asDecimal());
                        } else {
                            throw new StreamingError("Incompatible input type",
                                    StreamingError.INVALID_PARAMETER_VALUE);
                        }
                    }
                    return Optional.absent();
                }

                @Override protected Optional<OutputMessage> onStop() {
                    OutputMessage message = new OutputMessage();
                    ProcessOutputs outputs = new ProcessOutputs();
                    outputs.addOutput(SUM, LiteralData.of(state.get()));
                    message.setPayload(outputs);
                    message.setProcessID(getProcessID());
                    return Optional.of(message);
                }

                @Override public void close() {/* do nothing */}
            };
        }
    }
}
