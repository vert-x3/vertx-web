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
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ResponseTimeTest extends WebTestBase {

  public ResponseTimeTest() {
    super(ReportMode.FORBIDDEN);
  }

  @Test
  public void testRequestTime1() throws Exception {
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String reqTime = resp.headers().get("x-response-time");
      Assert.assertNotNull(reqTime);
      Assert.assertTrue(reqTime.endsWith("ms"));
    }, 200, "OK", null);
  }

  @Test
  public void testRequestTime2() throws Exception {
    router.route().handler(ResponseTimeHandler.create());
    router.route().handler(rc -> vertx.setTimer(250, tid -> rc.response().end()));
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String reqTime = resp.headers().get("x-response-time");
      Assert.assertNotNull(reqTime);
      Assert.assertTrue(reqTime.endsWith("ms"));
      String time = reqTime.substring(0, reqTime.length() - 2);
      Integer dur = Integer.valueOf(time);
      Assert.assertTrue(dur >= 250);
    }, 200, "OK", null);
  }


}
