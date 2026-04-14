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

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ResponseTimeTest extends WebTestBase2 {

  @Test
  public void testRequestTime1() throws Exception {
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(rc -> rc.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    String reqTime = resp.headers().get("x-response-time");
    assertNotNull(reqTime);
    assertTrue(reqTime.endsWith("ms"));
  }

  @Test
  public void testRequestTime2() throws Exception {
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(rc -> vertx.setTimer(250, tid -> rc.response().end()));
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    String reqTime = resp.headers().get("x-response-time");
    assertNotNull(reqTime);
    assertTrue(reqTime.endsWith("ms"));
    String time = reqTime.substring(0, reqTime.length() - 2);
    Integer dur = Integer.valueOf(time);
    assertTrue(dur >= 250);
  }


}
