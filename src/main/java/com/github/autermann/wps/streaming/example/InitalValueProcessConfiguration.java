package com.github.autermann.wps.streaming.example;

import com.github.autermann.wps.streaming.ProcessConfiguration;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class InitalValueProcessConfiguration<T> extends ProcessConfiguration {

    private T initialValue;

    public void setInitialValue(T initialValue) {
        this.initialValue = initialValue;
    }

    public T getInitialValue() {
        return this.initialValue;
    }

}
