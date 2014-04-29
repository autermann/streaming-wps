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


import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
abstract class RepositoryFuture<K, I, O, T> extends LoadableFuture<T> {

    protected final Repository<K, I, O> repository;
    protected final K key;

    RepositoryFuture(Repository<K, I, O> repository, K key) {
        this.repository = repository;
        this.key = key;
    }

    public boolean setAvailable(T value) {
        save(Preconditions.checkNotNull(value));
        return super.setAvailable();
    }

    protected abstract void save(T value);

}
