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

/**
 * @author Paulo Lopes
 */
public class RerouteTest extends WebTestBase {

  @Test
  public void testReroute() throws Exception {
    router.get("/users/:name").handler(ctx -> {
      ctx.response().end("/users/:name");
    });
    router.get("/me").handler(ctx -> {
      ctx.reroute("/users/paulo");
    });

    testRequest(HttpMethod.GET, "/me", 200, "OK", "/users/:name");
  }

  @Test
  public void testRerouteReparse() throws Exception {
    router.get("/users/:name").handler(ctx -> {
      ctx.response().end(ctx.request().getParam("name"));
    });
    router.get("/me").handler(ctx -> {
      ctx.reroute("/users/paulo");
    });

    testRequest(HttpMethod.GET, "/me", 200, "OK", "paulo");
  }

  @Test
  public void testRerouteMethod() throws Exception {
    router.post("/me").handler(ctx -> {
      ctx.response().end("POST");
    });
    router.get("/me").handler(ctx -> {
      ctx.reroute(HttpMethod.POST, "/me");
    });

    testRequest(HttpMethod.GET, "/me", 200, "OK", "POST");
  }
}
