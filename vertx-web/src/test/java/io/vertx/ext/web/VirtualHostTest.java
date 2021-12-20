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
package io.vertx.ext.web;

import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.SocketAddress;
import org.junit.Test;

/**
 * @author Paulo Lopes
 */
public class VirtualHostTest extends WebTestBase {

  @Test
  public void testVHost() throws Exception {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("www.mysite.com")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostPort() throws Exception {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("www.mysite.com:8080")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostIPv6Any() throws Exception {
    router.route().virtualHost("::").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("[::]")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostIPv6AnyPort() throws Exception {
    router.route().virtualHost("::").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("[::]:8080")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostIPv6Home() throws Exception {
    router.route().virtualHost("::1").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("[::1]")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostIPv6HomePort() throws Exception {
    router.route().virtualHost("::1").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("[::1]:8080")
      .setPort(80), req -> {}, 200, "OK", null);
  }

  @Test
  public void testVHostShouldFail() throws Exception {
    router.route().virtualHost("*.com").handler(ctx -> ctx.response().end());

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(new RequestOptions().setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("www.mysite.net")
      .setPort(80), req -> {}, 500, "Internal Server Error", null);
  }

  @Test
  public void testVHostSubRouter() throws Exception {

    Router a = Router.router(vertx);
    a.get("/somepath").handler(RoutingContext::end);

    router.route("/*").virtualHost("*.com").subRouter(a);
    testRequest(new RequestOptions()
      .setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("www.mysite.com")
      .setPort(80)
      .setURI("/somepath"), req -> {}, 200, "OK", null);

    // Or

    router.route().virtualHost("*.com").subRouter(a);
    testRequest(new RequestOptions()
      .setServer(SocketAddress.inetSocketAddress(8080, "localhost"))
      .setHost("www.mysite.com")
      .setPort(80)
      .setURI("/somepath"), req -> {}, 200, "OK", null);
  }

}
