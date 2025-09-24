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
package io.vertx.ext.web.tests.handler.sockjs;

import io.netty.util.internal.PlatformDependent;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.vertx.core.buffer.Buffer.buffer;
import static org.junit.Assert.assertTrue;

import org.testcontainers.DockerClientFactory;

/**
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSProtocolTest {

  private static Vertx vertx;
  private static HttpServer server;

    public static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }
  @BeforeClass
  public static void before() throws Exception {
    //Ignoring tests if docker is not configured
    Assume.assumeTrue("Docker is not available.", isDockerAvailable());
    vertx = Vertx.vertx();
    server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    installTestApplications(router, vertx);
    server.requestHandler(router).listen(8081, "localhost");
  }

  @AfterClass
  public static void after() {
    //Ignoring tests if docker is not configured
    if (isDockerAvailable()) { 
      server.close();
      vertx.close();
    }
  }

  private String runPython(String cmd, Predicate<String> exitTest) throws Exception {
    StringBuilder output = new StringBuilder();
    String path = new File("./src/test/sockjs-protocol/").getCanonicalPath();
    Testcontainers.exposeHostPorts(8081);
    try {
      try (GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("python:2.7-alpine"))) {
        container.withFileSystemBind(path, "/usr/src/myapp");
        container.withWorkingDirectory("/usr/src/myapp");
        container.addEnv("SOCKJS_URL", "host.testcontainers.internal:8081");
        CountDownLatch latch = new CountDownLatch(1);
        container.withLogConsumer(frame -> {
          String s = frame.getUtf8String();
          output.append(frame.getUtf8String());
          if (exitTest.test(s)) {
            latch.countDown();
          }
        });
        container.withCommand(cmd);
        container.start();
        latch.await(50, TimeUnit.SECONDS);
      }
    } finally {
      Testcontainers.exposeHostPorts();
    }
    return output.toString();
  }

  public static void installTestApplications(Router router, Vertx vertx) {

    // These applications are required by the SockJS protocol and QUnit tests
    router.post().handler(BodyHandler.create());

    router.route("/echo*").subRouter(
      SockJSHandler.create(
        vertx,
        new SockJSHandlerOptions().setMaxBytesStreaming(4096))
        .socketHandler(sock -> sock.handler(sock::write)));

    router.route("/close*").subRouter(
      SockJSHandler.create(vertx,
        new SockJSHandlerOptions().setMaxBytesStreaming(4096))
        .socketHandler(sock -> {
          // Close with a small delay so the opening sockjs frame "o" is not aggregated in the same TCP frame
          // than the SockJS close frame "c[3000,"Go away!"]"
          vertx.setTimer(10, id -> sock.close(3000, "Go away!"));
        }));
    router.route("/disabled_websocket_echo*").subRouter(
      SockJSHandler.create(vertx, new SockJSHandlerOptions()
        .setMaxBytesStreaming(4096).addDisabledTransport("WEBSOCKET"))
        .socketHandler(sock -> sock.handler(sock::write)));
    router.route("/ticker*").subRouter(
      SockJSHandler.create(vertx,
        new SockJSHandlerOptions().setMaxBytesStreaming(4096))
        .socketHandler(sock -> {
          long timerID = vertx.setPeriodic(1000, tid -> sock.write(buffer("tick!")));
          sock.endHandler(v -> vertx.cancelTimer(timerID));
        }));
    router.route("/amplify*").subRouter(
      SockJSHandler.create(vertx,
        new SockJSHandlerOptions().setMaxBytesStreaming(4096))
        .socketHandler(sock -> sock.handler(data -> {
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
    router.route("/broadcast*").subRouter(
      SockJSHandler.create(vertx,
        new SockJSHandlerOptions().setMaxBytesStreaming(4096).setRegisterWriteHandler(true))
        .socketHandler(new Handler<SockJSSocket>() {
          Set<String> connections = new HashSet<>();

          public void handle(SockJSSocket sock) {
            String writeHandlerID = sock.writeHandlerID();
            if (writeHandlerID != null) {
              connections.add(writeHandlerID);
              sock.handler(buffer -> {
                for (String actorID : connections) {
                  vertx.eventBus().publish(actorID, buffer);
                }
              });
              sock.endHandler(v -> connections.remove(writeHandlerID));
            }
          }
        }));
    SockJSHandlerOptions options = new SockJSHandlerOptions().
      setMaxBytesStreaming(4096)
      .setInsertJSESSIONID(true);
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
    Router socketHandler = sockJSHandler.socketHandler(sock -> sock.handler(sock::write));
    router.route("/cookie_needed_echo*").subRouter(socketHandler);
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the master branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol
   */
  @Test
  public void testProtocol() throws Exception {
    Assume.assumeFalse(PlatformDependent.isWindows());
    String output = runPython("python sockjs-protocol.py", s -> s.startsWith("OK"));
    assertTrue(output, output.contains("Ran 67 tests"));
  }

  /*
  We run the actual Python SockJS protocol tests - these are taken from the master branch of the sockjs-protocol repository:
  https://github.com/sockjs/sockjs-protocol
   */
  @Test
  public void testQuirks() throws Exception {
    Assume.assumeFalse(PlatformDependent.isWindows());
    String output = runPython("python http-quirks.py", s -> s.startsWith("OK"));
    assertTrue(output, output.contains("Ran 1 test"));
  }
}
