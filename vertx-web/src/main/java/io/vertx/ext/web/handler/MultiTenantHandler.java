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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.MultiTenantHandlerImpl;

import java.util.function.Function;

/**
 * A handler which selectively executes another handler if a precondition is met.
 *
 * There are cases where applications are build as multi tenant, in this cases one of the
 * common tasks is to configure different authentication mechanisms for each tenant.
 *
 * This handler will allow registering any other handler and will only execute it if
 * the precondition is met. There are 2 way of defining a precondition:
 *
 * <ul>
 *     <li>A http header value for example <pre>X-Tenant</pre></li>
 *     <li>A custom extractor function that can return a String from the context</li>
 * </ul>
 *
 * Requests that pass the validation will contain a new key in the routing context with
 * the tenant id, for the case of being a default handler the value if this key will be "default".
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
public interface MultiTenantHandler extends Handler<RoutingContext> {

  /**
   * The default key used to identify a tenant in the context data.
   */
  String TENANT = "tenant";

  /**
   * Create a MultiTenant handler that will extract the tenant id from a given header name.
   *
   * @param header the header to lookup (e.g.: "X-Tenant")
   * @return the new handler.
   */
  static MultiTenantHandler create(String header) {
    return create((ctx) -> ctx.request().getHeader(header));
  }

  /**
   * Create a MultiTenant handler using a custom tenant extraction function.
   *
   * @param tenantExtractor the function that extracts the tenant id from the request
   * @return the new handler.
   */
  static MultiTenantHandler create(Function<RoutingContext, String> tenantExtractor) {
    return create(tenantExtractor, TENANT);
  }

  /**
   * Create a MultiTenant handler using a custom tenant extraction function.
   *
   * @param tenantExtractor the function that extracts the tenant id from the request
   * @param contextKey the custom key to store the tenant id in the context
   * @return the new handler.
   */
  static MultiTenantHandler create(Function<RoutingContext, String> tenantExtractor, String contextKey) {
    return new MultiTenantHandlerImpl(tenantExtractor, contextKey);
  }

  /**
   * Add a handler for a given tenant to this handler.
   *
   * Both tenant and handler cannot be null.
   *
   * @param tenant the tenant id
   * @param handler the handler to register.
   * @throws IllegalStateException In case a handler is already present for that tenant.
   * @return a fluent reference to self.
   */
  @Fluent
  MultiTenantHandler addTenantHandler(String tenant, Handler<RoutingContext> handler);

  /**
   * Add a default handler for the case when no tenant was matched.
   *
   * The handler cannot be null.
   *
   * @param handler the handler to register.
   * @return a fluent reference to self.
   */
  @Fluent
  MultiTenantHandler addDefaultHandler(Handler<RoutingContext> handler);
}
