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

import java.math.BigInteger;

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
import com.github.autermann.wps.streaming.StreamingProcessDescription;
import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.data.input.ProcessInputs;
import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescriptionTransformer implements
        ProcessInputDescription.ReturningVisitor<ProcessInputDescription.Builder<?, ?>>,
        ProcessOutputDescription.ReturningVisitor<ProcessOutputDescription.Builder<?, ?>> {

    private ProcessInputs staticInputs;
    private StreamingProcessID id;
    private ProcessDescription description;

    public ProcessDescriptionTransformer setStaticInputs(
            ProcessInputs staticInputs) {
        this.staticInputs = staticInputs;
        return this;
    }

    public ProcessDescriptionTransformer setProcessID(StreamingProcessID id) {
        this.id = id;
        return this;
    }

    public ProcessDescriptionTransformer setDescription(
            ProcessDescription description) {
        this.description = description;
        return this;
    }

    public StreamingProcessDescription transform()
            throws ExceptionReport {
        StreamingProcessDescription.Builder<?, ?> b
                = StreamingProcessDescription.builder()
                .withIdentifier(id.toString())
                .withTitle(description.getTitle())
                .withAbstract(description.getAbstract().orNull())
                .withVersion(description.getVersion())
                .statusSupported(false)
                .storeSupported(description.isStoreSupported());
        for (OwsCodeType iid : description.getInputs()) {
            b.withInput(transform(description.getInput(iid)).orNull());
        }
        for (OwsCodeType oid : description.getOutputs()) {
            b.withOutput(transform(description.getOutput(oid)).orNull());
        }
        return b.build();
    }

    private Optional<InputOccurence> calculateOccurence(
            ProcessInputDescription input)
            throws ExceptionReport {
        final InputOccurence occurence = input.getOccurence();
        BigInteger supplied
                = BigInteger.valueOf(staticInputs.getInputs(input.getID())
                        .size());
        if (occurence.getMax().compareTo(supplied) < 0) {
            throw new ExceptionReport(String
                    .format("Static input %s is not in allowed occurence range (%s,%s)", input
                            .getID(), occurence.getMin(), occurence.getMax()), ExceptionReport.INVALID_PARAMETER_VALUE);
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

    public Optional<ProcessInputDescription> transform(
            ProcessInputDescription input)
            throws ExceptionReport {
        Optional<InputOccurence> newOccurence = calculateOccurence(input);
        if (newOccurence.isPresent()) {
            return Optional.of(input.visit(this).withIdentifier(input.getID())
                    .withTitle(input.getTitle())
                    .withAbstract(input.getAbstract().orNull())
                    .withOccurence(newOccurence.get()).build());
        } else {
            return Optional.absent();
        }
    }

    public Optional<ProcessOutputDescription> transform(
            ProcessOutputDescription output)
            throws ExceptionReport {
        return Optional.of(output.visit(this).withIdentifier(output.getID())
                .withTitle(output.getTitle())
                .withAbstract(output.getAbstract().orNull()).build());
    }

    @Override
    public ProcessInputDescription.Builder<?, ?> visit(
            BoundingBoxInputDescription input) {
        return BoundingBoxInputDescription.builder()
                .withDefaultCRS(input.getDefaultCRS().orNull())
                .withSupportedCRS(input.getSupportedCRS());
    }

    @Override
    public ProcessInputDescription.Builder<?, ?> visit(
            ComplexInputDescription input) {
        return ComplexInputDescription.builder()
                .withDefaultFormat(input.getDefaultFormat())
                .withSupportedFormat(input.getSupportedFormats())
                .withMaximumMegabytes(input.getMaximumMegabytes().orNull());
    }

    @Override
    public ProcessInputDescription.Builder<?, ?> visit(
            LiteralInputDescription input) {
        return LiteralInputDescription.builder()
                .withDataType(input.getDataType())
                .withAllowedValues(input.getAllowedValues())
                .withDefaultUOM(input.getDefaultUOM().orNull())
                .withSupportedUOM(input.getSupportedUOM());
    }

    @Override
    public ProcessOutputDescription.Builder<?, ?> visit(
            BoundingBoxOutputDescription output) {
        return BoundingBoxOutputDescription.builder()
                .withDefaultCRS(output.getDefaultCRS().orNull());
    }

    @Override
    public ProcessOutputDescription.Builder<?, ?> visit(
            ComplexOutputDescription output) {
        return ComplexOutputDescription.builder()
                .withDefaultFormat(output.getDefaultFormat());
    }

    @Override
    public ProcessOutputDescription.Builder<?, ?> visit(
            LiteralOutputDescription output) {
        return LiteralOutputDescription.builder()
                .withDataType(output.getDataType())
                .withDefaultUOM(output.getDefaultUOM().orNull());
    }
}
