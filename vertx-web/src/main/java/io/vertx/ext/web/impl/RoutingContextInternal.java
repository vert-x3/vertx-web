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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * Internal methods that are not expected or prime to be in the public API
 *
 * @author Paulo Lopes
 */
public interface RoutingContextInternal extends RoutingContext {

  int SESSION_HANDLER = 1 << 1;
  int BODY_HANDLER = 1 << 2;
  int CORS_HANDLER = 1 << 3;

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

  /**
   * @return the current router this context is being routed through. All routingContext is associated with a router and
   * never returns null.
   */
  @CacheReturn
  Router currentRouter();

  /**
   * If a sub router is mounted to a parent router and the request is routed to the sub Router, two routingContexts are generated.
   * sub routingContext associated sub router and {@code parent()} of sub routingContext associated parent router.
   *
   * @return the parent context for this context. It will be null for a top level router. For a sub-router of context has parent
   */
  @CacheReturn
  @Nullable RoutingContextInternal parent();

  /**
   * Set the body. Used by the {@link io.vertx.ext.web.handler.BodyHandler}.
   *
   * @param body  the body
   */
  void setBody(Buffer body);

  /**
   * Set the session. Used by the {@link io.vertx.ext.web.handler.SessionHandler}.
   *
   * @param session  the session
   */
  void setSession(Session session);

  int restIndex();

  boolean normalizedMatch();

  default String basePath() {
    // if we're on a sub router already we need to skip the matched path
    String mountPoint = mountPoint();

    int skip = mountPoint != null ? mountPoint.length() : 0;
    if (normalizedMatch()) {
      return normalizedPath().substring(skip, skip + restIndex());
    } else {
      String path = request().path();
      if (path != null) {
        return path.substring(skip, skip + restIndex());
      }
      return null;
    }

  };
}
