/*
 * Copyright 2014 Red Hat, Inc.
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
package io.vertx.ext.web.impl;

import io.vertx.ext.web.RoutingContext;

/**
 * Internal methods that are not expected or prime to be in the public API
 *
 * @author Paulo Lopes
 */
public interface RoutingContextInternal extends RoutingContext {

  int BODY_HANDLER = 1 << 1;
  int CORS_HANDLER = 1 << 2;

  /**
   * flags the current routing context as having visited the handler with {@code id}.
   * @param id one of the constants of this interface
   * @return self
   */
  RoutingContextInternal visitHandler(int id);

  /**
   * returns true if the current context has been visited by the handler with {@code id}.
   *
   * @param id one of the constants of this interface
   * @return true if the {@link #visitHandler(int)} has been called with the same id.
   */
  boolean seenHandler(int id);

  /**
   * propagates a matching failure across routers.
   *
   * @param matchFailure the desired match failure
   * @return fluent self
   */
  RoutingContextInternal setMatchFailure(int matchFailure);
}
