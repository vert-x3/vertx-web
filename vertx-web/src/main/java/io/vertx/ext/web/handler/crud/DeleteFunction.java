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

/**
 * Represents a user defined delete function. Given the {@code entity unique identifier} the function returns a future
 * result with a total number of records affected by the query.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
@FunctionalInterface
public interface DeleteFunction {

  /**
   * Delete an entity given the unique identifier {@code entity}. Returns a future result with the number of affected
   * rows.
   *
   * @param entity the unique identifier to delete.
   * @return future result with affected rows, e.g.: {@code 1}, {@code 0}
   */
  Future<Integer> apply(String entity);
}
