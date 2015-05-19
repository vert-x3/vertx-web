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

package io.vertx.ext.web.handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSHandlerTest extends WebTestBase {

  private static final Logger log = LoggerFactory.getLogger(SockJSHandlerTest.class);

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
    testNotFound("/echo//");
    testNotFound("/echo///");

    await();
  }

  private void testNotFound(String uri) {
    client.getNow(uri, resp -> {
      assertEquals(404, resp.statusCode());
      complete();
    });
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the 0.3.3 branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol/tree/v0.3.3
   */
  @Test
  public void testProtocol() throws Exception {
    String[] envp = new String[] {"SOCKJS_URL=http://localhost:8080"};
    File dir = new File("src/test/sockjs-protocol");
    Process p;
   // try {
      p = Runtime.getRuntime().exec("./venv/bin/python sockjs-protocol-0.3.3.py", envp, dir);
//    } catch (IOException e) {
//      if (e.getMessage().contains("No such file or directory")) {
//        System.out.println("Skipping SockJS protocol tests : could not run them");
//        return;
//      } else {
//        throw e;
//      }
//    }

    try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
      String line;
      while ((line = input.readLine()) != null) {
        log.info(line);
      }
    }

    int res = p.waitFor();

    // Make sure all tests pass
    assertEquals("Protocol tests failed", 0, res);

  }

}
