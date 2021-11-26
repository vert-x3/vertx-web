/*
 * Copyright 2021 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler.crud;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Represents a user defined query function. Given the {@code query}, the function returns a future result with a list
 * of objects. The query format is user specific, sorting should be done on the positive and negative properties and
 * pagination should respect the start and end attributes of {@link CrudQuery}.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
@FunctionalInterface
public interface QueryHandler<T> {

  /**
   * Query for {@link CrudQuery} objects and return a future result with a list of json objects.
   *
   * @param query the query to be used while locating objects from a user provided storage.
   * @return future result with the list of matched objects.
   */
  Future<List<T>> handle(CrudQuery query);
}
