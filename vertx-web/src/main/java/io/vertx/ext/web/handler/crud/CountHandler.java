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
 * Represents a user defined count function. Given the query the function returns a future result with a total number of
 * records affected by the query.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
@FunctionalInterface
public interface CountHandler {

  /**
   * Calculate the total number of affected rows by the Query.
   *
   * @param query query parameters, implementations should only refer to {@link CrudQuery#getQuery()}
   * @return Future result with affected rows
   */
  Future<Integer> handle(CrudQuery query);
}
