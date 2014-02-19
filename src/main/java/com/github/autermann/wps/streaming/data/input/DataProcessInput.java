package com.github.autermann.wps.streaming.data.input;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.data.Data;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DataProcessInput extends ProcessInput {

    private final Data data;

    public DataProcessInput(OwsCodeType id, Data data) {
        super(id);
        this.data = Preconditions.checkNotNull(data);
    }

    public Data getData() {
        return this.data;
    }

}
