package com.github.autermann.wps.streaming.util.dependency;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class InMemoryRepository<K, I, O> implements Repository<K, I, O> {

    private final Map<K, I> inputs;
    private final Map<K, O> outputs;

    public InMemoryRepository() {
        this.inputs = Maps.newConcurrentMap();
        this.outputs = Maps.newConcurrentMap();
    }

    @Override
    public I loadInput(K key) {
        return this.inputs.get(key);
    }

    @Override
    public O loadOutput(K key) {
        return this.outputs.get(key);
    }

    @Override
    public void saveInput(K key, I input) {
        this.inputs.put(key, input);
    }

    @Override
    public void saveOutput(K key, O output) {
        this.outputs.put(key, output);
    }

}
