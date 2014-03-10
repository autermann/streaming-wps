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
package com.github.autermann.wps.streaming.example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.input.InputOccurence;
import com.github.autermann.wps.commons.description.input.LiteralInputDescription;
import com.github.autermann.wps.commons.description.ows.OwsAllowedValues;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.commons.description.ows.OwsLanguageString;
import com.github.autermann.wps.streaming.MessageBroker;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.StreamingProcessDescription;
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
           version = StatefulSummingProcess.PROCESS_VERSION)
public class StatefulSummingProcess extends AbstractAnnotatedAlgorithm {
    protected static final String PROCESS_VERSION = "1.0.0";
    private final Configuration config = new Configuration();
    @Execute
    public void execute() throws ExceptionReport { MessageBroker.getInstance().addProcess(config); }
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

        @Override
        public StreamingProcessDescription describe()
                throws ExceptionReport {
            StreamingProcessDescription pd = new StreamingProcessDescription(
                    new OwsCodeType(getProcessID().toString()),
                    new OwsLanguageString(StatefulSummingProcess.class.getSimpleName()),
                    null, // no abstract
                    PROCESS_VERSION,
                    true, // one final result
                    false // no intermediate results
            );
            pd.addInput(new LiteralInputDescription(
                    SUMMAND,
                    new OwsLanguageString(SUMMAND.getValue()),
                    null, // no abstract
                    new InputOccurence(BigInteger.ONE,
                                       BigInteger.valueOf(Integer.MAX_VALUE)),
                    LiteralData.XS_DECIMAL,
                    OwsAllowedValues.any(),
                    null, // no default UOM
                    null  // no supported UOMs
            ));
            return pd;
        }
    }
}
