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
 */
public interface RoutingContextInternal extends RoutingContext {

  /**
   * propagates a matching failure across routers.
   *
   * @param matchFailure the desired match failure
   * @return fluent self
   */
  RoutingContextInternal setMatchFailure(int matchFailure);
}
