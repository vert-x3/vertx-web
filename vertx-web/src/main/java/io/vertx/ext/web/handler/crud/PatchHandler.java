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
 * Represents a user defined patch function. Given the {@code entity unique identifier}, the new incomplete object from
 * the request and the current object from the database, the function returns a future result with a total number of
 * records affected by the query.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
@FunctionalInterface
public interface PatchHandler<T> {

  /**
   * Patches an existing object and returns the number of affected rows.
   *
   * @param id the expected unique identifier
   * @param newObject the new (partial) object as read from the context body.
   * @param oldObject the existing object as returned from the {@link ReadHandler}.
   * @return future result with the number of affected rows.
   */
  Future<Integer> handle(String id, T newObject, T oldObject);
}
