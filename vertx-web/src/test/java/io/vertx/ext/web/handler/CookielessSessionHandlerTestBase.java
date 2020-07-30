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
import io.vertx.ext.web.Session;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookielessSessionHandlerTestBase extends WebTestBase {

	private SessionStore store;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = LocalSessionStore.create(vertx);
  }

	@Test
	public void testSessionCreation() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		router.route().handler(rc -> {
		  rc.response()
        .putHeader("X-Session-Id", "(" + rc.session().value() + ")")
        .end();
    });
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String sessionId = resp.headers().get("X-Session-Id");
			assertTrue(sessionId.startsWith("("));
      assertTrue(sessionId.endsWith(")"));
		}, 200, "OK", null);
	}

	@Test
	public void testSession() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicInteger requestCount = new AtomicInteger();
		router.route().handler(rc -> {
			Session sess = rc.session();
			assertNotNull(sess);
			assertNotNull(sess.id());
			switch (requestCount.get()) {
			case 0:
				rid.set(sess.id());
				sess.put("foo", "bar");
				break;
			case 1:
				assertEquals(rid.get(), sess.id());
				assertEquals("bar", sess.get("foo"));
				sess.put("eek", "wibble");
				break;
			case 2:
				assertEquals(rid.get(), sess.id());
				assertEquals("bar", sess.get("foo"));
				assertEquals("wibble", sess.get("eek"));
			}
			requestCount.incrementAndGet();
			rc.response().end();
		});

		// 1st call will add the session id to the reference (a real user would dump it in the response body...)
		testRequest(HttpMethod.GET, "/", 200, "OK");
		Thread.sleep(1000);
		testRequest(HttpMethod.GET, "/(" + rid.get() + ")", 200, "OK");
		Thread.sleep(1000);
		testRequest(HttpMethod.GET, "/(" + rid.get() + ")",200, "OK");
	}

	@Test
	public void testSessionExpires() throws Exception {
		long timeout = 1000;
		router.route().handler(SessionHandler.create(store).setSessionTimeout(timeout).setCookieless(true));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicInteger requestCount = new AtomicInteger();
		router.route().handler(rc -> {
			Session sess = rc.session();
			assertNotNull(sess);
			assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
			assertNotNull(sess.id());
			switch (requestCount.get()) {
			case 0:
				sess.put("foo", "bar");
				break;
			case 1:
				assertFalse(rid.get().equals(sess.id())); // New session
				assertNull(sess.get("foo"));
				break;
			}
			rid.set(sess.id());
			requestCount.incrementAndGet();
			rc.response().end();
		});

		testRequest(HttpMethod.GET, "/", 200, "OK");
		Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    testRequest(HttpMethod.GET, "/(" + rid.get() + ")", 200, "OK");
    waitUntil(() -> testSessionBlocking(rid.get(), Objects::nonNull));
		Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    waitUntil(() -> testSessionBlocking(rid.get(), Objects::isNull));
  }

  private boolean testSessionBlocking(String sessionId, Function<Session, Boolean> test) {
    CompletableFuture<Boolean> cf = new CompletableFuture<>();
    store.get(sessionId, ar -> {
      if (ar.succeeded()) {
        cf.complete(test.apply(ar.result()));
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    try {
      return cf.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    } catch (ExecutionException e) {
      throw new AssertionError(e);
    }
  }

	@Test
	public void testDestroySession() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicInteger requestCount = new AtomicInteger();
		router.route().handler(rc -> {
			Session sess = rc.session();
			assertNotNull(sess);
			assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
			assertNotNull(sess.id());
			switch (requestCount.get()) {
			case 0:
				rid.set(sess.id());
				sess.put("foo", "bar");
				sess.destroy();
				break;
			case 1:
				assertFalse(rid.get().equals(sess.id())); // New session
				assertNull(sess.get("foo"));
				rid.set(sess.id());
				sess.destroy();
				break;
			}
			requestCount.incrementAndGet();
			rc.response().end();
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
		testRequest(HttpMethod.GET, "/", 200, "OK");
		Thread.sleep(500); // Needed because session.destroy is async
		CountDownLatch latch1 = new CountDownLatch(1);
		store.get(rid.get(), onSuccess(res -> {
			assertNull(res);
			latch1.countDown();
		}));
		awaitLatch(latch1);
	}

	@Test
	public void testLastAccessed1() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		AtomicReference<Session> rid = new AtomicReference<>();
		long start = System.currentTimeMillis();
		router.route().handler(rc -> {
			rid.set(rc.session());
			rc.response().end();
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
		assertTrue(rid.get().lastAccessed() - start < 500);
		start = System.currentTimeMillis();
		Thread.sleep(1000);
		testRequest(HttpMethod.GET, "/", 200, "OK");
		assertTrue(rid.get().lastAccessed() - start >= 1000);
	}

	@Test
	public void testLastAccessed2() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		AtomicReference<Session> rid = new AtomicReference<>();
		router.route().handler(rc -> {
			rid.set(rc.session());
			rc.session().put("foo", "bar");
			vertx.setTimer(1000, tid -> rc.response().end());
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
		// accessed() is called after request too
		assertTrue(rid.get().lastAccessed() - System.currentTimeMillis() < 500);
	}

	@Test
	public void testIssue172_setnull() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieless(true));
		AtomicReference<Session> rid = new AtomicReference<>();

		router.route().handler(rc -> {
			rid.set(rc.session());
			rc.session().put("foo", null);
			vertx.setTimer(1000, tid -> rc.response().end());
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
	}
}
