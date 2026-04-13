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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TimeoutHandlerTest extends WebTestBase2 {

  @Test
  public void testTimeout() {
    long timeout = 500;
    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> {
      // Don't end it
    });
    testRequest(HttpMethod.GET, "/", 503, "Service Unavailable");
  }

  @Test
  public void testTimeoutWithCustomEndHandler() {
    long timeout = 500;

    AtomicBoolean ended = new AtomicBoolean();
    router.route().handler(routingContext -> {
      routingContext.addEndHandler(event -> ended.set(true));
      routingContext.next();
    });

    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> {
      // Don't end it
    });
    testRequest(HttpMethod.GET, "/", 503, "Service Unavailable");

    assertWaitUntil(ended::get);
  }


  @Test
  public void testTimeoutCancelled() throws Exception {
    long timeout = 500;
    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testTimeoutWithReroute() throws Exception {
    router.route().handler(TimeoutHandler.create(500));
    router.get("/a").handler(rc -> rc.reroute("/b"));
    router.get("/b").handler(RoutingContext::end);
    router.errorHandler(TimeoutHandler.DEFAULT_ERRORCODE, rc -> fail());
    testRequest(HttpMethod.GET, "/a", 200, "OK");
  }
}
