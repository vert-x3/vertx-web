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

import io.vertx.core.Context;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.ApexTestBase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author <a href="mailto:stephane.bastian.dev@gmail.com">Stéphane Bastian</a>
 */
public class BlockingHandlerTest extends ApexTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testBlockingHandler() throws Exception {
    List<Thread> threads = new ArrayList<>();
    List<Context> contexts = new ArrayList<>();
    router.route().handler(rc -> {
      System.out.println("route1 thread - " + Thread.currentThread());
      System.out.println("route1 context - " + rc.vertx().getOrCreateContext());
      threads.add(Thread.currentThread());
      contexts.add(rc.vertx().getOrCreateContext());
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      System.out.println("route2 thread - " + Thread.currentThread());
      System.out.println("route2 context - " + rc.vertx().getOrCreateContext());
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      rc.next();
    });
    router.route().blockingHandler(rc -> {
      System.out.println("route3 thread - " + Thread.currentThread());
      System.out.println("route3 context - " + rc.vertx().getOrCreateContext());
      assertTrue(!threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      rc.next();
    });
    router.route().handler(rc -> {
      System.out.println("route4 thread - " + Thread.currentThread());
      System.out.println("route4 context - " + rc.vertx().getOrCreateContext());
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      rc.next();
    });
    router.route().handler(rc -> {
      System.out.println("route5 thread - " + Thread.currentThread());
      System.out.println("route5 context - " + rc.vertx().getOrCreateContext());
      assertTrue(threads.get(0).equals(Thread.currentThread()));
      assertTrue(contexts.get(0).equals(rc.vertx().getOrCreateContext()));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }
  
}
