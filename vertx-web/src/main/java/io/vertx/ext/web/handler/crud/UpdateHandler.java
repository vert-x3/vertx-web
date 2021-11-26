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

/**
 * Represents a user defined update function. Given the {@code entity unique identifier} and request body, the function
 * returns a future result with nuber of affected rows.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
@FunctionalInterface
public interface UpdateHandler<T> {

  /**
   * Update a single entity from the user defined storage.
   * @param entity the unique identifier to update.
   * @param newObject the request body.
   * @return future result with number of affected rows.
   */
  Future<Integer> handle(String entity, T newObject);
}
