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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.github.autermann.wps.commons.Format;
import com.github.autermann.wps.streaming.data.OwsCodeType;
import com.google.common.collect.Maps;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ProcessDescription {

    private final OwsCodeType identifier;
    private final Map<OwsCodeType, ProcessInputDescription> inputs;
    private final Map<OwsCodeType, ProcessOutputDescription> outputs;

    public ProcessDescription(OwsCodeType identifier) {
        this.identifier = identifier;
        this.inputs = Maps.newHashMap();
        this.outputs = Maps.newHashMap();
    }

    public OwsCodeType getIdentifier() {
        return identifier;
    }

    public void addProcessInputDescription(ProcessInputDescription input) {
        this.inputs.put(input.getIdentifier(), input);
    }

    public void addProcessOutputDescription(ProcessOutputDescription output) {
        this.outputs.put(output.getIdentifier(), output);
    }

    public ProcessInputDescription getInput(OwsCodeType identifier) {
        return inputs.get(identifier);
    }

    public ProcessOutputDescription getOutput(OwsCodeType identifier) {
        return outputs.get(identifier);
    }

    public Set<OwsCodeType> getInputs() {
        return Collections.unmodifiableSet(inputs.keySet());
    }

    public Set<OwsCodeType> getOutputs() {
        return Collections.unmodifiableSet(outputs.keySet());
    }

    public static class ProcessInputDescription {
        private final OwsCodeType identifier;
        private final BigInteger minOccurs;
        private final BigInteger maxOccurs;

        public ProcessInputDescription(OwsCodeType identifier,
                                       BigInteger minOccurs,
                                       BigInteger maxOccurs) {
            this.identifier = checkNotNull(identifier);
            this.minOccurs = minOccurs;
            this.maxOccurs = maxOccurs;
        }

        public OwsCodeType getIdentifier() {
            return identifier;
        }

        public BigInteger getMinOccurs() {
            return minOccurs;
        }

        public BigInteger getMaxOccurs() {
            return maxOccurs;
        }
    }

    public static class ProcessOutputDescription {
        private final OwsCodeType identifier;

        public ProcessOutputDescription(OwsCodeType identifier) {
            this.identifier = checkNotNull(identifier);
        }

        public OwsCodeType getIdentifier() {
            return identifier;
        }
    }

    public static class BoundingBoxInputDescription extends ProcessInputDescription {
        private final Set<String> supportedCrs;
        private final String defaultCrs;

        public BoundingBoxInputDescription(OwsCodeType identifier,
                                           BigInteger minOccurs,
                                           BigInteger maxOccurs,
                                           String defaultCrs,
                                           Set<String> supportedCrs) {
            super(identifier, minOccurs, maxOccurs);
            this.supportedCrs = checkNotNull(supportedCrs);
            this.defaultCrs = checkNotNull(defaultCrs);
        }

        public Set<String> getSupportedCrs() {
            return Collections.unmodifiableSet(supportedCrs);
        }

        public String getDefaultCrs() {
            return defaultCrs;
        }
    }

    public static class LiteralInputDescription extends ProcessInputDescription {
        private final String dataType;
        private final Set<String> allowedValues;
        private final Set<String> uoms;
        private final boolean any;

        public LiteralInputDescription(OwsCodeType identifier,
                                       BigInteger minOccurs,
                                       BigInteger maxOccurs,
                                       String dataType,
                                       Set<String> uoms) {
            this(identifier, minOccurs, maxOccurs, dataType, Collections
                    .<String>emptySet(), uoms, true);
        }

        public LiteralInputDescription(OwsCodeType identifier,
                                       BigInteger minOccurs,
                                       BigInteger maxOccurs,
                                       String dataType,
                                       Set<String> allowedValues,
                                       Set<String> uoms) {
            this(identifier, minOccurs, maxOccurs, dataType, allowedValues, uoms, false);
        }

        public LiteralInputDescription(OwsCodeType identifier,
                                       BigInteger minOccurs,
                                       BigInteger maxOccurs,
                                       String dataType,
                                       Set<String> allowedValues,
                                       Set<String> uoms,
                                       boolean any) {
            super(identifier, minOccurs, maxOccurs);
            this.dataType = checkNotNull(dataType);
            this.allowedValues = checkNotNull(allowedValues);
            this.uoms = checkNotNull(uoms);
            this.any = any;
        }

        public String getDataType() {
            return dataType;
        }

        public Set<String> getAllowedValues() {
            return Collections.unmodifiableSet(allowedValues);
        }

        public Set<String> getUoms() {
            return Collections.unmodifiableSet(uoms);
        }

        public boolean isAny() {
            return any;
        }

    }

    public static class ComplexInputDescription extends ProcessInputDescription {
        private final Set<Format> formats;
        private final Format defaultFormat;

        public ComplexInputDescription(OwsCodeType identifier,
                                       BigInteger minOccurs,
                                       BigInteger maxOccurs,
                                       Format defaultFormat,
                                       Set<Format> formats) {
            super(identifier, minOccurs, maxOccurs);
            this.formats = checkNotNull(formats);
            this.defaultFormat = checkNotNull(defaultFormat);
        }

        public Set<Format> getFormats() {
            return Collections.unmodifiableSet(formats);
        }

        public Format getDefaultFormat() {
            return defaultFormat;
        }

    }

    public static class BoundingBoxOutputDescription extends ProcessOutputDescription {
        private final Set<String> supportedCrs;
        private final String defaultCrs;

        public BoundingBoxOutputDescription(OwsCodeType identifier,
                                            String defaultCrs,
                                            Set<String> supportedCrs) {
            super(identifier);
            this.supportedCrs = checkNotNull(supportedCrs);
            this.defaultCrs = checkNotNull(defaultCrs);
        }

        public Set<String> getSupportedCrs() {
            return Collections.unmodifiableSet(supportedCrs);
        }

        public String getDefaultCrs() {
            return defaultCrs;
        }

    }

    public static class LiteralOutputDescription extends ProcessOutputDescription {
        private final String dataType;
        private final Set<String> uoms;

        public LiteralOutputDescription(OwsCodeType identifier,
                                        String dataType,
                                        Set<String> uoms) {
            super(identifier);
            this.dataType = checkNotNull(dataType);
            this.uoms = checkNotNull(uoms);
        }

        public String getDataType() {
            return dataType;
        }

        public Set<String> getUoms() {
            return Collections.unmodifiableSet(uoms);
        }

    }

    public static class ComplexOutputDescription extends ProcessOutputDescription {
        private final Set<Format> formats;
        private final Format defaultFormat;

        public ComplexOutputDescription(OwsCodeType identifier,
                                        Format defaultFormat,
                                        Set<Format> formats) {
            super(identifier);
            this.formats = checkNotNull(formats);
            this.defaultFormat = checkNotNull(defaultFormat);
        }

        public Set<Format> getFormats() {
            return formats;
        }

        public Format getDefaultFormat() {
            return defaultFormat;
        }

    }
}
