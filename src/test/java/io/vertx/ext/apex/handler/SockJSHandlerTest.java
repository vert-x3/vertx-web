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

package io.vertx.ext.apex.handler;

import io.vertx.ext.apex.ApexTestBase;
import io.vertx.ext.apex.handler.sockjs.SockJSHandler;
import org.junit.Test;

/**
 * Port of https://github.com/sockjs/sockjs-protocol/blob/master/sockjs-protocol-0.3.3.py to Java
 *
 * TODO incomplete!
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSHandlerTest extends ApexTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    SockJSHandler.installTestApplications(router, vertx);
  }

  @Test
  public void testGreeting() {
    waitFor(2);
    testGreeting("/echo/");
    testGreeting("/echo");
    await();
  }

  private void testGreeting(String uri) {
    client.getNow(uri, resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals("text/plain; charset=UTF-8", resp.getHeader("content-type"));
      resp.bodyHandler(buff -> {
        assertEquals("Welcome to SockJS!\n", buff.toString());
        complete();
      });
    });
  }

  @Test
  public void testNotFound() {
    waitFor(5);

    testNotFound("/echo/a");
    testNotFound("/echo/a.html");
    testNotFound("/echo/a/a");
    testNotFound("/echo/a/a/");
    testNotFound("/echo/a/");

    await();
  }

  private void testNotFound(String uri) {
    client.getNow(uri, resp -> {
      assertEquals(404, resp.statusCode());
      complete();
    });
  }

  // TODO complete this


}
