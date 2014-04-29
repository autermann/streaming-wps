/*
 * Copyright (C) 2014 Christian Autermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.autermann.wps.streaming;

import com.github.autermann.wps.commons.description.ProcessDescription;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingProcessDescription extends ProcessDescription {
    private final boolean finalResult;
    private final boolean intermediateResults;

    public StreamingProcessDescription(Builder<?, ?> builder) {
        super(builder);
        this.finalResult = builder.finalResult;
        this.intermediateResults = builder.intermediateResults;
    }

    public boolean isFinalResult() {
        return this.finalResult;
    }

    public boolean isIntermediateResults() {
        return this.intermediateResults;
    }

    public static Builder<?, ?> builder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl extends Builder<StreamingProcessDescription, BuilderImpl> {
        @Override
        public StreamingProcessDescription build() {
            return new StreamingProcessDescription(this);
        }
    }

    public static abstract class Builder<T extends StreamingProcessDescription, B extends Builder<T, B>>
            extends ProcessDescription.Builder<T, B> {
        private boolean finalResult;
        private boolean intermediateResults;

        @SuppressWarnings("unchecked")
        public B hasFinalResult(boolean finalResult) {
            this.finalResult = finalResult;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B hasIntermediateResults(boolean intermediateResults) {
            this.intermediateResults = intermediateResults;
            return (B) this;
        }
    }
}
