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

import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static io.vertx.core.http.HttpHeaders.*;
import static io.vertx.ext.web.AllowForwardHeaders.*;

public class ForwardedTest extends WebTestBase {

  @Test
  public void testXForwardSSL() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testXForwardSSLVariation2() throws Exception {
    router.allowForward(FORWARD).route("/").handler(rc -> {
      // in this case the legacy headers are not considered and will not overwrite the value
      assertFalse(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "http");
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testXForwardSSLVariation3() throws Exception {
    router.allowForward(X_FORWARD).route("/").handler(rc -> {
      // this is variation of the previous test but in this case it should assert true
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testForwardedProto() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("Forwarded", "proto=https");
  }

  @Test
  public void testForwardedHostAlongWithXForwardSSL() throws Exception {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("Forwarded", "host=" + host, "X-Forwarded-Ssl", "On");
  }

  @Test
  public void testMultipleForwarded() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("Forwarded", "proto=https,proto=http");
  }

  @Test
  public void testForwardedProtoAlongWIthXForwardSSL() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "http");
      rc.end();
    });

    testRequest("Forwarded", "proto=http", "X-Forwarded-Ssl", "On");
  }

  @Test
  public void testForwardedHost() throws Exception {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("Forwarded", "host=" + host);
  }

  @Test
  public void testForwardedHostAndPort() throws Exception {
    String host = "vertx.io:1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("Forwarded", "host=" + host);
  }

  @Test
  public void testForwardedHostAndPortAndProto() throws Exception {
    String host = "vertx.io:1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("Forwarded", "host=" + host + ";proto=https");
  }

  @Test
  public void testXForwardedProto() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "https");
      rc.end();
    });

    testRequest("x-forwarded-proto", "https");
  }

  @Test
  public void testXForwardedProtoAlongWIthXForwardSSL() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "http");
      rc.end();
    });

    testRequest("x-FORWARDED-proto", "http", "X-Forwarded-Ssl", "On");
  }


  @Test
  public void testXForwardedHost() throws Exception {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("X-Forwarded-Host", host);
  }

  @Test
  public void testXForwardedHostAndPort() throws Exception {
    String host = "vertx.io:4321";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("X-Forwarded-Host", host);
  }

  @Test
  public void testXForwardedHostRemovesCommonPort() throws Exception {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + ":80");
  }

  @Test
  public void testXForwardedHostMultiple() throws Exception {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(rc.request().host(), host);
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + "," + "www.google.com");
  }

  @Test
  public void testXForwardedPort() throws Exception {
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().host().endsWith(":" + port));
      rc.end();
    });

    testRequest("X-Forwarded-Port", port);
  }

  @Test
  public void testXForwardedPortAndHost() throws Exception {
    String host = "vertx.io";
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().host().equals(host + ":" + port));
      rc.end();
    });

    testRequest("X-Forwarded-Host", host, "X-Forwarded-Port", port);
  }

  @Test
  public void testXForwardedPortAndHostWithPort() throws Exception {
    String host = "vertx.io";
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().host().equals(host + ":" + port));
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + ":4321", "X-Forwarded-Port", port);
  }

  @Test
  public void testIllegalPort() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().host().endsWith(":8080"));
      rc.end();
    });

    testRequest("X-Forwarded-Port", "illegal");
  }

  @Test
  public void testXForwardedFor() throws Exception {
    String host = "1.2.3.4";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().remoteAddress().host().equals(host));
      rc.end();
    });

    testRequest("X-Forwarded-For", host);
  }

  @Test
  public void testXForwardedForWithPort() throws Exception {
    String host = "1.2.3.4";
    int port = 1111;
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().remoteAddress().host().equals(host));
      assertTrue(rc.request().remoteAddress().port() == port);
      rc.end();
    });

    testRequest("X-Forwarded-For", host + ":" + port);
  }

  @Test
  public void testForwardedFor() throws Exception {
    String host = "1.2.3.4";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().remoteAddress().host().equals(host));
      rc.end();
    });

    testRequest("Forwarded", "for=" + host);
  }

  @Test
  public void testForwardedForIpv6() throws Exception {
    String host = "[2001:db8:cafe::17]";
    int port = 4711;

    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().remoteAddress().host().equals(host));
      assertTrue(rc.request().remoteAddress().port() == port);
      rc.end();
    });

    testRequest("Forwarded", "for=\"" + host + ":" + port + "\"");
  }

  @Test
  public void testNoForwarded() throws Exception {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().remoteAddress().host().equals("127.0.0.1"));
      assertTrue(rc.request().host().equals("localhost:8080"));
      assertTrue(rc.request().scheme().equals("http"));
      assertFalse(rc.request().isSSL());
      rc.end();
    });

    testRequest();
  }

  @Test
  public void testForwardedDisabled() throws Exception {
    router.allowForward(NONE).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals(rc.request().scheme(), "http");
      rc.end();
    });

    testRequest("Forwarded", "proto=https");
  }


  private void testRequest(String... headers) throws Exception {
    testRequest(HttpMethod.GET, "/", req -> {
      int i = 0;
      while (i < headers.length)
        req.putHeader(headers[i++], headers[i++]);
    }, 200, "OK", null);
  }

  @Test
  public void testForwardedForAndWebSocket() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    String host = "vertx.io:1234";
    String address = "1.2.3.4";
    router.allowForward(ALL).route("/ws").handler(rc -> {
      HttpServerRequest request = rc.request();
      MultiMap headers = request.headers();
      if (headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true)) {
        request
          .toWebSocket(onSuccess(socket -> {
            assertTrue(socket.host().equals(host));
            assertTrue(socket.isSsl());
            assertTrue(socket.remoteAddress().host().equals(address));
            latch.countDown();
          }));
      } else {
        fail("Expected websocket connection");
      }
    });

    client.webSocket(new WebSocketConnectOptions().setURI("/ws").addHeader("Forwarded", "host=" + host + ";proto=https" + ";for=" + address), onSuccess(e -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }

}
