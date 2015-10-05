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
 *
 * Kind of hard to test this!
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LoggerHandlerTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();

  }

  @Test
  public void testLogger1() throws Exception {
    LoggerHandler logger = LoggerHandler.create();
    testLogger(logger);
  }

  @Test
  public void testLogger2() throws Exception {
    LoggerHandler logger = LoggerHandler.create(LoggerFormat.TINY);
    testLogger(logger);
  }

  @Test
  public void testLogger3() throws Exception {
    LoggerHandler logger = LoggerHandler.create(true, LoggerFormat.TINY);
    testLogger(logger);
  }

  private void testLogger(LoggerHandler logger) throws Exception {
    router.route().handler(logger);
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/somedir", 200, "OK");
  }


}
