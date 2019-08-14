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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TimeoutHandlerTest extends WebTestBase {

  @Test
  public void testTimeout() throws Exception {
    long timeout = 500;
    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> {
      // Don't end it
    });
    testRequest(HttpMethod.GET, "/", 503, "Service Unavailable");
  }

  @Test
  public void testTimeoutWithCustomBodyEndHandler() throws Exception {
    long timeout = 500;

    AtomicBoolean ended = new AtomicBoolean();
    router.route().handler(routingContext -> {
      routingContext.addBodyEndHandler(event -> ended.set(true));
      routingContext.next();
    });

    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> {
      // Don't end it
    });
    testRequest(HttpMethod.GET, "/", 503, "Service Unavailable");

    waitUntil(ended::get);
  }


  @Test
  public void testTimeoutCancelled() throws Exception {
    long timeout = 500;
    router.route().handler(TimeoutHandler.create(timeout));
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
    Thread.sleep(1000); // Let timer kick in, if it's going to
  }


}
