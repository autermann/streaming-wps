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
package com.github.autermann.wps.streaming;

import com.github.autermann.wps.commons.description.ProcessDescription;
import com.github.autermann.wps.commons.description.ows.OwsCodeType;
import com.github.autermann.wps.commons.description.ows.OwsLanguageString;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingProcessDescription extends ProcessDescription {
    private final boolean finalResult;
    private final boolean intermediateResults;

    public StreamingProcessDescription(OwsCodeType identifier,
                                       OwsLanguageString title,
                                       OwsLanguageString abstrakt,
                                       String version,
                                       boolean finalResult,
                                       boolean intermediateResults) {
        super(identifier, title, abstrakt, version);
        this.finalResult = finalResult;
        this.intermediateResults = intermediateResults;
    }

    public boolean isFinalResult() {
        return finalResult;
    }

    public boolean isIntermediateResults() {
        return intermediateResults;
    }

}
