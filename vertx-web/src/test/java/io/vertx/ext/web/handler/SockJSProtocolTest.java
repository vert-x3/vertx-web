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
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSProtocolTest extends WebTestBase {

  private static final Logger log = LoggerFactory.getLogger(SockJSProtocolTest.class);

  @Override
  public void setUp() throws Exception {
    super.setUp();
    SockJSHandler.installTestApplications(router, vertx);
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the 0.3.3 branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol/tree/v0.3.3
   */
  @Test
  public void testProtocol() throws Exception {
    String sockjsenv = "SOCKJS_URL=http://localhost:8080";
    File dir = new File("/home/plopes/Projects/sockjs/sockjs-protocol");
    String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    Process p;
    if (OS.contains("mac") || OS.contains("darwin")) {
      // Use default python installation on OSX
      String[] envp = {
          sockjsenv,
          "PYTHONPATH=" + new File("src/test/sockjs-protocol/venv/lib/python2.7/site-packages").getAbsolutePath()
      };
      p = Runtime.getRuntime().exec("python sockjs-protocol-0.3.3.py", envp, dir);
    } else {
      String[] envp = new String[] {"SOCKJS_URL=http://localhost:8080"};
      p = Runtime.getRuntime().exec("python sockjs-protocol-0.3.3.py", envp, dir);
    }

    log.info("--------------------------------");
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
