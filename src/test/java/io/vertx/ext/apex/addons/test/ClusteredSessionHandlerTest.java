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

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.apex.addons.impl.ClusteredSessionStore;
import io.vertx.ext.apex.addons.impl.SessionHandler;
import io.vertx.ext.apex.core.CookieHandler;
import io.vertx.ext.apex.core.Router;
import io.vertx.ext.apex.core.Session;
import io.vertx.ext.apex.core.SessionStore;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionHandlerTest extends SessionHandlerTestBase {

  int numNodes = 3;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    VertxOptions options = new VertxOptions();
    options.setClustered(true);
    options.setClusterManager(new FakeClusterManager());
    startNodes(numNodes);
    store = ClusteredSessionStore.clusteredSessionStore(vertices[0]);
  }

  @Test
  public void testClusteredSession() throws Exception {
    Router router1 = Router.router(vertices[0]);
    router1.route().handler(CookieHandler.cookieHandler());
    SessionStore store1 = ClusteredSessionStore.clusteredSessionStore(vertices[0]);
    router1.route().handler(SessionHandler.sessionHandler(store1));
    HttpServer server1 = vertices[0].createHttpServer(new HttpServerOptions().setPort(8081).setHost("localhost"));
    server1.requestHandler(router1::accept);
    CountDownLatch latch1 = new CountDownLatch(1);
    server1.listen(onSuccess(s -> latch1.countDown()));
    HttpClient client1 = vertices[0].createHttpClient(new HttpClientOptions());

    Router router2 = Router.router(vertices[1]);
    router2.route().handler(CookieHandler.cookieHandler());
    SessionStore store2 = ClusteredSessionStore.clusteredSessionStore(vertices[1]);
    router2.route().handler(SessionHandler.sessionHandler(store2));
    HttpServer server2 = vertices[1].createHttpServer(new HttpServerOptions().setPort(8082).setHost("localhost"));
    server2.requestHandler(router2::accept);
    CountDownLatch latch2 = new CountDownLatch(1);
    server2.listen(onSuccess(s -> latch2.countDown()));
    HttpClient client2 = vertices[0].createHttpClient(new HttpClientOptions());

    Router router3 = Router.router(vertices[2]);
    router3.route().handler(CookieHandler.cookieHandler());
    SessionStore store3 = ClusteredSessionStore.clusteredSessionStore(vertices[2]);
    router3.route().handler(SessionHandler.sessionHandler(store3));
    HttpServer server3 = vertices[2].createHttpServer(new HttpServerOptions().setPort(8083).setHost("localhost"));
    server3.requestHandler(router3::accept);
    CountDownLatch latch3 = new CountDownLatch(1);
    server3.listen(onSuccess(s -> latch3.countDown()));
    HttpClient client3 = vertices[0].createHttpClient(new HttpClientOptions());

    router1.route().handler(rc -> {
      Session sess = rc.session();
      sess.data().put("foo", "bar");
      rc.response().end();
    });

    router2.route().handler(rc -> {
      Session sess = rc.session();
      assertEquals("bar", sess.data().getString("foo"));
      sess.data().put("eek", "wibble");
      rc.response().end();
    });

    router3.route().handler(rc -> {
      Session sess = rc.session();
      assertEquals("bar", sess.data().getString("foo"));
      assertEquals("wibble", sess.data().getString("eek"));
      rc.response().end();
    });

    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequestBuffer(client1, HttpMethod.GET, 8081, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    testRequestBuffer(client2, HttpMethod.GET, 8082, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);
    testRequestBuffer(client3, HttpMethod.GET, 8083, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);

  }

}
