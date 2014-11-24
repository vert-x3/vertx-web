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

package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.Timeout;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TimeoutTest extends ApexTestBase {

  @Test
  public void testTimeout() throws Exception {
    long timeout = 500;
    router.route().handler(Timeout.timeout(timeout));
    router.route().handler(rc -> {
      // Don't end it
    });
    testRequest(HttpMethod.GET, "/", 408, "Request Timeout");
  }

  @Test
  public void testTimeoutCancelled() throws Exception {
    long timeout = 500;
    router.route().handler(Timeout.timeout(timeout));
    router.route().handler(rc -> {
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    Thread.sleep(1000); // Let timer kick in, if it's going to
  }


}
