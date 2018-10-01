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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.impl.InternalCookieStore;
import io.vertx.ext.web.client.impl.SessionAwareWebClientImpl;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
@RunWith(VertxUnitRunner.class)
public class SessionAwareWebClientTest {
  private static final int PORT = 8080;
  
  private SessionAwareWebClientImpl client;
  private Vertx vertx;

  private HttpServer server;

  protected VertxOptions getOptions() {
    return new VertxOptions().setAddressResolverOptions(new AddressResolverOptions()
        .setHostsValue(Buffer.buffer("127.0.0.1 somehost\n" + "127.0.0.1 localhost")));
  }

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx(getOptions());
    HttpClient vc = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(PORT).setDefaultHost("localhost"));
    client = (SessionAwareWebClientImpl) SessionAwareWebClient.build(WebClient.wrap(vc));
    server = vertx.createHttpServer(new HttpServerOptions().setPort(PORT).setHost("0.0.0.0"));
  }
  
  @After
  public void terDown() {
    vertx.close();
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
    server.listen(ar -> {
      context.assertTrue(ar.succeeded());
      async.complete();
    });
    async.await();
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
      validate(context, client.getCookieStore().get(false, "localhost", "/"), 
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
      validate(context, client.getCookieStore().get(false, "localhost", "/"), 
          new String[] { "test1" ,"test2", "test3" }, new String[] { "toast1", "toast2", "toast3" });
      async.complete();
    });
  }

  @Test
  public void testSendUserCookie(TestContext context) {
    prepareServer(context, req -> {
      req.response().setChunked(true);
      req.response().headers().add("set-cookie", ServerCookieEncoder.STRICT.encode(new DefaultCookie("test", "toast")));
      Cookie c = getCookieValue(req, "test");
      if (c != null) {
        req.response().write("OK");
      }
    });

    HttpRequest<Buffer> req = client.get(PORT, "localhost", "/");

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
  public void testCookieStore(TestContext context) {
    InternalCookieStore store = (InternalCookieStore) CookieStore.build();
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
