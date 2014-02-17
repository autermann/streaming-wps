package com.github.autermann.wps.streaming.util.dependency;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface Repository<K, I, O> {

    I loadInput(K key);

    O loadOutput(K key);

    void saveInput(K key, I input);

    void saveOutput(K key, O output);

}
