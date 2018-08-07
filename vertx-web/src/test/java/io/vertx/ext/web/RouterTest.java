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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    for (HttpMethod meth : METHODS) {
      testRoutePathAndMethod(meth, true);
    }
  }

  @Test
  public void testRoutePathAndMethodBegin() throws Exception {
    for (HttpMethod meth : METHODS) {
      testRoutePathAndMethod(meth, false);
    }
  }

  private void testRoutePathAndMethod(HttpMethod method, boolean exact) throws Exception {
    String path = "/blah";
    router.clear();
    router.route(method, exact ? path : path + "*").handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    if (exact) {
      testPathExact(method, path);
    } else {
      testPathBegin(method, path);
    }
    for (HttpMethod meth : METHODS) {
      if (meth != method) {
        testRequest(meth, path, 404, "Not Found");
      }
    }
  }

  @Test
  public void testRoutePathOnly() throws Exception {
    String path1 = "/blah";
    router.route(path1).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    String path2 = "/quux";
    router.route(path2).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());

    testPathExact(path1);
    testPathExact(path2);
  }

  @Test
  public void testRoutePathOnlyBegin() throws Exception {
    String path1 = "/blah";
    router.route(path1 + "*").handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    String path2 = "/quux";
    router.route(path2 + "*").handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());

    testPathBegin(path1);
    testPathBegin(path2);
  }

  @Test
  public void testRoutePathWithTrailingSlashOnlyBegin() throws Exception {
    String path = "/some/path/";
    router.route(path + "*").handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathBegin(path);
  }

  @Test
  public void testRoutePathBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathExact(path);
  }

  @Test
  public void testRoutePathBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathBegin(path);
  }

  @Test
  public void testRoutePathAndMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathExact(HttpMethod.GET, path);
    testRequest(HttpMethod.POST, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMethodBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").method(HttpMethod.GET).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathBegin(HttpMethod.GET, path);
    testRequest(HttpMethod.POST, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMultipleMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).method(HttpMethod.POST).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathExact(HttpMethod.GET, path);
    testPathExact(HttpMethod.POST, path);
    testRequest(HttpMethod.PUT, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMultipleMethodBuilderBegin() throws Exception {
    String path = "/blah";
    router.route().path(path + "*").method(HttpMethod.GET).method(HttpMethod.POST).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testPathBegin(HttpMethod.GET, path);
    testPathBegin(HttpMethod.POST, path);
    testRequest(HttpMethod.PUT, path, 404, "Not Found");
  }

  private void testPathBegin(String path) throws Exception {
    for (HttpMethod meth : METHODS) {
      testPathBegin(meth, path);
    }
  }

  private void testPathExact(String path) throws Exception {
    for (HttpMethod meth : METHODS) {
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
    router.route().handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    for (HttpMethod meth : METHODS) {
      testNoPath(meth);
    }
  }

  @Test
  public void testRouteNoPath2() throws Exception {
    router.route().handler(rc -> {
      rc.response().setStatusMessage(rc.request().path());
      rc.next();
    });
    router.route().handler(rc -> rc.response().setStatusCode(200).end());
    for (HttpMethod meth : METHODS) {
      testNoPath(meth);
    }
  }

  @Test
  public void testRouteNoPathWithMethod() throws Exception {
    for (HttpMethod meth : METHODS) {
      testRouteNoPathWithMethod(meth);
    }
  }

  private void testRouteNoPathWithMethod(HttpMethod meth) throws Exception {
    router.clear();
    router.route().method(meth).handler(rc -> rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end());
    testNoPath(meth);
    for (HttpMethod m : METHODS) {
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
    });
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
    });
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
    router.route(path).handler(rc -> assertTrue(rc.response().ended()));
    testRequest(HttpMethod.GET, path, 200, "OK");
  }

  @Test
  public void testFailureHandler1() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    }).failureHandler(frc -> frc.response().setStatusCode(555).setStatusMessage("oh dear").end());
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
  public void testFailureUsingInvalidCharsInStatus() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> rc.response().setStatusMessage("Hello\nWorld!").end());
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testFailureinHandlingFailureWithInvalidStatusMessage() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    }).failureHandler(frc -> frc.response().setStatusMessage("Hello\nWorld").end());
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
    router.route(path).handler(rc -> rc.fail(400)).failureHandler(frc -> {
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
    router.route("/bl*").failureHandler(frc -> frc.response().setStatusCode(555).setStatusMessage("oh dear").end());
    testRequest(HttpMethod.GET, path, 555, "oh dear");
  }

  @Test
  public void testFailureHandler2CallFail() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> rc.fail(400));
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
    router.route(path).handler(rc -> rc.fail(400));
    // Default failure response
    testRequest(HttpMethod.GET, path, 400, "Bad Request");
  }

  @Test
  public void testFailureHandlerNoMatch() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    router.route("/other").failureHandler(frc -> frc.response().setStatusCode(555).setStatusMessage("oh dear").end());
    // Default failure response
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testFailureWithThrowable() throws Exception {
    String path = "/blah";
    Throwable failure = new Throwable();
    router.route(path).handler(rc -> rc.fail(failure)).failureHandler(frc -> {
      assertEquals(-1, frc.statusCode());
      assertSame(failure, frc.failure());
      frc.response().setStatusCode(500).setStatusMessage("Internal Server Error").end();
    });
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testFailureWithNullThrowable() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> rc.fail(null)).failureHandler(frc -> {
      assertEquals(-1, frc.statusCode());
      assertTrue(frc.failure() instanceof NullPointerException);
      frc.response().setStatusCode(500).setStatusMessage("Internal Server Error").end();
    });
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testPattern1() throws Exception {
    router.route("/:abc").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
    testPattern("/tim", "tim");
  }

  @Test
  public void testParamEscape() throws Exception {
    router.route("/demo/:abc").handler(rc -> {
      assertEquals("Hello World!", rc.request().params().get("abc"));
      rc.response().end(rc.request().params().get("abc"));
    });
    testRequest(HttpMethod.GET, "/demo/Hello%20World!", 200, "OK", "Hello World!");
  }

  @Test
  public void testParamEscape2() throws Exception {
    router.route("/demo/:abc").handler(rc -> {
      assertEquals("Hello/World!", rc.request().params().get("abc"));
      rc.response().end(rc.request().params().get("abc"));
    });
    testRequest(HttpMethod.GET, "/demo/Hello%2FWorld!", 200, "OK", "Hello/World!");
  }

  @Test
  public void testParamEscape3() throws Exception {
    router.route("/demo/:abc").handler(rc -> {
      assertEquals("http://www.google.com", rc.request().params().get("abc"));
      rc.response().end(rc.request().params().get("abc"));
    });
    testRequest(HttpMethod.GET, "/demo/http%3A%2F%2Fwww.google.com", 200, "OK", "http://www.google.com");
  }

  @Test
  public void testParamEscape4() throws Exception {
    router.route("/:var").handler(rc -> {
      assertEquals("/ping", rc.request().params().get("var"));
      rc.response().end(rc.request().params().get("var"));
    });
    testRequest(HttpMethod.GET, "/%2Fping", 200, "OK", "/ping");
  }

  @Test
  public void testPattern1WithMethod() throws Exception {
    router.route(HttpMethod.GET, "/:abc").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
    testPattern("/tim", "tim");
    testRequest(HttpMethod.POST, "/tim", 404, "Not Found");
  }

  @Test
  public void testPattern1WithBuilder() throws Exception {
    router.route().path("/:abc").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
    testPattern("/tim", "tim");
  }

  @Test
  public void testPattern2() throws Exception {
    router.route("/blah/:abc").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
    testPattern("/blah/tim", "tim");
  }

  @Test
  public void testPattern3() throws Exception {
    router.route("/blah/:abc/blah").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
    testPattern("/blah/tim/blah", "tim");
  }

  @Test
  public void testPattern4() throws Exception {
    router.route("/blah/:abc/foo").handler(rc -> rc.response().setStatusMessage(rc.request().params().get("abc")).end());
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

  @Test
  public void testPercentEncoding() throws Exception {
    router.route("/blah/:percenttext").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("percenttext")).end();
    });
    testPattern("/blah/abc%25xyz", "abc%xyz");
  }

  @Test
  public void testPathParamsAreFulfilled() throws Exception {
    router.route("/blah/:abc/quux/:def/eep/:ghi").handler(rc -> {
      Map<String, String> params = rc.pathParams();
      rc.response().setStatusMessage(params.get("abc") + params.get("def") + params.get("ghi")).end();
    });
    testPattern("/blah/tim/quux/julien/eep/nick", "timjuliennick");
  }

  @Test
  public void testPathParamsDoesNotOverrideQueryParam() throws Exception {
    final String paramName = "param";
    final String pathParamValue = "pathParamValue";
    final String queryParamValue1 = "queryParamValue1";
    final String queryParamValue2 = "queryParamValue2";
    final String sep = ",";
    router.route("/blah/:" + paramName + "/test").handler(rc -> {
      Map<String, String> params = rc.pathParams();
      MultiMap queryParams = rc.request().params();
      List<String> values = queryParams.getAll(paramName);
      String qValue = values.stream().collect(Collectors.joining(sep));
      rc.response().setStatusMessage(params.get(paramName) + "|" + qValue).end();
    });
    testRequest(HttpMethod.GET,
      "/blah/" + pathParamValue + "/test?" + paramName + "=" + queryParamValue1 + "&" + paramName + "=" + queryParamValue2,
      200,
      pathParamValue + "|" + queryParamValue1 + sep + queryParamValue2);
  }

  @Test
  public void testCorrectQueryParamatersEncapsulation() throws Exception {
    final String pathParameterName = "pathParameter";
    final String pathParamValue = "awesomePath";
    final String qName = "q";
    final String qValue1 = "a";
    final String qValue2 = "b";
    final String sName = "s";
    final String sValue = "sample_value";
    final String sep = ",";
    router.route("/blah/:" + pathParameterName + "/test").handler(rc -> {
      MultiMap params = rc.queryParams();
      assertFalse(params.contains(pathParameterName));
      String qExpected = String.join(",", params.getAll("q"));
      String statusMessage = String.join("/", qExpected, params.get("s"));
      rc.response().setStatusMessage(statusMessage).end();
    });
    testRequest(HttpMethod.GET,
      "/blah/" + pathParamValue + "/test?" + qName + "=" + qValue1 + "," + qValue2 + "&" + sName + "=" + sValue, 200,
      qValue1 + "," + qValue2 + "/" + sValue);
  }

  @Test
  public void testPathParamsWithReroute() throws Exception {
    String paramName = "param";
    String firstParamValue = "fpv";
    String secondParamValue = "secondParamValue";
    router.route("/first/:" + paramName + "/route").handler(rc -> {
      assertEquals(firstParamValue, rc.pathParam(paramName));
      rc.reroute(HttpMethod.GET, "/second/" + secondParamValue + "/route");
    });
    router.route("/second/:" + paramName + "/route").handler(rc -> rc.response().setStatusMessage(rc.pathParam(paramName)).end());
    testRequest(HttpMethod.GET, "/first/" + firstParamValue + "/route", 200, secondParamValue);
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
    router.routeWithRegex(".*foo.txt").handler(rc -> rc.response().setStatusMessage("ok").end());
    testPattern("/dog/cat/foo.txt", "ok");
    testRequest(HttpMethod.POST, "/dog/cat/foo.bar", 404, "Not Found");
  }

  @Test
  public void testRegexWithNamedParams() throws Exception {
    router.routeWithRegex(HttpMethod.GET, "\\/(?<name>[^\\/]+)\\/(?<surname>[^\\/]+)").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("name") + params.get("surname")).end();
    });
    testPattern("/joe/doe", "joedoe");
  }

  @Test
  public void testRegexWithNamedParamsKeepsIndexedParams() throws Exception {
    router.routeWithRegex(HttpMethod.GET, "\\/(?<name>[^\\/]+)\\/(?<surname>[^\\/]+)").handler(rc -> {
      MultiMap params = rc.request().params();
      rc.response().setStatusMessage(params.get("param0") + params.get("param1")).end();
    });
    testPattern("/joe/doe", "joedoe");
  }

  @Test
  public void testConsumes() throws Exception {
    router.route().consumes("text/html").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/json", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "something/html", 404, "Not Found");
  }

  @Test
  public void testConsumesWithParameterKey() throws Exception {
    router.route().consumes("text/html;boo").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=ya;itWorks=4real", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo;itWorks", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=ya", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
  }

  @Test
  public void testConsumesWithParameter() throws Exception {
    router.route().consumes("text/html;boo=ya").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=ya", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
  }

  @Test
  public void testConsumesWithQuotedParameterWithComma() throws Exception {
    router.route().consumes("text/html;boo=\"yeah,right\"").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=\"yeah,right\";itWorks=4real", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=\"yeah,right\"", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=\"yeah,right;itWorks=4real\"", 404, "Not Found");
    // this might look wrong but since there is only 1 entry per content-type, the comma has no semantic meaning
    // therefore it is ignored
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=yeah,right", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
  }

  @Test
  public void testConsumesWithQuotedParameterWithQuotes() throws Exception {
    router.route().consumes("text/html;boo=\"yeah\\\"right\"").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=\"yeah\\\"right\"", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=\"yeah,right\"", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=yeah,right", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
  }

  @Test
  public void testConsumesWithQParameterIgnored() throws Exception {
    router.route().consumes("text/html;q").consumes("text/html;q=0.1").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=yeah,right", 200, "OK");
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
  public void testConsumesVariableParameters() throws Exception {
    router.route().consumes("text/html;boo").consumes("text/html;works").handler(rc -> rc.response().end());
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;works", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo;works", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;boo=done;it=works", 200, "OK");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/html;yes=no;right", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/book;boo", 404, "Not Found");
    testRequestWithContentType(HttpMethod.GET, "/foo", "text/book;works=aright", 404, "Not Found");
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
    testRequest(HttpMethod.GET, "/foo", 200, "OK");
  }

  @Test
  public void testProducesWithParameterKey() throws Exception {
    router.route().produces("text/html;boo").handler(rc -> rc.response().end());
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;boo;itWorks", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;boo=ya", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;boo", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*/*", 200, "OK");
  }

  @Test
  public void testProducesWithParameter() throws Exception {
    router.route().produces("text/html;boo=ya").handler(rc -> rc.response().end());
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;boo=ya", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;boo", 404, "Not Found");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html", 404, "Not Found");
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
  public void testProducesWithQParameterIgnored() throws Exception {
    router.route().produces("text/html;q").produces("text/html;q=0.1").handler(rc -> rc.response().end());
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;a", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;q=2", 200, "OK");
    testRequest(HttpMethod.GET, "/foo", 200, "OK");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "*/*", 200, "OK");
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
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json;b,text/plain", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple2() throws Exception {
    router.route().produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/*,text/plain", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html;a,application/*,text/plain", 200, "application/json");
  }

  @Test
  public void testAcceptsWithSpaces() throws Exception {
    router.route("/json").produces("application/json").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/json", "    text/html    , application/*    , text/plain; q= 0.9  ", 200, "application/json");
    router.route("/html").produces("text/html").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/html", "    text/html    , application/*    , text/plain; q= 0.9  ", 200, "text/html");
    router.route("/text").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/text", "    text/html    , application/*    , text/plain; q= 0.9  ", 200, "text/plain");
  }

  @Test
  public void testAcceptsMultiple3() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json,text/plain", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json;a,text/plain", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json,text/plain;a", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,application/json;c,text/plain;a", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple4() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;a,application/json", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json;a", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple5() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json;q=0.9", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json;q=0.9;a", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain,application/json;a;q=0.9", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;a,application/json;q=0.9", 200, "text/plain");
  }

  @Test
  public void testAcceptsMultiple6() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;a", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9;a,application/json", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple7() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=1.0", 200, "application/json");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9;a,application/json;q=1.0", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple8() throws Exception {
    router.route().produces("application/json").produces("text/html").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=1.0", 200, "text/html");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9;b,application/json;q=1.0", 200, "text/html");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9;b,application/json;q=1.0;a", 200, "application/json");
  }

  @Test
  public void testAcceptsMultiple9() throws Exception {
    router.route().produces("application/json").produces("text/plain").handler(rc -> {
      rc.response().setStatusMessage(rc.getAcceptableContentType());
      rc.response().end();
    });
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=0.8", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9;d,application/json;q=0.8", 200, "text/plain");
    testRequestWithAccepts(HttpMethod.GET, "/foo", "text/html,text/plain;q=0.9,application/json;q=0.8;s", 200, "text/plain");
  }

  @Test
  public void testAcceptsMultipleWithParams() throws Exception {
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
    router.route("/abc").handler(rc -> {
    });
    router.route("/abc/def").handler(rc -> {
    });
    router.route("/xyz").handler(rc -> {
    });
    List<Route> routes = router.getRoutes();
    assertEquals(3, routes.size());
  }

  // Test that adding headersEndhandlers doesn't overwrite other ones
  @Test
  public void testHeadersEndHandler() throws Exception {
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(v -> rc.response().putHeader("header1", "foo"));
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(v -> rc.response().putHeader("header2", "foo"));
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(v -> rc.response().putHeader("header3", "foo"));
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
  public void testHeadersEndHandlerCalledBackwards() throws Exception {

    final AtomicInteger cnt = new AtomicInteger(0);

    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addHeadersEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.next();
    });
    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addHeadersEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.next();
    });
    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addHeadersEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testHeadersEndHandlerCalledBackwards2() throws Exception {

    final AtomicInteger cnt = new AtomicInteger(0);

    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addBodyEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.next();
    });
    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addBodyEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.next();
    });
    router.route().handler(rc -> {
      final int val = cnt.incrementAndGet();
      rc.addBodyEndHandler(v -> assertEquals(val, cnt.getAndDecrement()));
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testHeadersEndHandlerRemoveHandler() throws Exception {
    router.route().handler(rc -> {
      rc.addHeadersEndHandler(v -> rc.response().putHeader("header1", "foo"));
      rc.next();
    });
    router.route().handler(rc -> {
      Handler<Void> handler = v -> rc.response().putHeader("header2", "foo");
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
      rc.addBodyEndHandler(v -> cnt.incrementAndGet());
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> cnt.incrementAndGet());
      rc.next();
    });
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> cnt.incrementAndGet());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    assertWaitUntil(() -> cnt.get() == 3);
  }

  @Test
  public void testBodyEndHandlerRemoveHandler() throws Exception {
    AtomicInteger cnt = new AtomicInteger();
    router.route().handler(rc -> {
      rc.addBodyEndHandler(v -> cnt.incrementAndGet());
      rc.next();
    });
    router.route().handler(rc -> {
      Handler<Void> handler = v -> cnt.incrementAndGet();
      int handlerID = rc.addBodyEndHandler(handler);
      vertx.setTimer(1, tid -> {
        assertTrue(rc.removeBodyEndHandler(handlerID));
        assertFalse(rc.removeBodyEndHandler(handlerID + 1));
        rc.response().end();
      });
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
    assertWaitUntil(() -> cnt.get() == 1);
  }

  @Test
  public void testNoRoutes() throws Exception {
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
  }

  @Test
  public void testGet() throws Exception {
    router.get().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.GET, "/whatever", 200, "foo");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testGetWithPathExact() throws Exception {
    router.get("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.get("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.getWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.post().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.POST, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testPostWithPathExact() throws Exception {
    router.post("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.post("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.postWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.put().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.PUT, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testPutWithPathExact() throws Exception {
    router.put("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.put("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.putWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.delete().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.DELETE, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testDeleteWithPathExact() throws Exception {
    router.delete("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.delete("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.deleteWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.options().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.OPTIONS, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.HEAD, "/whatever", 404, "Not Found");
  }

  @Test
  public void testOptionsWithPathExact() throws Exception {
    router.options("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.options("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.optionsWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.head().handler(rc -> rc.response().setStatusMessage("foo").end());
    testRequest(HttpMethod.HEAD, "/whatever", 200, "foo");
    testRequest(HttpMethod.GET, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.POST, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.PUT, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.OPTIONS, "/whatever", 404, "Not Found");
    testRequest(HttpMethod.DELETE, "/whatever", 404, "Not Found");
  }

  @Test
  public void testHeadWithPathExact() throws Exception {
    router.head("/somepath/").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.head("/somepath/*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.headWithRegex("\\/somepath\\/.*").handler(rc -> rc.response().setStatusMessage("foo").end());
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
    router.route("/foo").handler(rc -> rc.response().setStatusMessage("socks").end());
    testRequest(HttpMethod.GET, "/foo", 200, "socks");
    testRequest(HttpMethod.GET, "/foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo//", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/////", 200, "socks");
  }

  @Test
  public void testRouteNormalised2() throws Exception {
    router.route("/foo/").handler(rc -> rc.response().setStatusMessage("socks").end());
    testRequest(HttpMethod.GET, "/foo", 200, "socks");
    testRequest(HttpMethod.GET, "/foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/", 200, "socks");
    testRequest(HttpMethod.GET, "//foo//", 200, "socks");
    testRequest(HttpMethod.GET, "//foo/////", 200, "socks");
  }

  @Test
  public void testRouteNormalised3() throws Exception {
    router.route("/").handler(rc -> rc.response().setStatusMessage("pants").end());
    testRequest(HttpMethod.GET, "/", 200, "pants");
    testRequest(HttpMethod.GET, "//", 200, "pants");
    testRequest(HttpMethod.GET, "///", 200, "pants");
  }

  @Test
  public void testIssue170() throws Exception {
    try {
      router.route("").handler(rc -> rc.response().end());
    } catch (IllegalArgumentException e) {
      testComplete();
      return;
    }

    fail("Should fail");
  }

  @Test
  public void testIssue170b() throws Exception {
    router.route("/").handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testIssue176() throws Exception {
    router.route().order(0).handler(context -> {
      context.response().headers().add("X-Here-1", "1");
      context.next();
    });
    router.route().order(0).handler(context -> {
      context.response().headers().add("X-Here-2", "2");
      context.next();
    });
    router.route().handler(context -> {
      context.response().headers().add("X-Here-3", "3");
      context.response().end();
    });

    testRequest(HttpMethod.GET, "/", null, resp -> {
      MultiMap headers = resp.headers();
      assertTrue(headers.contains("X-Here-1"));
      assertTrue(headers.contains("X-Here-2"));
      assertTrue(headers.contains("X-Here-3"));
    }, 200, "OK", null);
  }

  @Test
  public void testLocaleWithCountry() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.acceptableLocales().size());
      assertEquals("da", rc.preferredLocale().language());
      assertEquals("DK", rc.preferredLocale().country());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", req -> req.putHeader("Accept-Language", "en-gb;q=0.8, en;q=0.7, da_DK;q=0.9"), 200, "OK", null);
    testRequest(HttpMethod.GET, "/foo", req -> req.putHeader("Accept-Language", "en-gb;q=0.8, en;q=0.7, da-DK;q=0.9"), 200, "OK", null);
  }

  @Test
  public void testLocaleSimple() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.acceptableLocales().size());
      assertEquals("da", rc.preferredLocale().language());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", req -> req.putHeader("Accept-Language", "da, en-gb;q=0.8, en;q=0.7"), 200, "OK", null);
  }

  @Test
  public void testLocaleWithoutQuality() throws Exception {
    router.route().handler(rc -> {
      assertEquals(1, rc.acceptableLocales().size());
      assertEquals("en", rc.preferredLocale().language());
      assertEquals("GB", rc.preferredLocale().country());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", req -> req.putHeader("Accept-Language", "en-gb"), 200, "OK", null);
  }

  @Test
  public void testLocaleSameQuality() throws Exception {
    router.route().handler(rc -> {
      assertEquals(2, rc.acceptableLocales().size());
      assertEquals("pt", rc.preferredLocale().language());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", req -> req.putHeader("Accept-Language", "pt;q=0.9, en-gb;q=0.9"), 200, "OK", null);
  }

  @Test
  public void testLocaleNoHeaderFromClient() throws Exception {
    router.route().handler(rc -> {
      assertEquals(0, rc.acceptableLocales().size());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", 200, "OK");
  }

  @Test
  public void testUnderscoreOnRoutePath() throws Exception {
    router.route("/:account_id").handler(rc -> {
      assertEquals("foo", rc.request().params().get("account_id"));
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/foo", 200, "OK");
  }

  @Test
  public void testBadURL() throws Exception {
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/%7B%channel%%7D", 200, "OK");
  }

  @Test
  public void testDuplicateParams() throws Exception {
    router.route("/test/:p").handler(RoutingContext::next);
    router.route("/test/:p").handler(RoutingContext::next);
    router.route("/test/:p").handler(routingContext -> {
      assertEquals(1, routingContext.request().params().getAll("p").size());
      assertEquals("abc", routingContext.request().getParam("p"));
      routingContext.response().end();
    });
    testRequest(HttpMethod.GET, "/test/abc", 200, "OK");
  }

  @Test
  public void testDuplicateParams2() throws Exception {
    router.route("/test/:p").handler(RoutingContext::next);
    router.route("/test/:p").handler(ctx -> ctx.reroute("/done/abc/cde"));

    router.route("/done/:a/:p").handler(routingContext -> {
      assertEquals(1, routingContext.request().params().getAll("p").size());
      assertEquals("cde", routingContext.request().getParam("p"));
      routingContext.response().end();
    });
    testRequest(HttpMethod.GET, "/test/abc", 200, "OK");
  }

  @Test
  public void testSubRouterNPE() throws Exception {
    Router subRouter = Router.router(vertx);
    router.mountSubRouter("/", subRouter);

    testRequest(HttpMethod.GET, "foo", 404, "Not Found");
  }

  @Test
  public void testParamFirst() throws Exception {
    router.route("/:p/*").handler(context -> {
      context.response().headers().add("X-Here-1", "1");
      context.next();
    });
    router.route("/:p/test").handler(context -> {
      context.response().headers().add("X-Here-2", "2");
      context.response().end();
    });

    testRequest(HttpMethod.GET, "/abc/test", null, resp -> {
      MultiMap headers = resp.headers();
      assertTrue(headers.contains("X-Here-1"));
      assertTrue(headers.contains("X-Here-2"));
    }, 200, "OK", null);
  }

  @Test
  public void testGetWithPlusPath2() throws Exception {
    router.get("/:param1").useNormalisedPath(false).handler(rc -> {
      assertEquals("/some+path", rc.normalisedPath());
      assertEquals("some+path", rc.pathParam("param1"));
      assertEquals("some query", rc.request().getParam("q1"));
      rc.response().setStatusMessage("foo").end();
    });
    testRequest(HttpMethod.GET, "/some+path?q1=some+query", 200, "foo");
  }

  @Test
  public void testMultipleSetHandler() throws Exception {
    router.get("/path").handler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.next();
    }).handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    });
    testRequest(HttpMethod.GET, "/path", 200, "OK", "handler1handler2handler3");
  }

  @Test
  public void testMultipleSetFailureHandler() throws Exception {
    router.get("/path").handler(routingContext -> routingContext.fail(500)).failureHandler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.next();
    }).failureHandler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).failureHandler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.setStatusMessage("ERROR");
      response.setStatusCode(500);
      response.end(routingContext.get("response") + "handler3");
    });
    testRequest(HttpMethod.GET, "/path", 500, "ERROR", "handler1handler2handler3");
  }

  @Test
  public void testMultipleSetFailureHandlerCorrectOrder() throws Exception {
    router.route().failureHandler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.next();
    });

    router.get("/path").handler(routingContext -> routingContext.fail(500)).failureHandler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).failureHandler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.setStatusMessage("ERROR");
      response.setStatusCode(500);
      response.end(routingContext.get("response") + "handler3");
    });
    testRequest(HttpMethod.GET, "/path", 500, "ERROR", "handler1handler2handler3");
  }

  @Test
  public void testMultipleHandlersMixed() throws Exception {
    router.route().failureHandler(routingContext -> {
      routingContext.put("response", "fhandler1");
      routingContext.next();
    });

    router.get("/:param").handler(routingContext -> {
      if (routingContext.pathParam("param").equals("fail")) routingContext.fail(500);
      routingContext.put("response", "handler1");
      routingContext.next();
    }).handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    }).failureHandler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "fhandler2");
      routingContext.next();
    }).failureHandler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.setStatusMessage("ERROR");
      response.setStatusCode(500);
      response.end(routingContext.get("response") + "fhandler3");
    });
    testRequest(HttpMethod.GET, "/path", 200, "OK", "handler1handler2handler3");
    testRequest(HttpMethod.GET, "/fail", 500, "ERROR", "fhandler1fhandler2fhandler3");
  }

  @Test
  public void testMultipleHandlersMultipleConnections() throws Exception {
    router.get("/path").handler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.next();
    }).handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    });
    CountDownLatch latch = new CountDownLatch(100);

    for (int i = 0; i < 100; i++) {
      vertx.executeBlocking(future -> {
        try {
          testSyncRequest("GET", "/path", 200, "OK", "handler1handler2handler3");
          future.complete();
        } catch (Exception e) {
          e.printStackTrace();
          future.fail(e);
        }
      }, asyncResult -> {
        assertFalse(asyncResult.failed());
        assertNull(asyncResult.cause());
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  /*
  This test is for issue #729 and #740 about thread safety and errors of multiple handlers
  In this test case I try 100 connections in separated worker threads with random delays and old fashion Java sync http client.
  I've also added a timer when I call routingContext.next()
   */
  @Test
  public void testMultipleHandlersMultipleConnectionsDelayed() throws Exception {
    router.get("/path").handler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
    }).handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    });

    CountDownLatch latch = new CountDownLatch(100);
    for (int i = 0; i < 100; i++) {
      // using executeBlocking should create multiple connections
      vertx.executeBlocking(future -> {
        try {
          Thread.sleep((int) (1 + Math.random() * 10));
          testSyncRequest("GET", "/path", 200, "OK", "handler1handler2handler3");
          future.complete();
        } catch (Exception e) {
          future.fail(e);
        }
      }, asyncResult -> {
        assertFalse(asyncResult.failed());
        assertNull(asyncResult.cause());
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  /*
    This test is similar to test above but it mixes right and failing requests
   */
  @Test
  public void testMultipleHandlersMultipleConnectionsDelayedMixed() throws Exception {
    router.get("/:param").handler(routingContext -> {
      if (routingContext.pathParam("param").equals("fail")) {
        routingContext.fail(400);
      } else {
        routingContext.put("response", "handler1");
        routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
      }
    }).failureHandler(routingContext -> {
      routingContext.put("response", "fhandler1");
      routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
    }).handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    }).failureHandler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "fhandler2");
      routingContext.vertx().setTimer((int) (1 + Math.random() * 10), asyncResult -> routingContext.next());
    }).failureHandler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.setStatusMessage("ERROR");
      response.setStatusCode(400);
      response.end(routingContext.get("response") + "fhandler3");
    });

    final int multipleConnections = 500;

    CountDownLatch latch = new CountDownLatch(multipleConnections);

    Handler<Future<Object>> execute200Request = future -> {
      try {
        Thread.sleep((int) (1 + Math.random() * 10));
        testSyncRequest("GET", "/path", 200, "OK", "handler1handler2handler3");
        future.complete();
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
        future.fail(e);
      }
    };

    Handler<Future<Object>> execute400Request = future -> {
      try {
        Thread.sleep((int) (1 + Math.random() * 10));
        testSyncRequest("GET", "/fail", 400, "ERROR", "fhandler1fhandler2fhandler3");
        future.complete();
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
        future.fail(e);
      }
    };

    for (int i = 0; i < multipleConnections; i++) {
      // using executeBlocking should create multiple connections
      vertx.executeBlocking((new Random().nextBoolean() ? execute200Request : execute400Request), objectAsyncResult -> {
        assertTrue(objectAsyncResult.succeeded());
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }


  @Test
  public void testMultipleSetHandlerMultipleRouteObject() throws Exception {
    router.get("/path").handler(routingContext -> {
      routingContext.put("response", "handler1");
      routingContext.next();
    });
    router.get("/path").handler(routingContext -> {
      routingContext.put("response", routingContext.get("response") + "handler2");
      routingContext.next();
    }).handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      response.end(routingContext.get("response") + "handler3");
    });
    testRequest(HttpMethod.GET, "/path", 200, "OK", "handler1handler2handler3");
  }

  @Test
  public void testSetRegexGroupsNamesMethod() throws Exception {
    List<String> groupNames = new ArrayList<>();
    groupNames.add("hello");

    Route route1 = router.getWithRegex("\\/(?<p0>[a-z]{2})");
    route1.setRegexGroupsNames(groupNames);
    route1.handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("hello"))
      .end());
    testRequest(HttpMethod.GET, "/hi", 200, "hi");

  }

  @Test
  public void testRegexGroupsNamesWithMethodOverride() throws Exception {
    List<String> groupNames = new ArrayList<>();
    groupNames.add("FirstParam");
    groupNames.add("SecondParam");

    Route route = router.getWithRegex("\\/([a-z]{2})([a-z]{2})");
    route.setRegexGroupsNames(groupNames);
    route.handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("FirstParam") + "-" + routingContext.pathParam("SecondParam"))
      .end());
    testRequest(HttpMethod.GET, "/aabb", 200, "aa-bb");
  }

  @Test
  public void testSetRegexGroupsNamesMethodWithUnorderedGroups() throws Exception {
    List<String> groupNames = new ArrayList<>();
    groupNames.add("firstParam");
    groupNames.add("secondParam");

    Route route1 = router.getWithRegex("\\/(?<p1>[a-z]{2})(?<p0>[a-z]{2})");
    route1.setRegexGroupsNames(groupNames);
    route1.handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("firstParam") + "-" + routingContext.pathParam("secondParam"))
      .end());
    testRequest(HttpMethod.GET, "/bbaa", 200, "aa-bb");

  }

  @Test
  public void testSetRegexGroupsNamesMethodWithNestedRegex() throws Exception {
    List<String> groupNames = new ArrayList<>();
    groupNames.add("firstParam");
    groupNames.add("secondParam");

    Route route1 = router.getWithRegex("\\/(?<p1>[a-z]{2}(?<p0>[a-z]{2}))");
    route1.setRegexGroupsNames(groupNames);
    route1.handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("firstParam") + "-" + routingContext.pathParam("secondParam"))
      .end());
    testRequest(HttpMethod.GET, "/bbaa", 200, "aa-bbaa");

  }

  @Test
  public void testRegexGroupsNames() throws Exception {
    router.getWithRegex("\\/(?<firstParam>[a-z]{2})(?<secondParam>[a-z]{2})").handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("firstParam") + "-" + routingContext.pathParam("secondParam"))
      .end());
    testRequest(HttpMethod.GET, "/aabb", 200, "aa-bb");
  }

  @Test
  public void testRegexGroupsNamesWithNestedGroups() throws Exception {
    router.getWithRegex("\\/(?<secondParam>[a-z]{2}(?<firstParam>[a-z]{2}))").handler(routingContext -> routingContext
      .response()
      .setStatusCode(200)
      .setStatusMessage(routingContext.pathParam("firstParam") + "-" + routingContext.pathParam("secondParam"))
      .end());
    testRequest(HttpMethod.GET, "/bbaa", 200, "aa-bbaa");
  }

  private Handler<RoutingContext> generateHandler(final int i) {
    return routingContext -> routingContext.put(Integer.toString(i), i).next();
  }

  @Test
  public void stressTestMultipleHandlers() throws Exception {
    final int HANDLERS_NUMBER = 100;
    final int REQUESTS_NUMBER = 200;

    Route r = router.get("/path");
    for (int i = 0; i < HANDLERS_NUMBER; i++) {
      r.handler(generateHandler(i));
    }
    r.handler(routingContext -> {
      StringBuilder sum = new StringBuilder();
      for (int i = 0; i < HANDLERS_NUMBER; i++) {
        sum.append((Integer) routingContext.get(Integer.toString(i)));
      }
      routingContext.response()
        .setStatusCode(200)
        .setStatusMessage("OK")
        .end(sum.toString());
    });

    CountDownLatch latch = new CountDownLatch(REQUESTS_NUMBER);
    final StringBuilder sum = new StringBuilder();
    for (int i = 0; i < HANDLERS_NUMBER; i++) {
      sum.append(i);
    }
    for (int i = 0; i < REQUESTS_NUMBER; i++) {
      // using executeBlocking should create multiple connections
      vertx.executeBlocking(future -> {
        try {
          Thread.sleep((int) (1 + Math.random() * 10));
          testSyncRequest("GET", "/path", 200, "OK", sum.toString());
          future.complete();
        } catch (Exception e) {
          future.fail(e);
        }
      }, asyncResult -> {
        assertFalse(asyncResult.failed());
        assertNull(asyncResult.cause());
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  @Test
  public void testDecodingError() throws Exception {
    String BAD_PARAM = "~!@\\||$%^&*()_=-%22;;%27%22:%3C%3E/?]}{";

    router.route().handler(RoutingContext::next);
    router.route("/path").handler(rc -> rc.response().setStatusCode(500).end());
    testRequest(HttpMethod.GET, "/path?q=" + BAD_PARAM, 400, "Bad Request");
  }

  @Test
  public void testRoutePathNoSlashBegin() throws Exception {
    String path = "?test=something";
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, path, 400, "Bad Request");
  }

  @Test
  public void testMultipleHandlersWithFailuresDeadlock() throws Exception {
    AtomicBoolean first = new AtomicBoolean(true);
    CountDownLatch firstHandlerLatch = new CountDownLatch(1);
    CountDownLatch secondHandlerLatch = new CountDownLatch(1);

    router.get("/path").handler(event -> {
      if (!first.compareAndSet(true, false)) {
        // Second run, block until the second handler runs
        try {
          firstHandlerLatch.countDown();
          awaitLatch(secondHandlerLatch);

          // Add a small delay so the exception handler happens first
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // ignore
        }

        event.next();
      } else {
        vertx.executeBlocking(future -> {
          event.next();
          future.complete();
        }, asyncResult -> {});
      }
    });

    router.get("/path").handler(event -> {
      try {
        awaitLatch(firstHandlerLatch);
      } catch (InterruptedException e) {
        // ignore
      }
      secondHandlerLatch.countDown();
      event.fail(new NullPointerException());
    });

    CountDownLatch latch = new CountDownLatch(2);
    for (int i = 0; i < 2; i++) {
      vertx.executeBlocking(future -> {
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.rawMethod()).thenReturn("GET");
        when(request.uri()).thenReturn("http://localhost/path");
        when(request.absoluteURI()).thenReturn("http://localhost/path");
        when(request.host()).thenReturn("localhost");
        when(request.path()).thenReturn("/path");
        when(request.response()).thenReturn(response);
        when(response.ended()).thenReturn(true);
        router.handle(request);
        future.complete();
      }, asyncResult -> {
        assertFalse(asyncResult.failed());
        assertNull(asyncResult.cause());
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }
}
