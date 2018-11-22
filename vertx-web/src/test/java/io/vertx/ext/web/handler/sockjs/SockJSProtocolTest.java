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
package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.buffer.Buffer.buffer;
import static org.junit.Assert.assertEquals;

/**
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSProtocolTest {

  private static final Logger log = LoggerFactory.getLogger(SockJSProtocolTest.class);

  private static Vertx vertx;
  private static HttpServer server;

  @BeforeClass
  public static void before() throws Exception {
    vertx = Vertx.vertx();
    server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    installTestApplications(router, vertx);
    server.requestHandler(router).listen(8081);
  }

  @AfterClass
  public static void after() {
    server.close();
    vertx.close();
  }

  public static void installTestApplications(Router router, Vertx vertx) {

    // These applications are required by the SockJS protocol and QUnit tests

    router.route("/echo/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> sock.handler(sock::write)));
    router.route("/close/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> {
        // Close with a small delay so the opening sockjs frame "o" is not aggregated in the same TCP frame
        // than the SockJS close frame "c[3000,"Go away!"]"
        vertx.setTimer(10, id -> sock.close(3000, "Go away!"));
    }));
    router.route("/disabled_websocket_echo/*").handler(SockJSHandler.create(vertx, new SockJSHandlerOptions()
      .setMaxBytesStreaming(4096).addDisabledTransport("WEBSOCKET")).socketHandler(sock -> sock.handler(sock::write)));
    router.route("/ticker/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> {
      long timerID = vertx.setPeriodic(1000, tid -> sock.write(buffer("tick!")));
      sock.endHandler(v -> vertx.cancelTimer(timerID));
    }));
    router.route("/amplify/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> sock.handler(data -> {
      String str = data.toString();
      int n = Integer.valueOf(str);
      if (n < 0 || n > 19) {
        n = 1;
      }
      int num = (int) Math.pow(2, n);
      Buffer buff = buffer(num);
      for (int i = 0; i < num; i++) {
        buff.appendByte((byte) 'x');
      }
      sock.write(buff);
    })));
    router.route("/broadcast/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(new Handler<SockJSSocket>() {
      Set<String> connections = new HashSet<>();

      public void handle(SockJSSocket sock) {
        connections.add(sock.writeHandlerID());
        sock.handler(buffer -> {
          for (String actorID : connections) {
            vertx.eventBus().publish(actorID, buffer);
          }
        });
        sock.endHandler(v -> connections.remove(sock.writeHandlerID()));
      }
    }));
    router.route("/cookie_needed_echo/*").handler(SockJSHandler.create(vertx, new SockJSHandlerOptions().
      setMaxBytesStreaming(4096).setInsertJSESSIONID(true)).socketHandler(sock -> sock.handler(sock::write)));
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the master branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol
   */
  @Test
  public void testProtocol() throws Exception {
    // does this system have python 2.x?
    Process p = Runtime.getRuntime().exec("python pythonversion.py", null, new File("src/test"));
    int res = p.waitFor();

    if (res == 0) {
      File dir = new File("src/test/sockjs-protocol");
      p = Runtime
          .getRuntime()
          .exec("python sockjs-protocol.py", new String[]{"SOCKJS_URL=http://localhost:8081"}, dir);

      try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
        String line;
        while ((line = input.readLine()) != null) {
          log.info(line);
        }
      }

      res = p.waitFor();

      // Make sure all tests pass
      assertEquals("Protocol tests failed", 0, res);
    } else {
      System.err.println("*** No Python runtime sockjs tests will be skiped!!!");
    }
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the master branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol
   */
  @Test
  public void testQuirks() throws Exception {
    // does this system have python 2.x?
    Process p = Runtime.getRuntime().exec("python pythonversion.py", null, new File("src/test"));
    int res = p.waitFor();

    if (res == 0) {
      File dir = new File("src/test/sockjs-protocol");
      p = Runtime
        .getRuntime()
        .exec("python http-quirks.py", new String[]{"SOCKJS_URL=http://localhost:8081"}, dir);

      try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
        String line;
        while ((line = input.readLine()) != null) {
          log.info(line);
        }
      }

      res = p.waitFor();

      // Make sure all tests pass
      assertEquals("Protocol tests failed", 0, res);
    } else {
      System.err.println("*** No Python runtime sockjs tests will be skiped!!!");
    }
  }
}
