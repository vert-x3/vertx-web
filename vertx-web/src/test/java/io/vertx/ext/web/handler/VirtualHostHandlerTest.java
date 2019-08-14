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
public class VirtualHostHandlerTest extends WebTestBase {

  @Test
  public void testVHost() throws Exception {
    router.route().handler(VirtualHostHandler.create("*.com", ctx -> ctx.response().end()));

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(HttpMethod.GET, "/", req -> req.setHost("www.mysite.com"), 200, "OK", null);
  }

  @Test
  public void testVHostShouldFail() throws Exception {
    router.route().handler(VirtualHostHandler.create("*.com", ctx -> ctx.response().end()));

    router.route().handler(ctx -> ctx.fail(500));

    testRequest(HttpMethod.GET, "/", req -> req.setHost("www.mysite.net"), 500, "Internal Server Error", null);
  }
}
