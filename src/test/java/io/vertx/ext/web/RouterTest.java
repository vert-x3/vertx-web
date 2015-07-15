/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterTest extends WebTestBase {

  @Test
  public void testSimpleRoute() throws Exception {
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testInvalidPath() throws Exception {
    try {
      router.route("blah");
      fail();
    } catch (IllegalArgumentException e) {
      // OK
    }
    try {
      router.route().path("blah");
      fail();
    } catch (IllegalArgumentException e) {
      // OK
    }
  }
  
  @Test
  public void testRouteGetPath() throws Exception {
    assertEquals("/foo", router.route("/foo").getPath());
    assertEquals("/foo/:id", router.route("/foo/:id").getPath());
  }

  @Test
  public void testRouteGetPathWithParamsInHandler() throws Exception {
    router.route("/foo/:id").handler(rc -> {
      assertEquals("/foo/123", rc.normalisedPath());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/foo/123", 200, "OK");
  }

  @Test
  public void testRoutePathAndMethod() throws Exception {
    for (HttpMethod meth: METHODS) {
      testRoutePathAndMethod(meth, true);
    }
  }

  @Test
  public void testRoutePathAndMethodBegin() throws Exception {
    for (HttpMethod meth: METHODS) {
      testRoutePathAndMethod(meth, false);
    }
  }

  private void testRoutePathAndMethod(HttpMethod method, boolean exact) throws Exception {
    String path = "/blah";
    router.clear();
    router.route(method, exact ? path : path + "*").handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    if (exact) {
      testPathExact(method, path);
    } else {
      testPathBegin(method, path);
    }
    for (HttpMethod meth: METHODS) {
      if (meth != method) {
        testRequest(meth, path, 404, "Not Found");
      }
    }
  }

  @Test
  public void testRoutePathOnly() throws Exception {
    String path1 = "/blah";
    router.route(path1).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    String path2 = "/quux";
    router.route(path2).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });

    testPathExact(path1);
    testPathExact(path2);
  }

  @Test
  public void testRoutePathOnlyBegin() throws Exception {
    String path1 = "/blah";
    router.route(path1 + "*").handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    String path2 = "/quux";
    router.route(path2 + "*").handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });

    testPathBegin(path1);
    testPathBegin(path2);
  }
  
  @Test
  public void testRoutePathWithTrailingSlashOnlyBegin() throws Exception {
    String path = "/some/path/";
    router.route(path + "*").handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathBegin(path);
  }

  @Test
  public void testRoutePathBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathExact(path);
  }

  @Test
  public void testRoutePathBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathBegin(path);
  }

  @Test
  public void testRoutePathAndMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathExact(HttpMethod.GET, path);
    testRequest(HttpMethod.POST, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMethodBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").method(HttpMethod.GET).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathBegin(HttpMethod.GET, path);
    testRequest(HttpMethod.POST, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMultipleMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).method(HttpMethod.POST).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathExact(HttpMethod.GET, path);
    testPathExact(HttpMethod.POST, path);
    testRequest(HttpMethod.PUT, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMultipleMethodBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").method(HttpMethod.GET).method(HttpMethod.POST).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPathBegin(HttpMethod.GET, path);
    testPathBegin(HttpMethod.POST, path);
    testRequest(HttpMethod.PUT, path, 404, "Not Found");
  }

  private void testPathBegin(String path) throws Exception {
    for (HttpMethod meth: METHODS) {
      testPathBegin(meth, path);
    }
  }

  private void testPathExact(String path) throws Exception {
    for (HttpMethod meth: METHODS) {
      testPathExact(meth, path);
    }
  }

  private void testPathBegin(HttpMethod method, String path) throws Exception {
    testRequest(method, path, 200, path);
    testRequest(method, path + "wibble", 200, path + "wibble");
    if (path.endsWith("/")) {
      testRequest(method, path.substring(0, path.length() - 1) + "wibble", 404, "Not Found");
      testRequest(method, path.substring(0, path.length() - 1) + "/wibble", 200, path.substring(0, path.length() - 1) + "/wibble");
    } else {
      testRequest(method, path + "/wibble", 200, path + "/wibble");
      testRequest(method, path + "/wibble/floob", 200, path + "/wibble/floob");
      testRequest(method, path.substring(0, path.length() - 1), 404, "Not Found");
    }
    testRequest(method, "/", 404, "Not Found");
    testRequest(method, "/" + UUID.randomUUID().toString(), 404, "Not Found");
  }

  private void testPathExact(HttpMethod method, String path) throws Exception {
    testRequest(method, path, 200, path);
    testRequest(method, path + "wibble", 404, "Not Found");
    testRequest(method, path + "/wibble", 404, "Not Found");
    testRequest(method, path + "/wibble/floob", 404, "Not Found");
    testRequest(method, path.substring(0, path.length() - 1), 404, "Not Found");
    testRequest(method, "/", 404, "Not Found");
    testRequest(method, "/" + UUID.randomUUID().toString(), 404, "Not Found");
  }

  @Test
  public void testRouteNoPath() throws Exception {
    router.route().handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    for (HttpMethod meth: METHODS) {
      testNoPath(meth);
    }
  }

  @Test
  public void testRouteNoPath2() throws Exception {
    router.route().handler(rc -> {
      rc.response().setStatusMessage(rc.request().path());
      rc.next();
    });
    router.route().handler(rc -> {
      rc.response().setStatusCode(200).end();
    });
    for (HttpMethod meth: METHODS) {
      testNoPath(meth);
    }
  }

  @Test
  public void testRouteNoPathWithMethod() throws Exception {
    for (HttpMethod meth: METHODS) {
      testRouteNoPathWithMethod(meth);
    }
  }

  private void testRouteNoPathWithMethod(HttpMethod meth) throws Exception {
    router.clear();
    router.route().method(meth).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testNoPath(meth);
    for (HttpMethod m: METHODS) {
      if (m != meth) {
        testRequest(m, "/whatever", 404, "Not Found");
      }
    }
  }

  private void testNoPath(HttpMethod method) throws Exception {
    testRequest(method, "/", 200, "/");
    testRequest(method, "/wibble", 200, "/wibble");
    String rand = "/" + UUID.randomUUID().toString();
    testRequest(method, rand, 200, rand);
  }

  @Test
  public void testChaining() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      rc.next();
    });
    router.route(path).handler(rc -> {
      rc.response().write("oranges");
      rc.next();
    });
    router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
  }

  @Test
  public void testAsyncChaining() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      vertx.runOnContext(v -> rc.next());
    });
    router.route(path).handler(rc -> {
      rc.response().write("oranges");
      vertx.runOnContext(v -> rc.next());
    });
    router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
  }

  @Test
  public void testChainingWithTimers() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      vertx.setTimer(1, v -> rc.next());
    });
    router.route(path).handler(rc -> {
      rc.response().write("oranges");
      vertx.setTimer(1, v -> rc.next());
    });
    router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
  }

  @Test
  public void testOrdering() throws Exception {
    String path = "/blah";
    router.route(path).order(1).handler(rc -> {
      rc.response().write("apples");
      rc.next();
    });
    router.route(path).order(2).handler(rc -> {
      rc.response().write("oranges");
      rc.response().end();
    });
    router.route(path).order(0).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("bananas");
      rc.next();
    });
    testRequest(HttpMethod.GET, path, 200, "OK", "bananasapplesoranges");
  }

  @Test
  public void testLast() throws Exception {
    String path = "/blah";
    Route route = router.route(path);
    router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("oranges");
      rc.next();
    });
    router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.next();
    });
    route.last();
    route.handler(rc -> {
      rc.response().write("apples");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, path, 200, "OK", "orangesbananasapples");
  }

  @Test
  public void testDisableEnable() throws Exception {
    String path = "/blah";
    Route route1 = router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      rc.next();
    });;
    Route route2 = router.route(path).handler(rc -> {
      rc.response().write("oranges");
      rc.next();
    });
    Route route3 = router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });

    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
    route2.disable();
    testRequest(HttpMethod.GET, path, 200, "OK", "applesbananas");
    route1.disable();
    route3.disable();
    testRequest(HttpMethod.GET, path, 404, "Not Found");
    route3.enable();
    route1.enable();
    testRequest(HttpMethod.GET, path, 200, "OK", "applesbananas");
    route2.enable();
    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
  }

  @Test
  public void testRemove() throws Exception {
    String path = "/blah";
    Route route1 = router.route(path).handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      rc.next();
    });;
    Route route2 = router.route(path).handler(rc -> {
      rc.response().write("oranges");
      rc.next();
    });
    Route route3 = router.route(path).handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });

    testRequest(HttpMethod.GET, path, 200, "OK", "applesorangesbananas");
    route2.remove();
    testRequest(HttpMethod.GET, path, 200, "OK", "applesbananas");
    route1.remove();
    route3.remove();
    testRequest(HttpMethod.GET, path, 404, "Not Found");
  }

  @Test
  public void testClear() throws Exception {
    router.route().handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("apples");
      rc.next();
    });
    router.route().handler(rc -> {
      rc.response().write("bananas");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/whatever", 200, "OK", "applesbananas");
    router.clear();
    router.route().handler(rc -> {
      rc.response().setChunked(true);
      rc.response().write("grapes");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/whatever", 200, "OK", "grapes");
  }

  @Test
  public void testChangeOrderAfterActive1() throws Exception {
    String path = "/blah";
    Route route = router.route(path).handler(rc -> {
      rc.response().write("apples");
      rc.next();
    });
    try {
      route.order(23);
      fail();
    } catch (IllegalStateException e) {
      // OK
    }
  }

  @Test
  public void testChangeOrderAfterActive2() throws Exception {
    String path = "/blah";
    Route route = router.route(path).failureHandler(rc -> {
      rc.response().write("apples");
      rc.next();
    });
    try {
      route.order(23);
      fail();
    } catch (IllegalStateException e) {
      // OK
    }
  }

  @Test
  public void testNextAfterResponseEnded() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.response().end();
      rc.next();  // Call next
    });
    router.route(path).handler(rc -> {
      assertTrue(rc.response().ended());
    });
    testRequest(HttpMethod.GET, path, 200, "OK");
  }

  @Test
  public void testFailureHandler1() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    }).failureHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 555, "oh dear");
  }

  @Test
  public void testFailureinHandlingFailure() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    }).failureHandler(frc -> {
      throw new RuntimeException("super ouch!");
    });
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testSetExceptionHandler() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    CountDownLatch latch = new CountDownLatch(1);
    router.exceptionHandler(t -> {
      assertEquals("ouch!", t.getMessage());
      latch.countDown();
    });
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
    awaitLatch(latch);
  }

  @Test
  public void testFailureHandler1CallFail() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.fail(400);
    }).failureHandler(frc -> {
      assertEquals(400, frc.statusCode());
      frc.response().setStatusCode(400).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 400, "oh dear");
  }

  @Test
  public void testFailureHandler2() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    router.route("/bl*").failureHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 555, "oh dear");
  }

  @Test
  public void testFailureHandler2CallFail() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.fail(400);
    });
    router.route("/bl*").failureHandler(frc -> {
      assertEquals(400, frc.statusCode());
      frc.response().setStatusCode(400).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 400, "oh dear");
  }

  @Test
  public void testDefaultFailureHandler() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    // Default failure response
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testDefaultFailureHandlerCallFail() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      rc.fail(400);
    });
    // Default failure response
    testRequest(HttpMethod.GET, path, 400, "Bad Request");
  }

  @Test
  public void testFailureHandlerNoMatch() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    router.route("/other").failureHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    // Default failure response
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testPattern1() throws Exception {
    router.route("/:abc").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/tim", "tim");
  }

  @Test
  public void testPattern1WithMethod() throws Exception {
    router.route(HttpMethod.GET, "/:abc").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/tim", "tim");
    testRequest(HttpMethod.POST, "/tim", 404, "Not Found");
  }

  @Test
  public void testPattern1WithBuilder() throws Exception {
    router.route().path("/:abc").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/tim", "tim");
  }

  @Test
  public void testPattern2() throws Exception {
    router.route("/blah/:abc").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/blah/tim", "tim");
  }

  @Test
  public void testPattern3() throws Exception {
    router.route("/blah/:abc/blah").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/blah/tim/blah", "tim");
  }

  @Test
  public void testPattern4() throws Exception {
    router.route("/blah/:abc/foo").handler(rc -> {
      rc.response().setStatusMessage(rc.request().params().get("abc")).end();
    });
    testPattern("/blah/tim/foo", "tim");
  }

  @Test
  public void testPattern5() throws Exception {
    router.route("/blah/:abc/:def/:ghi").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("abc") + params.get("def") + params.get("ghi")).end();
    });
    testPattern("/blah/tim/julien/nick", "timjuliennick");
  }

  @Test
  public void testPattern6() throws Exception {
    router.route("/blah/:abc/:def/:ghi/blah").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("abc") + params.get("def") + params.get("ghi")).end();
    });
    testPattern("/blah/tim/julien/nick/blah", "timjuliennick");
  }

  @Test
  public void testPattern7() throws Exception {
    router.route("/blah/:abc/quux/:def/eep/:ghi").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("abc") + params.get("def") + params.get("ghi")).end();
    });
    testPattern("/blah/tim/quux/julien/eep/nick", "timjuliennick");
  }

  private void testPattern(String pathRoot, String expected) throws Exception {
    testRequest(HttpMethod.GET, pathRoot, 200, expected);
    testRequest(HttpMethod.GET, pathRoot + "/", 404, "Not Found");
    testRequest(HttpMethod.GET, pathRoot + "/wibble", 404, "Not Found");
    testRequest(HttpMethod.GET, pathRoot + "/wibble/blibble", 404, "Not Found");
  }

  @Test
  public void testInvalidPattern() throws Exception {
    router.route("/blah/:!!!/").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("!!!")).end();
    });
    testRequest(HttpMethod.GET, "/blah/tim", 404, "Not Found"); // Because it won't match
  }

  @Test
  public void testInvalidPatternWithBuilder() throws Exception {
    router.route().path("/blah/:!!!/").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("!!!")).end();
    });
    testRequest(HttpMethod.GET, "/blah/tim", 404, "Not Found"); // Because it won't match
  }

  @Test
  public void testGroupMoreThanOne() throws Exception {
    try {
      router.route("/blah/:abc/:abc");
      fail();
    } catch (IllegalArgumentException e) {
      // OK
    }
  }

  @Test
  public void testRegex1() throws Exception {
    router.routeWithRegex("\\/([^\\/]+)\\/([^\\/]+)").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("param0") + params.get("param1")).end();
    });
    testPattern("/dog/cat", "dogcat");
  }

  @Test
  public void testRegex1WithBuilder() throws Exception {
    router.route().pathRegex("\\/([^\\/]+)\\/([^\\/]+)").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("param0") + params.get("param1")).end();
    });
    testPattern("/dog/cat", "dogcat");
  }

  @Test
  public void testRegex1WithMethod() throws Exception {
    router.routeWithRegex(HttpMethod.GET, "\\/([^\\/]+)\\/([^\\/]+)").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("param0") + params.get("param1")).end();
    });
    testPattern("/dog/cat", "dogcat");
    testRequest(HttpMethod.POST, "/dog/cat", 404, "Not Found");
  }

  @Test
  public void testRegex2() throws Exception {
    router.routeWithRegex("\\/([^\\/]+)\\/([^\\/]+)/blah").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("param0") + params.get("param1")).end();
    });
    testPattern("/dog/cat/blah", "dogcat");
  }

  @Test
  public void testRegex3() throws Exception {
    router.routeWithRegex(".*foo.txt").handler(rc -> {
      rc.response().setStatusMessage("ok").end();
    });
    testPattern("/dog/cat/foo.txt", "ok");
    testRequest(HttpMethod.POST, "/dog/cat/foo.bar", 404, "Not Found");

  }

  @Test
  public void testConsumes() throws Exception {
    router.route().consumes("text/html").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "something/html", 404, "Not Found");
  }

  @Test
  public void testConsumesMultiple() throws Exception {
    router.route().consumes("text/html").consumes("application/json").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "something/html", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/blah", 404, "Not Found");
  }

  @Test
  public void testConsumesMissingSlash() throws Exception {
    // will assume "*/json"
    router.route().consumes("json").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
  }

  @Test
  public void testConsumesSubtypeWildcard() throws Exception {
    router.route().consumes("text/*").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 404, "Not Found");
  }

  @Test
  public void testConsumesTopLevelTypeWildcard() throws Exception {
    router.route().consumes("*/json").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/html", 404, "Not Found");
  }

  @Test
  public void testConsumesAll1() throws Exception {
    router.route().consumes("*/*").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html; someparam=12", 200, "OK");
    testRequest(HttpMethod.GET, "/foo", 200, "OK");
  }

  @Test
  public void testConsumesAll2() throws Exception {
    router.route().consumes("*").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html; someparam=12", 200, "OK");
    testRequest(HttpMethod.GET, "/foo", 200, "OK");
  }

  @Test
  public void testConsumesCTParamsIgnored() throws Exception {
    router.route().consumes("text/html").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html; someparam=12", 200, "OK");
  }

  @Test
  public void testConsumesNoContentType() throws Exception {
    router.route().consumes("text/html").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/foo", 404, "Not Found");
  }

  @Test
  public void testProduces() throws Exception {
    router.route().produces("text/html").handler(rc -> rc.response().end());
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "something/html", 404, "Not Found");
  }

  @Test
  public void testProducesMultiple() throws Exception {
    router.route().produces("text/html").produces("application/json").handler(rc -> rc.response().end());
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "application/json", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "something/html", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "application/blah", 404, "Not Found");
  }

  @Test
  public void testProducesMissingSlash() throws Exception {
    // will assume "*/json"
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "json", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text", 404, "Not Found");
  }

  @Test
  public void testProducesSubtypeWildcard() throws Exception {
    router.route().produces("text/html").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/*", 200, "text/html");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "application/*", 404, "Not Found");
  }

  @Test
  public void testProducesTopLevelTypeWildcard() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*/json", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*/html", 404, "Not Found");
  }

  @Test
  public void testProducesAll1() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*/*", 200, "application/json");
  }

  @Test
  public void testProducesAll2() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple1() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json,text/plain", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple2() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/*,text/plain", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple3() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json,text/plain", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple4() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json", 200, "text/plain");
  }

  @Test
  public void testAcceptsMultiple5() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json;q=0.9", 200, "text/plain");
  }

  @Test
  public void testAcceptsMultiple6() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple7() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=1.0", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple8() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=0.8", 200, "text/plain");
  }

  @Test
  public void testGetPutContextData() throws Exception {
    SomeObject obj = new SomeObject();
    router.route().handler(ctx -> {
      ctx.put("foo", "bar");
      ctx.put("blah", obj);
      ctx.next();
    });
    router.route().handler(ctx -> {
      assertEquals("bar", ctx.get("foo"));
      assertEquals(obj, ctx.get("blah"));
      ctx.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  class SomeObject {
  }

  @Test
  public void testGetRoutes() throws Exception {
    router.route("/abc").handler(rc -> {});
    router.route("/abc/def").handler(rc -> {});
    router.route("/xyz").handler(rc -> {});
    List<Route> routes = router.getRoutes();
    assertEquals(3, routes.size());
  }

  // Test that adding headersEndhandlers doesn't overwrite other ones
  @Test
  public void testHeadersEndHandler() throws Exception {
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(f -> {
        rc.response().putHeader("header1", "foo");
        f.complete();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(f -> {
        rc.response().putHeader("header2", "foo");
        f.complete();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(f -> {
        rc.response().putHeader("header3", "foo");
        f.complete();
      });
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      MultiMap headers = resp.headers();
      assertTrue(headers.contains("header1"));
      assertTrue(headers.contains("header2"));
      assertTrue(headers.contains("header3"));
    }, 200, "OK", null);
  }

  @Test
  public void testHeadersEndHandlerRemoveHandler() throws Exception {
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(f -> {
        rc.response().putHeader("header1", "foo");
        f.complete();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      Handler<Future> handler = fut -> {
        rc.response().putHeader("header2", "foo");
        fut.complete();
      };
      int handlerID = rc.addHeadersEndHandler(handler);
      vertx.setTimer(1, tid -> {
        assertTrue(rc.removeHeadersEndHandler(handlerID));
        assertFalse(rc.removeHeadersEndHandler(handlerID + 1));
        rc.response().end();
      });
    });

    testRequest(HttpMethod.GET, "/", null, resp -> {
      MultiMap headers = resp.headers();
      assertTrue(headers.contains("header1"));
    }, 200, "OK", null);
  }

  // Test that adding bodyEndhandlers doesn't overwrite other ones
  @Test
  public void testBodyEndHandler() throws Exception {
    AtomicInteger cnt = new AtomicInteger();
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> {
        cnt.incrementAndGet();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> {
        cnt.incrementAndGet();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> {
        cnt.incrementAndGet();
      });
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    waitUntil(() -> cnt.get() == 3);
  }

  @Test
  public void testBodyEndHandlerRemoveHandler() throws Exception {
    AtomicInteger cnt = new AtomicInteger();
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> {
        cnt.incrementAndGet();
      });
      rc.next();
    });
    router.route().handler(rc -> {
      Handler<Void> handler = v -> {
        cnt.incrementAndGet();
      };
      int handlerID = rc.addBodyEndHandler(handler);
      vertx.setTimer(1, tid -> {
        assertTrue(rc.removeBodyEndHandler(handlerID));
        assertFalse(rc.removeBodyEndHandler(handlerID + 1));
        rc.response().end();
      });
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
    waitUntil(() -> cnt.get() == 1);
  }

  @Test
  public void testNoRoutes() throws Exception {
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
  }

  @Test
  public void testGet() throws Exception {
    router.get().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.GET, "/whatever", 200, "foo");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testGetWithPathExact() throws Exception {
    router.get("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.GET, "/somepath/", 200, "foo");
    testRequest(HttpMethod.GET, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testGetWithPathBegin() throws Exception {
    router.get("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.GET, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testGetWithRegex() throws Exception {
    router.getWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.GET, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPost() throws Exception {
    router.post().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.POST, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testPostWithPathExact() throws Exception {
    router.post("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.POST, "/somepath/", 200, "foo");
    testRequest(HttpMethod.POST, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPostWithPathBegin() throws Exception {
    router.post("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.POST, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.POST, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPostWithRegex() throws Exception {
    router.postWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.POST, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.POST, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPut() throws Exception {
    router.put().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.PUT, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testPutWithPathExact() throws Exception {
    router.put("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.PUT, "/somepath/", 200, "foo");
    testRequest(HttpMethod.PUT, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPutWithPathBegin() throws Exception {
    router.put("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.PUT, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.PUT, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testPutWithRegex() throws Exception {
    router.putWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.PUT, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.PUT, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testDelete() throws Exception {
    router.delete().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.DELETE, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testDeleteWithPathExact() throws Exception {
    router.delete("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.DELETE, "/somepath/", 200, "foo");
    testRequest(HttpMethod.DELETE, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testDeleteWithPathBegin() throws Exception {
    router.delete("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.DELETE, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testDeleteWithRegex() throws Exception {
    router.deleteWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.DELETE, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testOptions() throws Exception {
    router.options().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.OPTIONS, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testOptionsWithPathExact() throws Exception {
    router.options("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.OPTIONS, "/somepath/", 200, "foo");
    testRequest(HttpMethod.OPTIONS, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testOptionsWithPathBegin() throws Exception {
    router.options("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.OPTIONS, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testOptionsWithRegex() throws Exception {
    router.optionsWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.OPTIONS, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testHead() throws Exception {
    router.head().handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.HEAD, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
  }

  @Test
  public void testHeadWithPathExact() throws Exception {
    router.head("/somepath/").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.HEAD, "/somepath/", 200, "foo");
    testRequest(HttpMethod.HEAD, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testHeadWithPathBegin() throws Exception {
    router.head("/somepath/*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.HEAD, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testHeadWithRegex() throws Exception {
    router.headWithRegex("\\/somepath\\/.*").handler(rc -> {
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.HEAD, "/somepath/whatever", 200, "foo");
    testRequest(HttpMethod.HEAD, "/otherpath/whatever", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/somepath/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/somepath/whatever", 404, "Not Found");
  }

  @Test
  public void testRouteNormalised1() throws Exception {
    router.route("/foo").handler(rc -> {
      rc.response().setStatusMessage("socks").end();
    });
    testRequest(HttpMethod.GET, "/foo", 200, "socks");
    testRequest(HttpMethod.GET, "/foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo//", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/////", 200, "socks");
  }

  @Test
  public void testRouteNormalised2() throws Exception {
    router.route("/foo/").handler(rc -> {
      rc.response().setStatusMessage("socks").end();
    });
    testRequest(HttpMethod.GET, "/foo", 200, "socks");
    testRequest(HttpMethod.GET, "/foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo//", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/////", 200, "socks");
  }

  @Test
  public void testRouteNormalised3() throws Exception {
    router.route("/").handler(rc -> {
      rc.response().setStatusMessage("pants").end();
    });
    testRequest(HttpMethod.GET, "/", 200, "pants");
    testRequest(HttpMethod.GET, "//", 200, "pants");
    testRequest(HttpMethod.GET, "///", 200, "pants");
  }



}
