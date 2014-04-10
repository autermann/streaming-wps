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
import java.util.List;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.input.LiteralInputDescription;
import com.github.autermann.wps.commons.description.output.LiteralOutputDescription;
import com.github.autermann.wps.commons.description.ows.OwsAllowedValues;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.ProcessConfiguration;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.data.LiteralData;
import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.data.input.ProcessInput;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.github.autermann.wps.streaming.data.output.ProcessOutputs;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.google.common.base.Function;
import com.google.common.base.Optional;

@Algorithm(identifier
        = "com.github.autermann.wps.streaming.example.MovingAverage",
           version = StatefulSummingProcess.PROCESS_VERSION)
public class MovingAverage extends AbstractStreamingProcess {
    private static final String PROCESS_VERSION = "1.0.0";
    private static final OwsCodeType VALUE = new OwsCodeType("value");
    private static final OwsCodeType MOVING_AVERAGE = new OwsCodeType("movingAverage");
    private static final OwsCodeType SAMPLE_SIZE = new OwsCodeType("sampleSize");

    private final Configuration config = new Configuration();

    @Override
    protected ProcessConfiguration getConfig() {
        return this.config;
    }

    @LiteralDataInput(identifier = "sampleWindowSize")
    public void setSampleWindowSize(long sampleWindowSize) {
        config.setSampleWindowSize(sampleWindowSize);
    }

    private class Configuration extends ProcessConfiguration {
        private long sampleWindowSize;

        private void setSampleWindowSize(long sampleWindowSize) {
            this.sampleWindowSize = sampleWindowSize;
        }

        public long getSampleWindowSize() {
            return sampleWindowSize;
        }

        @Override
        public StreamingExecutor createStreamingExecutor(MessageReceiver callback) {
            return new Executor(callback, this);
        }

        @Override
        public StreamingProcessDescription describe()
                throws ExceptionReport {
            return StreamingProcessDescription.builder()
                    .withIdentifier(getProcessID().toString())
                    .withTitle(MovingAverage.class.getSimpleName())
                    .withVersion(PROCESS_VERSION)
                    .withInput(LiteralInputDescription.builder()
                            .withIdentifier(VALUE)
                            .withTitle(VALUE.getValue())
                            .withDataType(LiteralData.XS_DECIMAL)
                            .withAllowedValues(OwsAllowedValues.any()))
                    .withOutput(LiteralOutputDescription.builder()
                            .withIdentifier(MOVING_AVERAGE)
                            .withTitle(MOVING_AVERAGE.getValue())
                            .withDataType(LiteralData.XS_DECIMAL))
                    .hasFinalResult(true)
                    .hasIntermediateResults(true)
                    .build();
        }
    }

    private class Executor extends StreamingExecutor {
        private final long sampleWindowSize;
        private long count = 0L;
        private BigDecimal sum = BigDecimal.ZERO;

        Executor(MessageReceiver callback, Configuration conf) {
            super(callback, conf);
            this.sampleWindowSize = conf.getSampleWindowSize();
        }

        private synchronized Optional<BigDecimal> calculate(BigDecimal value) {
            sum.add(value);
            if (++count == sampleWindowSize) {
                BigDecimal average = sum.divide(BigDecimal.valueOf(count));
                sum = BigDecimal.ZERO;
                count = 0L;
                return Optional.of(average);
            }
            return Optional.absent();
        }

        @Override
        protected Optional<ProcessOutputs> execute(ProcessInputs inputs)
                throws StreamingError {
            BigDecimal value = getBigDecimalInput(inputs, VALUE);
            Optional<BigDecimal> result = calculate(value);
            return result.transform(new Function<BigDecimal, ProcessOutputs>() {
                @Override
                public ProcessOutputs apply(BigDecimal result) {
                    return createProcessOutputs(result, sampleWindowSize);
                }
            });
        }

        @Override
        protected Optional<OutputMessage> onStop() {
            if (count >= 0) {
                OutputMessage message = new OutputMessage();
                message.setPayload(createProcessOutputs(sum, count));
                message.setProcessID(getProcessID());
                return Optional.of(message);
            } else {
                return Optional.absent();
            }
        }

        private ProcessOutputs createProcessOutputs(BigDecimal sum, long n) {
            BigDecimal average = sum.divide(BigDecimal.valueOf(n));
            return new ProcessOutputs()
                    .addOutput(MOVING_AVERAGE, LiteralData.of(average))
                    .addOutput(SAMPLE_SIZE, LiteralData.of(n));
        }

        private BigDecimal getBigDecimalInput(ProcessInputs inputs,
                                              OwsCodeType id)
                throws StreamingError {
            List<ProcessInput> values = inputs.getInputs(id);
            if (values.isEmpty()) {
                throw new StreamingError("Missing input parameter",
                                         StreamingError.MISSING_PARAMETER_VALUE);
            }
            if (values.size() > 1) {
                throw new StreamingError("Invalid input parameter cardinality",
                                         StreamingError.INVALID_PARAMETER_VALUE);
            }
            ProcessInput in = values.get(0);
            if (in.isData() && in.asData().getData().isLiteral() &&
                in.asData().getData().asLiteral().isNumeric()) {
                return in.asData().getData().asLiteral().asDecimal();
            } else {
                throw new StreamingError("Incompatible input type",
                                         StreamingError.INVALID_PARAMETER_VALUE);
            }
        }

        @Override
        public void close() {
            /* do nothing */
        }
    }
}
