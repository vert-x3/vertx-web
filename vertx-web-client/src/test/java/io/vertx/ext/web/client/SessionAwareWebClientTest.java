/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.impl.CookieStoreImpl;
import io.vertx.ext.web.client.spi.CookieStore;
import io.vertx.ext.web.multipart.MultipartForm;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
@RunWith(VertxUnitRunner.class)
public class SessionAwareWebClientTest {
  private static final int PORT = 8080;
  
  private WebClient plainWebClient;
  private WebClientSession client;
  private Vertx vertx;

  private HttpServer server;

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();
    plainWebClient = buildPlainWebClient();
    client = buildClient(plainWebClient, CookieStore.build());
    server = vertx.createHttpServer(new HttpServerOptions().setPort(PORT).setHost("0.0.0.0"));
  }
  
  private WebClient buildPlainWebClient() {
    HttpClient vc = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(PORT).setDefaultHost("localhost"));
    return WebClient.wrap(vc);
  }

  private WebClientSession buildClient(WebClient webClient, CookieStore cookieStore) {
    return WebClientSession.create(webClient, cookieStore);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  private void prepareServer(TestContext context, Consumer<HttpServerRequest> reqHandler) {
    Async async = context.async();
    server.requestHandler(req -> {
      try {
        reqHandler.accept(req);
      } finally {
        req.response().end();
      }
    });
    server.listen(context.asyncAssertSuccess(s -> async.complete()));
    async.awaitSuccess(15000);
  }
  
  private Cookie getCookieValue(HttpServerRequest req, String name) {
    List<String> cookies = req.headers().getAll("cookie");
    for (String h : cookies) {
      Set<Cookie> all = ServerCookieDecoder.STRICT.decode(h);
      for (Cookie c : all) {
        if (c.name().equals(name)) {
          return c;
        }
      }
    }
    return null;
  }

  @Test
  public void testReadCookie(TestContext context) {
    Async async = context.async();
    prepareServer(context, req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test", "toast")));
    });

    client.get(PORT, "localhost", "/").send(ar -> {
      context.assertTrue(ar.succeeded());
      validate(context, client.cookieStore().get(false, "localhost", "/"),
          new String[] { "test" }, new String[] { "toast" });
      async.complete();
    });
  }

  @Test
  public void testReadManyCookies(TestContext context) {
    Async async = context.async();
    prepareServer(context, req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test1", "toast1")));
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test2", "toast2")));
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test3", "toast3")));
    });

    client.get(PORT, "localhost", "/").send(ar -> {
      context.assertTrue(ar.succeeded());
      validate(context, client.cookieStore().get(false, "localhost", "/"),
          new String[] { "test1" ,"test2", "test3" }, new String[] { "toast1", "toast2", "toast3" });
      async.complete();
    });
  }

  @Test
  public void testReceiveAndSendCookieRegularPath(TestContext context) {
    testReceiveAndSendCookie(context, "/", "/");
  }

  @Test
  public void testReceiveAndSendCookieQuestionMark(TestContext context) {
    testReceiveAndSendCookie(context, "/path?a=b", "/path");
  }

  @Test
  public void testReceiveAndSendCookieHash(TestContext context) {
    testReceiveAndSendCookie(context, "/path#fragment", "/path");
  }

  public void testReceiveAndSendCookie(TestContext context, String pathToCall, String cookiePath) {
    prepareServer(context, req -> {
      req.response().setChunked(true);
      Cookie c = new DefaultCookie("test", "toast");
      c.setPath(cookiePath);
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(c));
      c = getCookieValue(req, "test");
      if (c != null) {
        req.response().write("OK");
      } else {
        req.response().write("ERR");
      }
    });

    HttpRequest<Buffer> req = client.get(PORT, "localhost", pathToCall);

    {
      Async async = context.async();
      req.send(ar -> {
        context.assertTrue(ar.succeeded());
        async.complete();
      });
      async.await();
    }

    Async async = context.async();
    req.send(ar -> {
      context.assertTrue(ar.succeeded());
      HttpResponse<Buffer> res = ar.result();
      context.assertEquals(200, res.statusCode());
      context.assertEquals("OK", res.bodyAsString());
      async.complete();
    });
  }

  @Test
  public void testSessionHeaders(TestContext context) {
    String headerName = "x-client-header";
    String headerVal = "MY-HEADER";
    
    prepareServer(context, req -> {
      req.response().setChunked(true);
      if (headerVal.equals(req.getHeader(headerName))) {
        req.response().write("OK");
      } else {
        req.response().write("ERR");
      }
    });
    
    Async async = context.async();
    client.addHeader(headerName, headerVal);
    client.get("/").send(ar -> {
      context.assertTrue(ar.succeeded());
      HttpResponse<Buffer> res = ar.result();
      context.assertEquals(200, res.statusCode());
      context.assertEquals("OK", res.bodyAsString());
      async.complete();
    });
    
  }

  @Test
  public void testSharedWebClient(TestContext context) {
    AtomicInteger cnt = new AtomicInteger(0);
    prepareServer(context, req -> {
      req.response().setChunked(true);
      Cookie c = getCookieValue(req, "test");
      if (c != null) {
        req.response().write("OK");
      } else {
        c = new DefaultCookie("test", "" + cnt.getAndIncrement());
        c.setPath("/");
        req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(c));
        req.response().write("ERR");
      }
    });

    int numClients = 4;

    WebClientSession[] clients = IntStream.range(0, numClients)
      .mapToObj(val -> val == 0 ? client :  buildClient(plainWebClient, CookieStore.build()))
      .toArray(WebClientSession[]::new);

    Async async = context.async(clients.length);
    for (WebClientSession client : clients) {
      HttpRequest<Buffer> req = client.get(PORT, "localhost", "/index.html");
  
      Async waiter = context.async();
      req.send(ar -> {
        context.assertTrue(ar.succeeded());
        waiter.complete();
      });
      waiter.await();
  
      req.send(ar -> {
        context.assertTrue(ar.succeeded());
        HttpResponse<Buffer> res = ar.result();
        context.assertEquals(200, res.statusCode());
        context.assertEquals("OK", res.bodyAsString());
        async.countDown();
      });
    }
    
    async.await();
    
    cnt.set(0);
    for (WebClientSession client : clients) {
      Iterable<Cookie> cookies = client.cookieStore().get(false, "localhost", "/");

      int i = 0;
      for (Cookie c : cookies) {
        context.assertEquals("" + cnt.getAndIncrement(), c.value());
        i++;
      }
      
      context.assertEquals(i, 1);
    }
  }

  @Test
  public void testClientHeaders(TestContext context) {
    final String headerPrefix = "x-h";
    prepareServer(context, req -> {
      String expected;
      for (int i = 0; i < 3; i++) {
        expected = String.valueOf(i);
        if (!expected.equals(req.getHeader(headerPrefix + i))) {
          req.response().setStatusCode(500);
          break;
        }
      }
    });

    HttpRequest<Buffer> req = client.get(PORT, "localhost", "/");

    Async respLatch = context.async();
    req.send(context.asyncAssertSuccess(resp -> {
      context.assertEquals(500, resp.statusCode());
      respLatch.complete();
    }));
    respLatch.awaitSuccess(15000);

    for (int i = 0; i < 3; i++) {
      req.putHeader(headerPrefix + i, String.valueOf(i));
    }
    
    Async async = context.async();
    req.send(ar -> {
      context.assertTrue(ar.succeeded());
      context.assertEquals(200, ar.result().statusCode());
      async.complete();
    });
  }
  
  @Test
  public void testHeadersAndCookies(TestContext context) {
    String headerName = "x-toolkit";
    String headerValue = "vert.x";
    String cookieName = "JSESSIONID";
    String cookieValue = "123";
    
    prepareServer(context, req -> {
      if (!headerValue.equals(req.getHeader(headerName))) {
        req.response().setStatusCode(500);
      }
      String val = req.getParam("c");
      if ("X".equals(val)) {
        req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie(cookieName, cookieValue)));
      } else {
        Cookie c = getCookieValue(req, cookieName);
        if (c == null || !cookieValue.equals(c.value())) {
          req.response().setStatusCode(500);
        }
      }
    });

    HttpRequest<Buffer> req = client.get(PORT, "localhost", "/")
        .putHeader(headerName, headerValue)
        .addQueryParam("c", "X");

    {
      Async async = context.async();
      req.send(ar -> {
        context.assertTrue(ar.succeeded());
        context.assertEquals(200, ar.result().statusCode());
        async.complete();
      });
      async.await();
    }
    
    req.queryParams().clear();
    Async async = context.async();
    req.send(ar -> {
      context.assertTrue(ar.succeeded());
      context.assertEquals(200, ar.result().statusCode());
      async.complete();
    });
  }
  
  @Test
  public void testRequestIsPrepared(TestContext context) {
    prepareServer(context, req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test", "toast")));
    });

    Consumer<HttpRequest<Buffer>> check = r -> {
      Async async = context.async();
      Cookie c = new DefaultCookie("test", "localhost");
      c.setPath("/");
      client.cookieStore().remove(c);
      r.send(ar -> {
        async.complete();
        validate(context, client.cookieStore().get(false, "localhost", "/"),
            new String[] { "test" }, new String[] { "toast" });
      });
      async.await();
    };

    check.accept(client.delete("/"));
    check.accept(client.delete("localhost", "/"));
    check.accept(client.delete(PORT, "localhost", "/"));
    check.accept(client.deleteAbs("http://localhost/"));
    check.accept(client.get("/"));
    check.accept(client.get("localhost", "/"));
    check.accept(client.get(PORT, "localhost", "/"));
    check.accept(client.getAbs("http://localhost/"));
    check.accept(client.head("/"));
    check.accept(client.head("localhost", "/"));
    check.accept(client.head(PORT, "localhost", "/"));
    check.accept(client.headAbs("http://localhost/"));
    check.accept(client.patch("/"));
    check.accept(client.patch("localhost", "/"));
    check.accept(client.patch(PORT, "localhost", "/"));
    check.accept(client.patchAbs("http://localhost/"));
    check.accept(client.post("/"));
    check.accept(client.post("localhost", "/"));
    check.accept(client.post(PORT, "localhost", "/"));
    check.accept(client.postAbs("http://localhost/"));
    check.accept(client.put("/"));
    check.accept(client.put("localhost", "/"));
    check.accept(client.put(PORT, "localhost", "/"));
    check.accept(client.putAbs("http://localhost/"));
    check.accept(client.request(HttpMethod.GET, new RequestOptions()));
    check.accept(client.request(HttpMethod.GET, "/"));
    check.accept(client.request(HttpMethod.GET, "localhost", "/"));
    check.accept(client.request(HttpMethod.GET, PORT, "localhost", "/"));
    check.accept(client.requestAbs(HttpMethod.GET, "http://localhost/"));
  }
  
  @Test
  public void testSendRequest(TestContext context) throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    client = buildClient(plainWebClient, new CookieStoreImpl() {
      @Override
      public CookieStore put(Cookie cookie) {
        count.incrementAndGet();
        return super.put(cookie);
      }
    });
    
    String encodedCookie = ServerCookieEncoder.STRICT.encode(new DefaultCookie("a", "1"));
    prepareServer(context, req -> {
      req.response().headers().add("set-cookie", encodedCookie);
    });
    
    int expected = 7;
    Async async = context.async(expected);
    Handler<AsyncResult<HttpResponse<Buffer>>> handler = ar -> { async.countDown(); };
    HttpRequest<Buffer> req = client.post("/");
    req.send(handler);
    req.sendBuffer(Buffer.buffer(), handler);
    req.sendForm(new CaseInsensitiveHeaders().add("a", "b"), handler);
    req.sendJson("", handler);
    req.sendJsonObject(new JsonObject(), handler);
    req.sendMultipartForm(MultipartForm.create().attribute("a", "b"), handler);
    
    File f = File.createTempFile("vertx", ".tmp");
    f.deleteOnExit();
    AsyncFile asyncFile = vertx.fileSystem().openBlocking(f.getAbsolutePath(), new OpenOptions());
    req.sendStream(asyncFile, handler);
    
    async.await();
    asyncFile.close();
    
    context.assertEquals(expected, count.get());
  }

  @Test
  public void testMultipleVerticles(TestContext testContext) {
    String cookieName = "a";
    
    prepareServer(testContext, req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie(cookieName, req.toString())));
    });
    
    int numVerticles = 4;
    int runs = 10;
    Async async = testContext.async(numVerticles * runs);
    
    String host = "localhost";
    String uri = "/";
    
    Verticle v = new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        vertx.eventBus().consumer("test", m -> {
          client.get(host, uri).send(ar -> {
            testContext.assertTrue(ar.succeeded());
            async.countDown();            
          });
        });
      }
    };
    
    Async asyncDeploy = testContext.async(numVerticles);
    for (int i = 0; i < numVerticles; i++) {
      vertx.deployVerticle(v, ar -> { asyncDeploy.countDown(); });
    }
    asyncDeploy.await();
    
    for (int i = 0; i < runs; i++) {
      vertx.eventBus().publish("test", "");
    }
    
    async.await();
    
    Async asyncEnd = testContext.async();
    vertx.undeploy(v.getClass().getName(), ar -> {
      asyncEnd.complete();
    });
    asyncEnd.await();
    
    int i = 0;
    Iterable<Cookie> all = client.cookieStore().get(false, host, uri);
    for (Cookie c : all) {
      i++;
      testContext.assertEquals(c.name(), cookieName);
    }
    assertEquals(1, i);
  }
  
  @Test
  public void testCookieStore(TestContext context) {
    CookieStore store = CookieStore.build();
    Cookie c;

    c = new DefaultCookie("a", "1");
    store.put(c);

    c = new DefaultCookie("b", "2");
    c.setDomain("vertx.io");
    store.put(c);

    c = new DefaultCookie("c", "3");
    c.setDomain("www.vertx.io");
    c.setPath("/web-client");
    store.put(c);

    c = new DefaultCookie("d", "4");
    c.setPath("/web-client");
    store.put(c);

    c = new DefaultCookie("e", "5");
    c.setDomain("vertx.io");
    c.setSecure(true);
    store.put(c);

    c = new DefaultCookie("b", "20");
    c.setDomain("www.vertx.io");
    store.put(c);

    c = new DefaultCookie("b", "200");
    c.setDomain("www.vertx.io");
    c.setPath("/web-client");
    store.put(c);

    validate(context, store.get(false, "www.vertx.io", "/"), new String[] { "a", "b" }, new String[] { "1", "20"} );
    validate(context, store.get(false, "a.www.vertx.io", "/"),  new String[] { "a", "b" }, new String[] { "1", "20"});
    validate(context, store.get(false, "test.vertx.io", "/"), new String[] { "a", "b" }, new String[] { "1", "2" });
    validate(context, store.get(false, "www.vertx.io", "/web-client"), 
        new String[] { "a", "b", "c", "d" }, 
        new String[] { "1", "200", "3", "4" });
    validate(context, store.get(true, "test.vertx.io", "/"), 
        new String[] { "a", "b", "e" }, 
        new String[] { "1", "2", "5" });
  }
  
  @Test
  public void testCookieStoreIsFluent(TestContext context) {
    CookieStore store = CookieStore.build();
    Cookie cookie = new DefaultCookie("a", "a");
    context.assertTrue(store == store.put(cookie));
    context.assertTrue(store == store.remove(cookie));
  }

  public void validate(TestContext context, Iterable<Cookie> cookies, String[] expectedNames, String[] expectedVals) {
    List<String> foundNames = new ArrayList<>();
    List<String> foundVals = new ArrayList<>();
    for (String s : expectedNames) {
      for (Cookie c : cookies) {
        if (c.name().equals(s)) {
          foundNames.add(c.name());
          foundVals.add(c.value());
          break;
        }
      }
    }
    
    assertArrayEquals(context, expectedNames, foundNames.toArray());
    if (expectedVals != null) {
      assertArrayEquals(context, expectedVals, foundVals.toArray());
    }
    
    int count = 0;
    Iterator<Cookie> iter = cookies.iterator();
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertEquals(expectedNames.length, count);
  }

  private void assertArrayEquals(TestContext context, String[] expected, Object[] found) {
    context.assertEquals(expected.length, found.length);
    for (int i = 0; i < expected.length; i++) {
      context.assertEquals(expected[i], found[i], "Element " + i + " is not equal");
    }
  }
}
