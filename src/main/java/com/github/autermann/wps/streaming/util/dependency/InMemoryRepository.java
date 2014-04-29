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
