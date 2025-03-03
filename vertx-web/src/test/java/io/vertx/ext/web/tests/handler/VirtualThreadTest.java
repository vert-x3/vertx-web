/*
 * Copyright 2025 Red Hat, Inc.
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
package io.vertx.ext.web.tests.handler;

import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.internal.VertxInternal;
import io.vertx.ext.web.Router;
import io.vertx.test.core.VertxTestBase;
import org.junit.Assume;
import org.junit.Test;

public class VirtualThreadTest extends VertxTestBase {

  @Test
  public void testBlockingHandler() {
    Assume.assumeTrue(isVirtualThreadAvailable());
    HttpServer server = vertx.createHttpServer();
    HttpClient client = vertx.createHttpClient();
    Router router = Router.router(vertx);
    server.requestHandler(router);
    router.get("/").blockingHandler(request -> {
      vertx.timer(200).await();
      request.response().end("Hello");
    });
    Context context = ((VertxInternal) vertx).createVirtualThreadContext();
    context.runOnContext(v -> {
      server.listen(8080, "localhost").await();
      long now = System.currentTimeMillis();
      Buffer body = client
        .request(HttpMethod.GET, 8080, "localhost", "/")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .await();
      assertEquals("Hello", body.toString());
      assertTrue(System.currentTimeMillis() - now >= 200);
      testComplete();
    });
    await();
  }
}
