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

package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class MultiTenantHandlerTest extends WebTestBase {

  @Test
  public void testHeader() throws Exception {
    router.clear();

    // assume tenants are identified by the header "X-Tenant"
    router.route().handler(
      MultiTenantHandler.create("X-Tenant")
        .addTenantHandler("tenant-1", ctx -> ctx.response().end("Hello from tenant-1"))
        .addTenantHandler("tenant-2", ctx -> ctx.response().end("Hello from tenant-2"))
        .addTenantHandler("tenant-3", ctx -> ctx.response().end("Hello from tenant-3"))
    );

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-1"),
      null,
      200,
      "OK", "Hello from tenant-1");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-2"),
      null,
      200,
      "OK", "Hello from tenant-2");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-3"),
      null,
      200,
      "OK", "Hello from tenant-3");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-4"),
      null,
      404,
      "Not Found", "<html><body><h1>Resource not found</h1></body></html>");

    testRequest(
      HttpMethod.GET,
      "/",
      null,
      null,
      404,
      "Not Found", "<html><body><h1>Resource not found</h1></body></html>");
  }

  @Test
  public void testDefaultHeader() throws Exception {
    router.clear();

    // assume tenants are identified by the header "X-Tenant"
    router.route().handler(
      MultiTenantHandler.create("X-Tenant")
        .addTenantHandler("tenant-1", ctx -> ctx.response().end("Hello from tenant-1"))
        .addTenantHandler("tenant-2", ctx -> ctx.response().end("Hello from tenant-2"))
        .addTenantHandler("tenant-3", ctx -> ctx.response().end("Hello from tenant-3"))
        .addDefaultHandler(ctx -> ctx.response().end("No valid tenant supplied"))
    );

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-1"),
      null,
      200,
      "OK", "Hello from tenant-1");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-2"),
      null,
      200,
      "OK", "Hello from tenant-2");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-3"),
      null,
      200,
      "OK", "Hello from tenant-3");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-4"),
      null,
      200,
      "OK", "No valid tenant supplied");

    testRequest(
      HttpMethod.GET,
      "/",
      null,
      null,
      200,
      "OK", "No valid tenant supplied");
  }

  @Test
  public void testCustomExtractor() throws Exception {
    router.clear();

    // assume tenants are identified by the query param
    router.route().handler(
      MultiTenantHandler.create(ctx -> {
        List<String> params = ctx.queryParam("tenant");
        return params.size() > 0 ? params.get(0) : null;
      })
        .addTenantHandler("t1", ctx -> {
          assertEquals("t1", ctx.get(MultiTenantHandler.TENANT));
          ctx.response().end("Hello from tenant-1");
        })
        .addTenantHandler("t2", ctx -> {
          assertEquals("t2", ctx.get(MultiTenantHandler.TENANT));
          ctx.response().end("Hello from tenant-2");
        })
        .addTenantHandler("t3", ctx -> {
          assertEquals("t3", ctx.get(MultiTenantHandler.TENANT));
          ctx.response().end("Hello from tenant-3");
        })
        .addDefaultHandler(ctx -> {
          assertEquals("default", ctx.get(MultiTenantHandler.TENANT));
          ctx.response().end("No valid tenant supplied");
        })
    );

    testRequest(
      HttpMethod.GET,
      "/?tenant=t1",
      200,
      "OK", "Hello from tenant-1");

    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.putHeader("X-Tenant", "tenant-4"),
      null,
      200,
      "OK", "No valid tenant supplied");

    testRequest(
      HttpMethod.GET,
      "/",
      null,
      null,
      200,
      "OK", "No valid tenant supplied");
  }
}
