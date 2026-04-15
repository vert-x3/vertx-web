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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.ext.web.tests.WebTestBase;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stephane.bastian.dev@gmail.com">Stéphane Bastian</a>
 */
public class BlockingHandlerTest extends WebTestBase {

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
  }

  @Test
  public void testBlockingHandler() throws Exception {
    List<Thread> threads = new ArrayList<>();
    List<Context> contexts = new ArrayList<>();
    router.route().handler(rc -> {
      threads.add(Thread.currentThread());
      contexts.add(rc.vertx().getOrCreateContext());
      assertTrue(rc.currentRoute() != null);
      rc.response().setChunked(true);
      rc.response().write("A");
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.response().write("B");
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.response().write("C");
      rc.next();
    });
    router.route().handler(rc -> {
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.response().write("D");
      rc.next();
    });
    router.route().handler(rc -> {
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.response().write("E");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK", "ABCDE");
  }

  @Test
  public void testBlockingHandlerFailure() throws Exception {
    List<Thread> threads = new ArrayList<>();
    List<Context> contexts = new ArrayList<>();
    router.route().handler(rc -> {
      threads.add(Thread.currentThread());
      contexts.add(rc.vertx().getOrCreateContext());
      rc.response().setChunked(true);
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.fail(501);
    });
    router.route().failureHandler(rc -> {
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      rc.response().setStatusCode(rc.statusCode()).end();
    });
    testRequest(HttpMethod.GET, "/", 501, "Not Implemented");
  }

  @Test
  public void testBlockingHandlerFailureThrowException() throws Exception {
    List<Thread> threads = new ArrayList<>();
    List<Context> contexts = new ArrayList<>();
    router.route().handler(rc -> {
      threads.add(Thread.currentThread());
      contexts.add(rc.vertx().getOrCreateContext());
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      throw new RuntimeException("foo");
    });
    router.route().failureHandler(rc -> {
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      assertTrue(rc.currentRoute()!=null);
      Throwable t = rc.failure();
      assertNotNull(t);
      assertTrue(t instanceof RuntimeException);
      assertEquals("foo", t.getMessage());
      rc.response().setStatusCode(500).end();
    });
    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
  }

  @Test
  public void testExecuteBlockingParallel() throws Exception {

    long start = System.currentTimeMillis();
    int numExecBlocking = 5;
    long pause = 1000;

    router.route().blockingHandler(rc -> {
      try {
        Thread.sleep(pause);
      } catch (Exception ignore) {
      }
      rc.response().end();
    }, false);

    List<Future<Void>> futures = new ArrayList<>();
    for (int i = 0; i < numExecBlocking; i++) {
      futures.add(webClient.get("/").send()
        .expecting(HttpResponseExpectation.SC_OK)
        .mapEmpty());
    }
    for (Future<Void> future : futures) {
      future.await();
    }

    long now = System.currentTimeMillis();
    // we sleep for 5 seconds and we expect to be done within 2 + 1 seconds
    // this proves we run in parallel
    long leeway = 2000;
    assertTrue(now - start < pause + leeway);
  }

}
