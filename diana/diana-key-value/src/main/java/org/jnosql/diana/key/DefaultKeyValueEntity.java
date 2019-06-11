/*
 *
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.jnosql.diana.key;

import jakarta.nosql.TypeSupplier;
import jakarta.nosql.Value;
import jakarta.nosql.key.KeyValueEntity;

import java.util.Objects;

/**
 * The default implementation of {@link KeyValueEntity}
 */
final class DefaultKeyValueEntity implements KeyValueEntity {

    private final Object key;

    private final Object value;

    DefaultKeyValueEntity(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public <K> K getKey(TypeSupplier<K> typeSupplier) {
        Objects.requireNonNull(typeSupplier, "typeSupplier is required");
        return Value.of(key).get(typeSupplier);
    }

    @Override
    public <K> K getKey(Class<K> clazz) {
        Objects.requireNonNull(clazz, "clazz is required");
        return Value.of(key).get(clazz);
    }

    @Override
    public <V> V getValue(TypeSupplier<V> typeSupplier) {
        Objects.requireNonNull(typeSupplier, "typeSupplier is required");
        return Value.of(value).get(typeSupplier);
    }

    @Override
    public <V> V getValue(Class<V> clazz) {
        Objects.requireNonNull(clazz, "clazz is required");
        return Value.of(value).get(clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "DefaultKeyValueEntity{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
