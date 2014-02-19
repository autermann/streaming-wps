package com.github.autermann.wps.streaming.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.opengis.ows.x11.BoundingBoxType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class BoundingBoxData extends Data {

    private final BoundingBoxType xml;

    public BoundingBoxData(BoundingBoxType xml) {
        this.xml = checkNotNull(xml);
    }

    public BoundingBoxType getXml() {
        return this.xml;
    }

}
