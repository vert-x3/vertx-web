/*
 * Copyright 2019 Red Hat, Inc.
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
package io.vertx.ext.web.handler.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.MultiTenantHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class MultiTenantHandlerImpl implements MultiTenantHandler {

  private final Map<String, Handler<RoutingContext>> handlerMap = new HashMap<>();

  private final Function<RoutingContext, String> tenantExtractor;
  private final String contextKey;

  private Handler<RoutingContext> defaultHandler;

  public MultiTenantHandlerImpl(Function<RoutingContext, String> tenantExtractor, String contextKey) {
    this.tenantExtractor = Objects.requireNonNull(tenantExtractor);
    this.contextKey = Objects.requireNonNull(contextKey);
  }

  @Override
  public MultiTenantHandler addTenantHandler(String tenant, Handler<RoutingContext> handler) {
    Objects.requireNonNull(tenant);
    Objects.requireNonNull(handler);

    if (handlerMap.put(tenant, handler) != null) {
      throw new IllegalStateException("tenant '" + tenant + "' already present");
    }
    return this;
  }

  @Override
  public MultiTenantHandler addDefaultHandler(Handler<RoutingContext> handler) {
    Objects.requireNonNull(handler);
    this.defaultHandler = handler;
    return this;
  }

  @Override
  public void handle(RoutingContext ctx) {
    final String tenant = tenantExtractor.apply(ctx);
    final Handler<RoutingContext> handler = handlerMap.getOrDefault(tenant, defaultHandler);

    if (handler != null) {
      // there's a handler for this tenant
      ctx.put(contextKey, tenant == null ? "default" : tenant);
      // continue as usual
      handler.handle(ctx);
    } else {
      // no handler found, this handle is not applicable
      // continue with the chain
      ctx.next();
    }
  }
}
