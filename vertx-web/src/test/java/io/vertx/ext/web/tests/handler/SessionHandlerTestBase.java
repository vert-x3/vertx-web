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

import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.AbstractSession;
import io.vertx.test.core.TestUtils;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.Assert;
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
public abstract class SessionHandlerTestBase extends WebTestBase {

	public SessionHandlerTestBase() {
		super(ReportMode.FORBIDDEN);
	}

	public SessionHandlerTestBase(ReportMode reportMode) {
		super(reportMode);
	}

	protected SessionStore store;

	@Test
	public void testSessionCookieName() throws Exception {
		String sessionCookieName = "acme.sillycookie";
		router.route().handler(SessionHandler.create(store).setSessionCookieName(sessionCookieName));
		router.route().handler(rc -> rc.response().end());
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertTrue(setCookie.startsWith(sessionCookieName + "="));
		}, 200, "OK", null);
	}

	@Test
	public void testSessionCookiePath() throws Exception {
		router.route().handler(SessionHandler.create(store).setSessionCookiePath("/path"));
		router.route().handler(rc -> rc.response().end());
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertTrue(setCookie.contains("Path=/path"));
		}, 200, "OK", null);
	}

	@Test
	public void testSessionCookieHttpOnlyFlag() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieHttpOnlyFlag(true));
		router.route().handler(rc -> rc.response().end());

		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");

			Assert.assertTrue(setCookie.contains("; HTTPOnly"));
		}, 200, "OK", null);
	}

	@Test
	public void testSessionCookieSecureFlag() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieSecureFlag(true));
		router.route().handler(rc -> rc.response().end());

		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");

			Assert.assertTrue(setCookie.contains("; Secure"));
		}, 200, "OK", null);
	}

	@Test
	public void testSessionCookieSecureFlagAndHttpOnlyFlags() throws Exception {
		router.route().handler(SessionHandler.create(store).setCookieSecureFlag(true).setCookieHttpOnlyFlag(true));
		router.route().handler(rc -> rc.response().end());

		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");

			Assert.assertTrue(setCookie.contains("; Secure"));
			Assert.assertTrue(setCookie.contains("; HTTPOnly"));
		}, 200, "OK", null);
	}

	@Test
	public void testSessionFields() throws Exception {
		router.route().handler(SessionHandler.create(store));
		AtomicReference<String> rid = new AtomicReference<>();
		router.route().handler(rc -> {
			Session sess = rc.session();
			Assert.assertNotNull(sess);
			Assert.assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
			Assert.assertNotNull(sess.id());
			rid.set(sess.value());
			Assert.assertFalse(sess.isDestroyed());
			Assert.assertEquals(SessionHandler.DEFAULT_SESSION_TIMEOUT, sess.timeout());
			rc.response().end();
		});
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertTrue(setCookie.startsWith(SessionHandler.DEFAULT_SESSION_COOKIE_NAME + "="));
			int pos = setCookie.indexOf("; Path=" + SessionHandler.DEFAULT_SESSION_COOKIE_PATH);
			String sessID = setCookie.substring(18, pos);
			Assert.assertEquals(rid.get(), sessID);
		}, 200, "OK", null);
	}

	@Test
	public void testSession() throws Exception {
    SessionHandler sessionHandler = SessionHandler.create(store);
    router.route().handler(sessionHandler);
    AtomicReference<String> rid = new AtomicReference<>();
    AtomicInteger requestCount = new AtomicInteger();
    router.route().handler(rc -> {
      Session sess = rc.session();
      Assert.assertNotNull(sess);
      Assert.assertNotNull(sess.id());
      switch (requestCount.get()) {
        case 0:
          rid.set(sess.id());
          sess.put("foo", "bar");
          break;
        case 1:
          Assert.assertEquals(rid.get(), sess.id());
          Assert.assertEquals("bar", sess.get("foo"));
          sess.put("eek", "wibble");
          break;
        case 2:
          Assert.assertEquals(rid.get(), sess.id());
          Assert.assertEquals("bar", sess.get("foo"));
          Assert.assertEquals("wibble", sess.get("eek"));
      }
      requestCount.incrementAndGet();
      sessionHandler.flush(rc).onFailure(rc::fail).onSuccess(v -> rc.response().end());
    });
    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), resp -> {
      String setCookie = resp.headers().get("set-cookie");
      // if the cookie was regenerated
      if (setCookie != null) {
        rSetCookie.set(setCookie);
      }
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
  }

	@Test
	public void testSessionExpires() throws Exception {
		long timeout = 1000;
		router.route().handler(SessionHandler.create(store).setSessionTimeout(timeout));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicInteger requestCount = new AtomicInteger();
		router.route().handler(rc -> {
			Session sess = rc.session();
			Assert.assertNotNull(sess);
			Assert.assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
			Assert.assertNotNull(sess.id());
			switch (requestCount.get()) {
			case 0:
				sess.put("foo", "bar");
				break;
			case 1:
				Assert.assertFalse(rid.get().equals(sess.id())); // New session
				Assert.assertNull(sess.get("foo"));
				break;
			}
			rid.set(sess.id());
			requestCount.incrementAndGet();
			rc.response().end();
		});
		AtomicReference<String> rSetCookie = new AtomicReference<>();
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			rSetCookie.set(setCookie);
		}, 200, "OK", null);
		Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
    waitUntil(() -> testSessionBlocking(rid.get(), Objects::nonNull));
		Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    waitUntil(() -> testSessionBlocking(rid.get(), Objects::isNull));
  }

  private boolean testSessionBlocking(String sessionId, Function<Session, Boolean> test) {
    CompletableFuture<Boolean> cf = new CompletableFuture<>();
    store.get(sessionId).onComplete(ar -> {
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
		router.route().handler(SessionHandler.create(store));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicInteger requestCount = new AtomicInteger();
		router.route().handler(rc -> {
			Session sess = rc.session();
			Assert.assertNotNull(sess);
			Assert.assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
			Assert.assertNotNull(sess.id());
			switch (requestCount.get()) {
			case 0:
				rid.set(sess.id());
				sess.put("foo", "bar");
				sess.destroy();
				break;
			case 1:
				Assert.assertFalse(rid.get().equals(sess.id())); // New session
				Assert.assertNull(sess.get("foo"));
				rid.set(sess.id());
				sess.destroy();
				break;
			}
			requestCount.incrementAndGet();
			rc.response().end();
		});
		testRequest(HttpMethod.GET, "/", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertNull(setCookie);
			// the cookie got destroyed even before the end of the request, so no side
			// effects are expected
		}, 200, "OK", null);
		testRequest(HttpMethod.GET, "/", null, null, 200, "OK", null);
		Thread.sleep(500); // Needed because session.destroy is async
		CountDownLatch latch1 = new CountDownLatch(1);
		store.get(rid.get()).onComplete(TestUtils.onSuccess(res -> {
			Assert.assertNull(res);
			latch1.countDown();
		}));
		TestUtils.awaitLatch(latch1);
	}

	@Test
	public void testLastAccessed1() throws Exception {
    router.route().handler(SessionHandler.create(store));
    AtomicReference<Session> rid = new AtomicReference<>();
    router.route().handler(rc -> {
      rid.set(rc.session());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    long start = rid.get().lastAccessed();
    int millis = 250;
    Thread.sleep(millis);
    testRequest(HttpMethod.GET, "/", 200, "OK");
    Assert.assertTrue(rid.get().lastAccessed() - start >= millis);
  }

	@Test
	public void testLastAccessed2() throws Exception {
		router.route().handler(SessionHandler.create(store));
		AtomicReference<Session> rid = new AtomicReference<>();
		router.route().handler(rc -> {
			rid.set(rc.session());
			rc.session().put("foo", "bar");
			vertx.setTimer(1000, tid -> rc.response().end());
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
		// accessed() is called after request too
		Assert.assertTrue(rid.get().lastAccessed() - System.currentTimeMillis() < 500);
	}

	@Test
	public void testIssue172_setnull() throws Exception {
		router.route().handler(SessionHandler.create(store));
		AtomicReference<Session> rid = new AtomicReference<>();

		router.route().handler(rc -> {
			rid.set(rc.session());
			rc.session().put("foo", null);
			vertx.setTimer(1000, tid -> rc.response().end());
		});
		testRequest(HttpMethod.GET, "/", 200, "OK");
	}

	@Test
	public void testSessionCookieAttack() throws Exception {
		router.route().handler(SessionHandler.create(store));
		// faking that there was some auth error
		router.route().handler(rc -> rc.fail(401));

		testRequest(HttpMethod.GET, "/", null, resp -> Assert.assertNull(resp.headers().get("set-cookie")), 401,
				"Unauthorized", null);
	}

	@Test
	public void testSessionCookieInvalidatedOnError() throws Exception {
		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicReference<String> id = new AtomicReference<>();

		router.route().handler(SessionHandler.create(store));
		// faking some error
		router.route().handler(rc -> {
			switch (counter.getAndIncrement()) {
			case 0:
				// store the reference
				id.set(rc.session().id());
				rc.response().end();
				break;
			case 1:
				Assert.assertEquals(id.get(), rc.session().id());
				rc.fail(500);
				break;
			case 2:
				Assert.assertEquals(id.get(), rc.session().id());
				rc.response().end();
				break;
			}
		});

		AtomicReference<String> sessionID = new AtomicReference<>();
		// first call will get a session cookie
		testRequest(HttpMethod.GET, "/", null, resp -> {
			Assert.assertNotNull(resp.headers().get("set-cookie"));
			String setCookie = resp.headers().get("set-cookie");
			sessionID.set(setCookie);
		}, 200, "OK", null);
		// ensure that on the second call, in case of error, the cookie is not present
		testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", sessionID.get()),
				resp -> Assert.assertNull(resp.headers().get("set-cookie")), 500, "Internal Server Error", null);
		// ensure that on the third call, the session is still valid
		testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", sessionID.get()),
				resp -> Assert.assertNull(resp.headers().get("set-cookie")), 200, "OK", null);
	}

	protected long doTestSessionRetryTimeout() throws Exception {
    SessionHandler sessionHandler = SessionHandler.create(store);
		router.route().handler(sessionHandler);
		AtomicReference<Session> rid = new AtomicReference<>();

		router.get("/0").handler(rc -> {
			rid.set(rc.session());
			rc.session().put("foo", "foo_value");
			rc.response().end();
		});
		router.get("/1").handler(rc -> {
			rid.set(rc.session());
			Assert.assertEquals("foo_value", rc.session().get("foo"));
      rc.session().destroy();
      sessionHandler.flush(rc).onComplete(v -> rc.response().end(), rc::fail);
		});
		router.get("/2").handler(rc -> {
			rid.set(rc.session());
			Assert.assertEquals(null, rc.session().<String>get("foo"));
			rc.response().end();
		});

		AtomicReference<String> sessionID = new AtomicReference<>();
		testRequest(HttpMethod.GET, "/0", req -> {
		}, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			System.out.println(setCookie);
			sessionID.set(setCookie);
		}, 200, "OK", null);
		CountDownLatch responseReceived = new CountDownLatch(1);
		testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", sessionID.get()), resp -> {
			responseReceived.countDown();
		}, 200, "OK", null);
		TestUtils.awaitLatch(responseReceived);
		long now = System.nanoTime();
		testRequest(HttpMethod.GET, "/2", req -> req.putHeader("cookie", sessionID.get()), 200, "OK", null);
		return MILLISECONDS.convert(System.nanoTime() - now, NANOSECONDS);
	}

  @Test
  public void testInvalidation() throws Exception {
    router.route().handler(SessionHandler.create(store).setCookieSameSite(CookieSameSite.STRICT));
    AtomicReference<Session> rid = new AtomicReference<>();

    router.get("/0").handler(rc -> {
      rid.set(rc.session());
      rc.session().put("foo", "foo_value");
      rc.response().end();
    });
    router.get("/1").handler(rc -> {
      rid.set(rc.session());
      Assert.assertEquals("foo_value", rc.session().get("foo"));
      rc.session().destroy();
      rc.response().end();
    });

    AtomicReference<String> sessionID = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/0", req -> {
    }, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      sessionID.set(setCookie);
    }, 200, "OK", null);
    CountDownLatch responseReceived = new CountDownLatch(1);
    testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", sessionID.get()), resp -> {
      // ensure that expired cookies still contain the the configured properties
      Assert.assertTrue(resp.headers().get("set-cookie").contains("SameSite=Strict"));
      responseReceived.countDown();
    }, 200, "OK", null);
    TestUtils.awaitLatch(responseReceived);
  }

	@Test
	public void testSessionFixation() throws Exception {

		final AtomicReference<String> sessionId = new AtomicReference<>();

		router.route().handler(SessionHandler.create(store));
		// call #0 anonymous a random id should be returned
		router.route("/0").handler(rc -> {
			sessionId.set(rc.session().value());
			rc.response().end();
		});
		// call #1 fake auth security upgrade is done so session id must change
		router.route("/1").handler(rc -> {
			// previous id must match
			Assert.assertEquals(sessionId.get(), rc.session().value());
			rc.session().regenerateId();
			rc.response().end();
		});

		testRequest(HttpMethod.GET, "/0", null, resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertNotNull(setCookie);
		}, 200, "OK", null);

		CountDownLatch responseReceived = new CountDownLatch(1);
		testRequest(HttpMethod.GET, "/1",
				req -> req.putHeader("cookie", "vertx-web.session=" + sessionId.get() + "; Path=/"), resp -> {
					String setCookie = resp.headers().get("set-cookie");
					Assert.assertNotNull(setCookie);
					Assert.assertFalse(("vertx-web.session=" + sessionId.get() + "; Path=/").equals(setCookie));
					responseReceived.countDown();
				}, 200, "OK", null);
		TestUtils.awaitLatch(responseReceived);

    assertWaitUntil(() -> {
      CompletableFuture<Session> cf = new CompletableFuture<>();
      store.get(sessionId.get()).onComplete(get -> {
        if (get.succeeded()) {
          cf.complete(get.result());
        } else {
          cf.completeExceptionally(get.cause());
        }
      });
      try {
        return cf.get() == null;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        throw new RuntimeException(e.getCause());
      }
    }, TimeUnit.MILLISECONDS.convert(1, MINUTES), "old id must not be valid anymore");
	}

	@Test
	public void testSessionIdLength() throws Exception {

		router.route().handler(SessionHandler.create(store));

		router.route("/1").handler(rc -> {
			// previous id must match
			Assert.assertFalse("abc".equals(rc.session().id()));
			rc.response().end();
		});

		testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", "vertx-web.session=abc; Path=/"), resp -> {
			String setCookie = resp.headers().get("set-cookie");
			Assert.assertNotNull(setCookie);
		}, 200, "OK", null);
	}

	@Test
	public void testVersion() throws Exception {
		AbstractSession session = (AbstractSession) store.createSession(10000);

		Assert.assertEquals(0, session.version());
		session.put("k", "v");

		store.put(session).onComplete(res -> {
			if (res.failed()) {
				Assert.fail("failed to store");
			}
			store.get(session.value()).onComplete(res1 -> {
				if (res1.failed()) {
					Assert.fail("failed to store");
				}

				AbstractSession session1 = (AbstractSession) res1.result();
				// session was stored for the first time so it must have version 1
				Assert.assertEquals(1, session1.version());
				// confirm that the content is present
				Assert.assertEquals("v", session1.get("k"));

				store.put(session1).onComplete(res2 -> {
					if (res2.failed()) {
						Assert.fail("failed to store");
					}
					store.get(session1.value()).onComplete(res3 -> {
						if (res3.failed()) {
							Assert.fail("failed to store");
						}

						AbstractSession session2 = (AbstractSession) res3.result();
						// session was stored again but no changes were applied to the content
						// therefore version should remain the same
						Assert.assertEquals(1, session2.version());
						// confirm the content is present
						Assert.assertEquals("v", session2.get("k"));

						// update content
						session2.put("k", "w");

						store.put(session2).onComplete(res4 -> {
							if (res4.failed()) {
								Assert.fail("failed to store");
							}
							store.get(session2.value()).onComplete(res5 -> {
								if (res5.failed()) {
									Assert.fail("failed to store");
								}

								AbstractSession session3 = (AbstractSession) res5.result();
								// session was stored again but changes were applied to the content
								// therefore version should must increment
								Assert.assertEquals(2, session3.version());
								// confirm the content is present
								Assert.assertEquals("w", session3.get("k"));
								testComplete();
							});
						});
					});
				});
			});
		});

		await();
	}

  @Test
  public void testLazySessionNotAccessed() throws Exception {
    String sessionCookieName = "acme.sillycookie";
    router.route().handler(SessionHandler.create(store).setSessionCookieName(sessionCookieName).setLazySession(true));
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      Assert.assertTrue(setCookie == null);
    }, 200, "OK", null);
  }

  @Test
  public void testLazySessionAccessed() throws Exception {
    String sessionCookieName = "acme.sillycookie";
    router.route().handler(SessionHandler.create(store).setSessionCookieName(sessionCookieName).setLazySession(true));
    router.route().handler(rc -> {
      rc.session();
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      Assert.assertTrue(setCookie.startsWith(sessionCookieName + "="));
    }, 200, "OK", null);
  }

  @Test
  public void testRegenerateAndCookies() throws Exception {

    final SessionHandler sessionHandler =
      SessionHandler.create(LocalSessionStore.create(vertx))
        .setLazySession(true)
        .setCookieHttpOnlyFlag(true)
        .setSessionCookieName("vid");

    router.route("/login")
      .handler(sessionHandler)
      .handler(ctx -> {
        ctx.session().regenerateId();
        ctx.end(ctx.request().path());
      });

    router.route("/user")
      .handler(sessionHandler)
      .handler(ctx -> ctx.end(ctx.request().path()));

    final AtomicReference<String> sessionID = new AtomicReference<>();

    testRequest(HttpMethod.GET, "/login", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      Assert.assertNotNull(setCookie);
      sessionID.set(setCookie);
    }, 200, "OK", null);

    // second call
    testRequest(HttpMethod.GET, "/user", req -> {
      req.putHeader("Cookie", sessionID.get());
    }, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      // cookie doesn't change, no need to re-issue it again
      Assert.assertNull(setCookie);
    }, 200, "OK", null);
  }

  @Test
  public void testSessionCookieSigning() throws Exception {
    final AtomicReference<String> sessionId = new AtomicReference<>();
    final AtomicReference<String> firstSessionId = new AtomicReference<>();
    final AtomicReference<String> sessionHeader = new AtomicReference<>();
    final SessionHandler handler = SessionHandler.create(store)
      .setSigningSecret("any-string-value");

    router.route().handler(handler);
    // capture the session ID
    router.route("/0").handler(rc -> {
      sessionId.set(rc.session().value());
      rc.response().end();
    });

    // Initiate a session and check it's signed
    testRequest(HttpMethod.GET, "/0", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      Assert.assertNotNull(setCookie);
      String cookieData = setCookie.substring(setCookie.indexOf("=") + 1, setCookie.indexOf(";"));

      Assert.assertFalse(sessionId.get().isEmpty());
      Assert.assertTrue(cookieData.contains(sessionId.get()));

      String[] cookieParts = cookieData.split("\\.");
      // Cookie session has an id with a signature in, so check we added another one
      Assert.assertEquals(sessionId.get().split("\\.").length + 1, cookieParts.length);

      firstSessionId.set(sessionId.get());
      sessionHeader.set(setCookie);
    }, 200, "OK", null);

    // check the signed cookie can be used to access the session
    testRequest(HttpMethod.GET, "/0", req -> {
      req.putHeader("cookie", sessionHeader.get());
    }, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      // check not issued a new session
      Assert.assertNull(setCookie);
      Assert.assertEquals(firstSessionId.get(), sessionId.get());
    }, 200, "OK", null);

    // Finally edit the cookie to show signature rejects it
    testRequest(HttpMethod.GET, "/0", req -> {
      req.putHeader("cookie", sessionHeader.get().replaceFirst(sessionId.get(), "random-session-id"));
    }, resp -> {
      // check new session created
      String setCookie = resp.headers().get("set-cookie");
      Assert.assertNotNull(setCookie);
    }, 200, "OK", null);
  }
}
