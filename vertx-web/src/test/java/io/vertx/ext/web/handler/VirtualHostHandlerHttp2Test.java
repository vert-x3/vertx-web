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

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class VirtualHostHandlerHttp2Test extends WebTestBase {


  @Override
  public void setUp() throws Exception {
    super.setUp();
    serverPort = 8181;
    router = Router.router(vertx);
    server = vertx.createHttpServer(new HttpServerOptions()
      .setPort(serverPort)
      .setHost("localhost")
    );
    client = vertx.createHttpClient(new HttpClientOptions()
      .setDefaultPort(serverPort)
      .setProtocolVersion(HttpVersion.HTTP_2)
    );
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(onSuccess(res -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

  @Test
  public void testVHost() throws Exception {
    router.route().handler(VirtualHostHandler.create("*.com", ctx -> {
      ctx.response().end();
    }));

    router.route().handler(ctx -> {
      ctx.fail(500);
    });

    testRequest(HttpMethod.GET, "/", req -> req.setHost("www.mysite.com"), 200, "OK", null);
  }

  @Test
  public void testVHostShouldFail() throws Exception {
    router.route().handler(VirtualHostHandler.create("*.com", ctx -> {
      ctx.response().end();
    }));

    router.route().handler(ctx -> {
      ctx.fail(500);
    });

    testRequest(HttpMethod.GET, "/", req -> req.setHost("www.mysite.net"), 500, "Internal Server Error", null);
  }
}
