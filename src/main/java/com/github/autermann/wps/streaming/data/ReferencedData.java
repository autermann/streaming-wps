package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.opengis.wps.x100.InputReferenceType;


/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class ReferencedData extends Data {

    private final InputReferenceType xml;

    public ReferencedData(InputReferenceType xml) {
        this.xml = checkNotNull(xml);
    }

    public InputReferenceType getXml() {
        return xml;
    }

}
