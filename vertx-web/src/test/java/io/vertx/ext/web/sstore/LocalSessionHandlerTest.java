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

package io.vertx.ext.web.sstore;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.SessionHandlerTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LocalSessionHandlerTest extends SessionHandlerTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = LocalSessionStore.create(vertx);
  }

  @Test
  public void testRetryTimeout() throws Exception {
    assertTrue(doTestSessionRetryTimeout() < 3000);
  }

  @Test
  public void test2123() throws Exception {
    SessionHandler sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
      .setSessionTimeout(10_000)
      .setLazySession(true);

    router.clear();

    router.route().handler(sessionHandler);
    router.route().handler(ctx -> {
      ctx.session();
      ctx.response().setStatusCode(500);
      sessionHandler.flush(ctx, asyncResult -> {
        // store was skipped, so we signed with a success
        assertTrue(asyncResult.succeeded());
        ctx.end();
      });
    });

    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
  }
}
