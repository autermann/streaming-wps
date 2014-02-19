package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.base.Optional;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class LiteralData extends Data {

    private final String type;
    private final String value;
    private final Optional<String> uom;

    public LiteralData(String type, String value, String uom) {
        this.type = checkNotNull(type);
        this.value = checkNotNull(emptyToNull(value));
        this.uom = Optional.fromNullable(emptyToNull(uom));
    }

    public LiteralData(String type, String value) {
        this(type, value, null);
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public Optional<String> getUom() {
        return this.uom;
    }

}
