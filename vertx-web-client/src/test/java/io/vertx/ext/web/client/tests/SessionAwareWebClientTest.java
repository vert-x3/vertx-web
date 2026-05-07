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
package io.vertx.ext.web.client.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.impl.CookieStoreImpl;
import io.vertx.ext.web.client.spi.CookieStore;
import io.vertx.ext.web.multipart.MultipartForm;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
@VertxTest
public class SessionAwareWebClientTest {
  private static final int PORT = 8080;

  private WebClient plainWebClient;
  private WebClientSession client;
  private Vertx vertx;

  private HttpServer server;

  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    this.vertx = vertx;
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

  private void prepareServer(Consumer<HttpServerRequest> reqHandler) {
    server.requestHandler(req -> {
      try {
        reqHandler.accept(req);
      } finally {
        if (!req.response().ended())
          req.response().end();
      }
    });
    server
      .listen()
      .await();
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
  public void testReadCookie() {
    prepareServer(req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test", "toast")));
    });
    client.get(PORT, "localhost", "/").send().await();
    validate(client.cookieStore().get(false, "localhost", "/"),
      new String[] { "test" }, new String[] { "toast" });
  }

  @Test
  public void testReadManyCookies() {

    prepareServer(req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test1", "toast1")));
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test2", "toast2")));
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test3", "toast3")));
    });

    client
      .get(PORT, "localhost", "/")
      .send()
      .await();
    validate(client.cookieStore().get(false, "localhost", "/"),
      new String[] { "test1" ,"test2", "test3" }, new String[] { "toast1", "toast2", "toast3" });
  }

  @Test
  public void testReceiveAndSendCookieRegularPath() {
    testReceiveAndSendCookie("/", "/");
  }

  @Test
  public void testReceiveAndSendCookieQuestionMark() {
    testReceiveAndSendCookie("/path?a=b", "/path");
  }

  @Test
  public void testReceiveAndSendCookieHash() {
    testReceiveAndSendCookie("/path#fragment", "/path");
  }

  public void testReceiveAndSendCookie(String pathToCall, String cookiePath) {

    prepareServer(req -> {
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

    req.send().await();

    HttpResponse<Buffer> resp = req.send().await();
    assertEquals(200, resp.statusCode());
    assertEquals("OK", resp.bodyAsString());
  }

  @Test
  public void testSessionHeaders() {
    String headerName = "x-client-header";
    String headerVal = "MY-HEADER";

    prepareServer(req -> {
      req.response().setChunked(true);
      if (headerVal.equals(req.getHeader(headerName))) {
        req.response().write("OK");
      } else {
        req.response().write("ERR");
      }
    });

    client.addHeader(headerName, headerVal);
    HttpResponse<Buffer> res = client.get("/").send().await();
    assertEquals(200, res.statusCode());
    assertEquals("OK", res.bodyAsString());
  }

  @Test
  public void testSharedWebClient() {
    AtomicInteger cnt = new AtomicInteger(0);

    int numClients = 4;

    prepareServer(req -> {
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

    WebClientSession[] clients = IntStream.range(0, numClients)
      .mapToObj(val -> val == 0 ? client :  buildClient(plainWebClient, CookieStore.build()))
      .toArray(WebClientSession[]::new);

    for (WebClientSession webClientSession : clients) {
      HttpRequest<Buffer> req = webClientSession.get(PORT, "localhost", "/index.html");

      req.send().await();

      HttpResponse<Buffer> res = req.send().await();
      assertEquals(200, res.statusCode());
      assertEquals("OK", res.bodyAsString());
    }
  }

  @Test
  public void testClientHeaders() {
    final String headerPrefix = "x-h";

    prepareServer(req -> {
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

    HttpResponse<Buffer> resp = req.send().await();
    assertEquals(500, resp.statusCode());

    for (int i = 0; i < 3; i++) {
      req.putHeader(headerPrefix + i, String.valueOf(i));
    }

    HttpResponse<Buffer> res = req.send().await();
    assertEquals(200, res.statusCode());
  }

  @Test
  public void testHeadersAndCookies() {
    String headerName = "x-toolkit";
    String headerValue = "vert.x";
    String cookieName = "JSESSIONID";
    String cookieValue = "123";

    prepareServer(req -> {
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

    HttpResponse<Buffer> resp = req.send().await();
    assertEquals(200, resp.statusCode());

    req.queryParams().clear();
    resp = req.send().await();
    assertEquals(200, resp.statusCode());
  }

  @Test
  public void testRequestIsPrepared() {
    prepareServer(req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test", "toast")));
    });

    Consumer<HttpRequest<Buffer>> check = r -> {
      Cookie c = new DefaultCookie("test", "localhost");
      c.setPath("/");
      client.cookieStore().remove(c);
      r.send().await();
      validate(client.cookieStore().get(false, "localhost", "/"),
        new String[] { "test" }, new String[] { "toast" });
    };

    check.accept(client.delete("/"));
    check.accept(client.delete("localhost", "/"));
    check.accept(client.delete(PORT, "localhost", "/"));
    check.accept(client.deleteAbs("http://localhost:8080/"));
    check.accept(client.get("/"));
    check.accept(client.get("localhost", "/"));
    check.accept(client.get(PORT, "localhost", "/"));
    check.accept(client.getAbs("http://localhost:8080/"));
    check.accept(client.head("/"));
    check.accept(client.head("localhost", "/"));
    check.accept(client.head(PORT, "localhost", "/"));
    check.accept(client.headAbs("http://localhost:8080/"));
    check.accept(client.patch("/"));
    check.accept(client.patch("localhost", "/"));
    check.accept(client.patch(PORT, "localhost", "/"));
    check.accept(client.patchAbs("http://localhost:8080/"));
    check.accept(client.post("/"));
    check.accept(client.post("localhost", "/"));
    check.accept(client.post(PORT, "localhost", "/"));
    check.accept(client.postAbs("http://localhost:8080/"));
    check.accept(client.put("/"));
    check.accept(client.put("localhost", "/"));
    check.accept(client.put(PORT, "localhost", "/"));
    check.accept(client.putAbs("http://localhost:8080/"));
    check.accept(client.request(HttpMethod.GET, new RequestOptions()));
    check.accept(client.request(HttpMethod.GET, "/"));
    check.accept(client.request(HttpMethod.GET, "localhost", "/"));
    check.accept(client.request(HttpMethod.GET, PORT, "localhost", "/"));
    check.accept(client.requestAbs(HttpMethod.GET, "http://localhost:8080/"));
  }

  @Test
  public void testSendRequest(Checkpoint checkpoint) throws IOException {
    AtomicInteger count = new AtomicInteger(0);
    client = buildClient(plainWebClient, new CookieStoreImpl() {
      @Override
      public CookieStore put(Cookie cookie) {
        count.incrementAndGet();
        return super.put(cookie);
      }
    });

    String encodedCookie = ServerCookieEncoder.STRICT.encode(new DefaultCookie("a", "1"));
    int expected = 7;
    CountDownLatch done = checkpoint.asLatch(expected);

    prepareServer(req -> {
      req.response().headers().add("set-cookie", encodedCookie);
    });

    Handler<AsyncResult<HttpResponse<Buffer>>> handler = ar -> { done.countDown(); };
    HttpRequest<Buffer> req = client.post("/");
    req.send().onComplete(handler);
    req.sendBuffer(Buffer.buffer()).onComplete(handler);
    req.sendForm(HttpHeaders.set("a", "b")).onComplete(handler);
    req.sendJson("").onComplete(handler);
    req.sendJsonObject(new JsonObject()).onComplete(handler);
    req.sendMultipartForm(MultipartForm.create().attribute("a", "b")).onComplete(handler);

    File f = File.createTempFile("vertx", ".tmp");
    f.deleteOnExit();
    AsyncFile asyncFile = vertx.fileSystem().openBlocking(f.getAbsolutePath(), new OpenOptions());
    req.sendStream(asyncFile).onComplete(handler);
  }

  @Test
  public void testMultipleVerticles(Checkpoint checkpoint) {
    String cookieName = "a";

    int numVerticles = 4;
    int runs = 10;

    prepareServer(req -> {
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie(cookieName, req.toString())));
    });

    String host = "localhost";
    String uri = "/";

    CountDownLatch done = checkpoint.asLatch(numVerticles * runs);
    Deployable v = new VerticleBase() {
      @Override
      public Future<?> start() throws Exception {
        vertx.eventBus().consumer("test", m -> {
          client.get(host, uri).send().onComplete(ar -> {
            assertTrue(ar.succeeded());
            done.countDown();
          });
        });
        return super.start();
      }
    };

    for (int i = 0; i < numVerticles; i++) {
      vertx.deployVerticle(v).await();
    }

    for (int i = 0; i < runs; i++) {
      vertx.eventBus().publish("test", "");
    }
  }

  @Test
  public void testCookieStore(Checkpoint checkpoint) {
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

    validate(store.get(false, "www.vertx.io", "/"), new String[] { "a", "b" }, new String[] { "1", "20"} );
    validate(store.get(false, "a.www.vertx.io", "/"),  new String[] { "a", "b" }, new String[] { "1", "20"});
    validate(store.get(false, "test.vertx.io", "/"), new String[] { "a", "b" }, new String[] { "1", "2" });
    validate(store.get(false, "www.vertx.io", "/web-client"),
        new String[] { "a", "b", "c", "d" },
        new String[] { "1", "200", "3", "4" });
    validate(store.get(true, "test.vertx.io", "/"),
        new String[] { "a", "b", "e" },
        new String[] { "1", "2", "5" });
    checkpoint.flag();
  }

  @Test
  public void testCookieStoreIsFluent(Checkpoint checkpoint) {
    CookieStore store = CookieStore.build();
    Cookie cookie = new DefaultCookie("a", "a");
    assertTrue(store == store.put(cookie));
    assertTrue(store == store.remove(cookie));
    checkpoint.flag();
  }

  @Test
  public void testRedirectWithoutLosingCookies() throws Exception {
    String location = "http://localhost:" + PORT + "/ok";

    prepareServer(req -> {
      if (req.path().equals("/redirect")) {
        req
          .response()
          .setStatusCode(301)
          .putHeader("Location", location)
          .putHeader(HttpHeaders.SET_COOKIE, "vertx-web.session=8770f50f5221108c8b8b2d4ee9aef76e");
      } else {
        assertEquals("vertx-web.session=8770f50f5221108c8b8b2d4ee9aef76e", req.getHeader(HttpHeaders.COOKIE));
        req
          .response()
          .end(req.path());
      }
    });

    HttpResponse<Buffer> resp = client.get("/redirect")
      .followRedirects(true)
      .send()
      .await();
    assertEquals(200, resp.statusCode());
    assertEquals("/ok", resp.body().toString());
  }

  public void validate(Iterable<Cookie> cookies, String[] expectedNames, String[] expectedVals) {
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

    assertArrayEquals(expectedNames, foundNames.toArray());
    if (expectedVals != null) {
      assertArrayEquals(expectedVals, foundVals.toArray());
    }

    int count = 0;
    for (Cookie cookie : cookies) {
      count++;
    }
    assertEquals(expectedNames.length, count);
  }

  private void assertArrayEquals(String[] expected, Object[] found) {
    assertEquals(expected.length, found.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], found[i], "Element " + i + " is not equal");
    }
  }
}
