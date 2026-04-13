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
package io.vertx.ext.web.tests;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

/**
 * @author Paulo Lopes
 */
public class VirtualHostTest extends WebTestBase2 {

  @Test
  public void testVHost() {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "www.mysite.com").send(), 200, "OK");
  }

  @Test
  public void testVHostPort() {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "www.mysite.com:8080").send(), 200, "OK");
  }

  @Test
  public void testVHostIPv6Any() {
    router.route().virtualHost("::").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "[::]").send(), 200, "OK");
  }

  @Test
  public void testVHostIPv6AnyPort() {
    router.route().virtualHost("::").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "[::]:8080").send(), 200, "OK");
  }

  @Test
  public void testVHostIPv6Home() {
    router.route().virtualHost("::1").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "[::1]").send(), 200, "OK");
  }

  @Test
  public void testVHostIPv6HomePort() {
    router.route().virtualHost("::1").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "[::1]:8080").send(), 200, "OK");
  }

  @Test
  public void testVHostShouldFail() {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(webClient.get(8080, "localhost", "/").putHeader("Host", "www.mysite.net").send(), 500, "Internal Server Error");
  }

  @Test
  public void testVHostSubRouter() {

    Router a = Router.router(vertx);
    a.get("/somepath").handler(RoutingContext::end);

    router.route("/*").virtualHost("*.com").subRouter(a);
    testRequest(webClient.get(8080, "localhost", "/somepath").putHeader("Host", "www.mysite.com").send(), 200, "OK");

    // Or

    router.route().virtualHost("*.com").subRouter(a);
    testRequest(webClient.get(8080, "localhost", "/somepath").putHeader("Host", "www.mysite.com").send(), 200, "OK");
  }

}
