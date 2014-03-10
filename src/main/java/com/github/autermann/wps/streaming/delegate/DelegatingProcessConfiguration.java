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

import java.math.BigInteger;
import java.net.URI;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.commons.description.input.BoundingBoxInputDescription;
import com.github.autermann.wps.commons.description.input.ComplexInputDescription;
import com.github.autermann.wps.commons.description.input.InputOccurence;
import com.github.autermann.wps.commons.description.input.LiteralInputDescription;
import com.github.autermann.wps.commons.description.input.ProcessInputDescription;
import com.github.autermann.wps.commons.description.output.BoundingBoxOutputDescription;
import com.github.autermann.wps.commons.description.output.ComplexOutputDescription;
import com.github.autermann.wps.commons.description.output.LiteralOutputDescription;
import com.github.autermann.wps.commons.description.output.ProcessOutputDescription;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.streaming.ProcessConfiguration;
import com.github.autermann.wps.streaming.StreamingExecutor;
import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingProcessConfiguration extends ProcessConfiguration {
    private URI remoteURL;
    private ProcessDescription processDescription;

    public URI getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(URI remoteURL) {
        this.remoteURL = checkNotNull(remoteURL);
    }

    public ProcessDescription getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(ProcessDescription processDescription) {
        this.processDescription = checkNotNull(processDescription);
    }

    @Override
    public StreamingExecutor createStreamingExecutor(MessageReceiver callback) {
        return new DelegatingExecutor(callback, this);
    }

    @Override
    public StreamingProcessDescription describe() throws ExceptionReport {
        StreamingProcessDescription pd = new StreamingProcessDescription(
                new OwsCodeType(getProcessID().toString()),
                getProcessDescription().getTitle(),
                getProcessDescription().getAbstract().orNull(),
                getProcessDescription().getVersion(), false, true);
        describeInputs(pd);
        describeOutputs(pd);
        return pd;
    }

    private void describeOutputs(StreamingProcessDescription pd) {
        for (ProcessOutputDescription output : getProcessDescription().getOutputDescriptions()) {
            if (output.isBoundingBox()) {
                BoundingBoxOutputDescription bboxOutput = output.asBoundingBox();
                pd.addOutput(new BoundingBoxOutputDescription(
                        bboxOutput.getID(),
                        bboxOutput.getTitle(),
                        bboxOutput.getAbstract().orNull(),
                        bboxOutput.getDefaultCRS().orNull(),
                        null));
            } else if (output.isComplex()) {
                ComplexOutputDescription complexOutput = output.asComplex();
                pd.addOutput(new ComplexOutputDescription(
                        complexOutput.getID(),
                        complexOutput.getTitle(),
                        complexOutput.getAbstract().orNull(),
                        complexOutput.getDefaultFormat(),
                        null));
            } else if (output.isLiteral()) {
                LiteralOutputDescription literalOutput = output.asLiteral();
                pd.addOutput(new LiteralOutputDescription(
                        literalOutput.getID(),
                        literalOutput.getTitle(),
                        literalOutput.getAbstract().orNull(),
                        literalOutput.getDataType(),
                        literalOutput.getDefaultUOM().orNull(),
                        null));
            }
        }
    }

    private void describeInputs(StreamingProcessDescription pd)
            throws ExceptionReport {
        for (ProcessInputDescription input : getProcessDescription().getInputDescriptions()) {
            Optional<ProcessInputDescription> updated = updateOccurence(input);
            if (updated.isPresent()) {
                pd.addInput(updated.get());
            }
        }
    }

    private Optional<ProcessInputDescription> updateOccurence(ProcessInputDescription input) throws ExceptionReport {
        Optional<InputOccurence> newOccurence = calculateOccurence(input);
        final ProcessInputDescription updated;
        if (!newOccurence.isPresent()) {
            updated = null;
        } else if (input.isBoundingBox()) {
            BoundingBoxInputDescription bboxInput = input.asBoundingBox();
            updated = new BoundingBoxInputDescription(
                    bboxInput.getID(),
                    bboxInput.getTitle(),
                    bboxInput.getAbstract().orNull(),
                    newOccurence.get(),
                    bboxInput.getDefaultCRS().orNull(),
                    bboxInput.getSupportedCRS());
        } else if (input.isComplex()) {
            ComplexInputDescription complexInput = input.asComplex();
            updated = new ComplexInputDescription(
                    complexInput.getID(),
                    complexInput.getTitle(),
                    complexInput.getAbstract().orNull(),
                    newOccurence.get(),
                    complexInput.getDefaultFormat(),
                    complexInput.getFormats(),
                    complexInput.getMaximumMegabytes().orNull());
        } else if (input.isLiteral()) {
            LiteralInputDescription literalInput = input.asLiteral();
            updated = new LiteralInputDescription(
                    literalInput.getID(),
                    literalInput.getTitle(),
                    literalInput.getAbstract().orNull(),
                    newOccurence.get(),
                    literalInput.getDataType(),
                    literalInput.getAllowedValues(),
                    literalInput.getDefaultUOM().orNull(),
                    literalInput.getUOMs());
        } else {
            updated = null;
        }
        return Optional.fromNullable(updated);
    }

    private Optional<InputOccurence> calculateOccurence(ProcessInputDescription input)
            throws ExceptionReport {
        final InputOccurence occurence = input.getOccurence();
        BigInteger supplied = BigInteger.valueOf(getStaticInputs().getInputs(input.getID()).size());
        if (occurence.getMax().compareTo(supplied) < 0) {
            throw new ExceptionReport(String.format(
                    "Static input %s is not in allowed occurence range (%s,%s)",
                    input.getID(), occurence.getMin(), occurence.getMax()),
                                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }

        if (supplied.equals(BigInteger.ZERO)) {
            BigInteger newMax = occurence.getMax().subtract(supplied);
            if (newMax.compareTo(BigInteger.ONE) < 0) {
                // no more occurences are allowed
                return Optional.absent();
            }
            BigInteger newMin
                    = occurence.getMin().subtract(supplied).max(BigInteger.ZERO);
            return Optional.of(new InputOccurence(newMin, newMax));
        }
        return Optional.of(occurence);
    }

}
