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
        StreamingProcessDescription.Builder<?,?> b = StreamingProcessDescription.builder()
                .withIdentifier(getProcessID().toString())
                .withTitle(getProcessDescription().getTitle())
                .withAbstract(getProcessDescription().getAbstract().orNull())
                .withVersion(getProcessDescription().getVersion())
                .statusSupported(false)
                .storeSupported(getProcessDescription().isStoreSupported());
        describeInputs(b);
        describeOutputs(b);
        return b.build();
    }

    private void describeOutputs(StreamingProcessDescription.Builder<?,?> pd) {
        for (ProcessOutputDescription output : getProcessDescription().getOutputDescriptions()) {
            final ProcessOutputDescription.Builder<?,?> ob;
            if (output.isBoundingBox()) {
                ob = BoundingBoxOutputDescription.builder()
                        .withDefaultCRS(output.asBoundingBox().getDefaultCRS().orNull());
            } else if (output.isComplex()) {
                ob = ComplexOutputDescription.builder()
                        .withDefaultFormat(output.asComplex().getDefaultFormat());
            } else if (output.isLiteral()) {
                ob = LiteralOutputDescription.builder()
                        .withDataType(output.asLiteral().getDataType())
                        .withDefaultUOM(output.asLiteral().getDefaultUOM().orNull());
            } else {
                continue;
            }
            pd.withOutput(ob
                    .withIdentifier(output.getID())
                    .withTitle(output.getTitle())
                    .withAbstract(output.getAbstract().orNull()));
        }
    }

    private void describeInputs(StreamingProcessDescription.Builder<?,?> pd)
            throws ExceptionReport {
        for (ProcessInputDescription input : getProcessDescription().getInputDescriptions()) {
            Optional<ProcessInputDescription> updated = updateOccurence(input);
            if (updated.isPresent()) {
                pd.withInput(updated.get());
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
            updated = BoundingBoxInputDescription.builder()
                    .withIdentifier(bboxInput.getID())
                    .withTitle(bboxInput.getTitle())
                    .withAbstract(bboxInput.getAbstract().orNull())
                    .withOccurence(newOccurence.get())
                    .withDefaultCRS(bboxInput.getDefaultCRS().orNull())
                    .withSupportedCRS(bboxInput.getSupportedCRS())
                    .build();
        } else if (input.isComplex()) {
            ComplexInputDescription complexInput = input.asComplex();
            updated = ComplexInputDescription.builder()
                    .withIdentifier(complexInput.getID())
                    .withTitle(complexInput.getTitle())
                    .withAbstract(complexInput.getAbstract().orNull())
                    .withOccurence(newOccurence.get())
                    .withDefaultFormat(complexInput.getDefaultFormat())
                    .withSupportedFormat(complexInput.getSupportedFormats())
                    .withMaximumMegabytes(complexInput.getMaximumMegabytes().orNull())
                    .build();
        } else if (input.isLiteral()) {
            LiteralInputDescription literalInput = input.asLiteral();
            updated = LiteralInputDescription.builder()
                    .withIdentifier(literalInput.getID())
                    .withTitle(literalInput.getTitle())
                    .withAbstract(literalInput.getAbstract().orNull())
                    .withOccurence(newOccurence.get())
                    .withDataType(literalInput.getDataType())
                    .withAllowedValues(literalInput.getAllowedValues())
                    .withDefaultUOM(literalInput.getDefaultUOM().orNull())
                    .withSupportedUOM(literalInput.getSupportedUOM())
                    .build();
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
