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

package io.vertx.ext.rest.test;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.rest.Route;
import io.vertx.ext.rest.Router;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterTest extends VertxTestBase {

  HttpServer server;
  HttpClient client;
  Router router;
  static Set<HttpMethod> METHODS = new HashSet<>(Arrays.asList(HttpMethod.DELETE, HttpMethod.GET,
    HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.TRACE, HttpMethod.POST, HttpMethod.PUT));


  @Override
  public void setUp() throws Exception {
    super.setUp();
    router = Router.router();
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    client = vertx.createHttpClient(new HttpClientOptions());
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(onSuccess(res -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

  @Override
  public void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
    if (server != null) {
      CountDownLatch latch = new CountDownLatch(1);
      server.close((asyncResult) -> {
        assertTrue(asyncResult.succeeded());
        latch.countDown();
      });
      awaitLatch(latch);
    }
    super.tearDown();
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
  public void testRoutePathAndMethod() throws Exception {
    for (HttpMethod meth: METHODS) {
      testRoutePathAndMethod(meth);
    }
  }

  private void testRoutePathAndMethod(HttpMethod method) throws Exception {
    String path = "/blah";
    router.clear();
    router.route(method, path).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPath(method, path);
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

    testPath(path1);
    testPath(path2);
  }

  @Test
  public void testRoutePathBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPath(path);
  }

  @Test
  public void testRoutePathAndMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPath(HttpMethod.GET, path);
    testRequest(HttpMethod.POST, path, 404, "Not Found");
  }

  @Test
  public void testRoutePathAndMultipleMethodBuilder() throws Exception {
    String path = "/blah";
    router.route().path(path).method(HttpMethod.GET).method(HttpMethod.POST).handler(rc -> {
      rc.response().setStatusCode(200).setStatusMessage(rc.request().path()).end();
    });
    testPath(HttpMethod.GET, path);
    testPath(HttpMethod.POST, path);
    testRequest(HttpMethod.PUT, path, 404, "Not Found");
  }

  private void testPath(String path) throws Exception {
    for (HttpMethod meth: METHODS) {
      testPath(meth, path);
    }
  }

  private void testPath(HttpMethod method, String path) throws Exception {
    testRequest(method, path, 200, path);
    testRequest(method, path + "wibble", 200, path + "wibble");
    testRequest(method, path + "/wibble", 200, path + "/wibble");
    testRequest(method, path + "/wibble/floob", 200, path + "/wibble/floob");
    testRequest(method, path.substring(0, path.length() - 1), 404, "Not Found");
    testRequest(method, "/", 404, "Not Found");
    testRequest(method, "/" + UUID.randomUUID().toString(), 404, "Not Found");
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage) throws Exception {
    testRequest(method, path, statusCode, statusMessage, null);
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage,
                             String responseBody) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.request(method, 8080, "localhost", path, resp -> {
      assertEquals(statusCode, resp.statusCode());
      assertEquals(statusMessage, resp.statusMessage());
      if (responseBody == null) {
        latch.countDown();
      } else {
        resp.bodyHandler(buff -> {
          assertEquals(responseBody, buff.toString());
          latch.countDown();
        });
      }
    }).end();
    awaitLatch(latch);
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
      // An extra call to next shouldn't cause a problem
      rc.next();
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
      // An extra call to next shouldn't cause a problem
      rc.next();
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
    route.last(true);
    route.handler(rc -> {
      rc.response().write("apples");
      rc.response().end();
      rc.next();
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
      rc.next();
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
      rc.next();
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
    Route route = router.route(path).exceptionHandler(rc -> {
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

  // TODO timeouts

  // TODO test route with no path specified

  // TODO test consumes/produces

  // params

  // regex

  // cookies

  // form params

  // uploads

  @Test
  public void testExceptionHandler1() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    }).exceptionHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 555, "oh dear");
  }

  @Test
  public void testExceptionHandler2() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    router.route("/bl").exceptionHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    testRequest(HttpMethod.GET, path, 555, "oh dear");
  }

  @Test
  public void testDefaultExceptionHandler() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    // Default failure response
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }

  @Test
  public void testExceptionHandlerNoMatch() throws Exception {
    String path = "/blah";
    router.route(path).handler(rc -> {
      throw new RuntimeException("ouch!");
    });
    router.route("/other").exceptionHandler(frc -> {
      frc.response().setStatusCode(555).setStatusMessage("oh dear").end();
    });
    // Default failure response
    testRequest(HttpMethod.GET, path, 500, "Internal Server Error");
  }



//  @Test
//  public void testTimeout() throws Exception {
//    String path = "/blah";
//    router.route(path).handler(rc -> {
//      rc.next();
//    });
//    router.route(path).handler(rc -> {
//      rc.next();
//    });
//    testRequest(HttpMethod.GET, path, 404, "Not Found");
//  }

  //private static final String DEFAULT_404 = "<html><body><h1>Resource not found</h1></body></html>";

}
