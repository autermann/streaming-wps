package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.autermann.wps.commons.Format;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class ComplexData extends Data {

    private final Format format;
    private final String content;

    public ComplexData(Format format, String content) {
        this.format = checkNotNull(format);
        this.content = checkNotNull(content);
    }

    public String getContent() {
        return this.content;
    }

    public Format getFormat() {
        return this.format;
    }

}
