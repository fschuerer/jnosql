/*
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
 */
package org.jnosql.artemis.graph;

import jakarta.nosql.NonUniqueResultException;
import jakarta.nosql.mapping.PreparedStatement;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class DefaultPreparedStatement implements PreparedStatement {

    private final GremlinExecutor executor;

    private final String gremlin;

    private final Map<String, Object> params = new HashMap<>();

    private final GraphTraversalSource traversalSource;

    DefaultPreparedStatement(GremlinExecutor executor, String gremlin, GraphTraversalSource traversalSource) {
        this.executor = executor;
        this.gremlin = gremlin;
        this.traversalSource = traversalSource;
    }


    @Override
    public PreparedStatement bind(String name, Object value) {
        requireNonNull(name, "name is required");
        requireNonNull(value, "value is required");
        params.put(name, value);
        return this;
    }

    @Override
    public <T> Stream<T> getResult() {
        return executor.executeGremlin(traversalSource, gremlin, params);
    }

    @Override
    public <T> Optional<T> getSingleResult() {
        Stream<T> entities = getResult();
        final Iterator<T> iterator = entities.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final T entity = iterator.next();
        if (!iterator.hasNext()) {
            return Optional.of(entity);
        }
        throw new NonUniqueResultException("There is more than one result found in the gremlin query: " + gremlin);
    }
}
