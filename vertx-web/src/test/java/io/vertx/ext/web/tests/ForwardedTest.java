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

package io.vertx.ext.web.tests;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.net.NetClient;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.junit5.Checkpoint;
import io.vertx.test.core.TestUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import static io.vertx.ext.web.AllowForwardHeaders.*;

public class ForwardedTest extends WebTestBase {

  @Test
  public void testXForwardSSL() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testXForwardSSLVariation2() {
    router.allowForward(FORWARD).route("/").handler(rc -> {
      // in this case the legacy headers are not considered and will not overwrite the value
      assertFalse(rc.request().isSSL());
      assertEquals("http", rc.request().scheme());
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testXForwardSSLVariation3() {
    router.allowForward(X_FORWARD).route("/").handler(rc -> {
      // this is variation of the previous test but in this case it should assert true
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("X-Forwarded-Ssl", "On");
  }

  @Test
  public void testXForwardedForIpv6() {
    String host = "[2001:db8:cafe::17]";
    int port = 4711;

    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      assertEquals(port, rc.request().remoteAddress().port());
      rc.end();
    });

    testRequest("X-Forwarded-For", host + ":" + port);
  }

  @Test
  public void testXForwardedForIpv6NoPort() {
    String host = "[2001:db8:cafe::17]";

    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("X-Forwarded-For", host);
  }

  @Test
  public void testXForwardedForRawIpv6NoPort() {
    // Make sure this is not seen as a host:port
    String host = "2001:db8:85a3:8d3:1319:8a2e:370:9c82";

    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("X-Forwarded-For", host);
  }

  @Test
  public void testForwardedProto() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "proto=https");
  }

  @Test
  public void testForwardedHostAlongWithXForwardSSL() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "host=" + host, "X-Forwarded-Ssl", "On");
  }

  @Test
  public void testForwardedHostAlongWithXForwardSSLWithUppercase() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "Host=" + host, "X-Forwarded-Ssl", "On");
  }

  @Test
  public void testMultipleForwarded() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "proto=https,proto=http");
  }

  @Test
  public void testMultipleForwardedWithUppercase() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "Proto=https,Proto=http");
  }

  @Test
  public void testForwardedProtoAlongWIthXForwardSSL() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals("http", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "proto=http", "X-Forwarded-Ssl", "On");
  }

  @Test
  public void testForwardedHost() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("Forwarded", "host=" + host);
  }

  @Test
  public void testForwardedHostWithUppercase() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().host());
      rc.end();
    });

    testRequest("Forwarded", "Host=" + host);
  }

  @Test
  public void testForwardedHostAndPort() {
    String host = "vertx.io:1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("Forwarded", "host=" + host);
  }

  @Test
  public void testForwardedHostAndPortAndProto() {
    String host = "vertx.io:1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "host=" + host + ";proto=https");
  }

  @Test
  public void testForwardedHostAndPortAndProtoWithUppercase() {
    String host = "vertx.io:1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "Host=" + host + ";Proto=https");
  }

  @Test
  public void testXForwardedProto() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().isSSL());
      assertEquals("https", rc.request().scheme());
      rc.end();
    });

    testRequest("x-forwarded-proto", "https");
  }

  @Test
  public void testXForwardedProtoAlongWIthXForwardSSL() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals("http", rc.request().scheme());
      rc.end();
    });

    testRequest("x-FORWARDED-proto", "http", "X-Forwarded-Ssl", "On");
  }


  @Test
  public void testXForwardedHost() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host);
  }

  @Test
  public void testXForwardedHostAndPort() {
    String host = "vertx.io:4321";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host);
  }

  @Test
  public void testXForwardedHostRemovesCommonPort() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + ":80");
  }

  @Test
  public void testXForwardedHostMultiple() {
    String host = "vertx.io";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + "," + "www.google.com");
  }

  @Test
  public void testXForwardedPort() {
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().authority().toString().endsWith(":" + port));
      rc.end();
    });

    testRequest("X-Forwarded-Port", port);
  }

  @Test
  public void testXForwardedPortAndHost() {
    String host = "vertx.io";
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host + ":" + port, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host, "X-Forwarded-Port", port);
  }

  @Test
  public void testXForwardedPortAndHostWithPort() {
    String host = "vertx.io";
    String port = "1234";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host + ":" + port, rc.request().authority().toString());
      rc.end();
    });

    testRequest("X-Forwarded-Host", host + ":4321", "X-Forwarded-Port", port);
  }

  @Test
  public void testIllegalPort() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertTrue(rc.request().authority().toString().endsWith(":8080"));
      rc.end();
    });

    testRequest("X-Forwarded-Port", "illegal");
  }

  @Test
  public void testXForwardedFor() {
    String host = "1.2.3.4";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("X-Forwarded-For", host);
  }

  @Test
  public void testXForwardedForWithPort() {
    String host = "1.2.3.4";
    int port = 1111;
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      assertEquals(port, rc.request().remoteAddress().port());
      rc.end();
    });

    testRequest("X-Forwarded-For", host + ":" + port);
  }

  @Test
  public void testForwardedFor() {
    String host = "1.2.3.4";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("Forwarded", "for=" + host);
  }

  @Test
  public void testForwardedForWithUpperCase() {
    String host = "1.2.3.4";
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("Forwarded", "For=" + host);
  }

  @Test
  public void testForwardedForIpv6() {
    String host = "[2001:db8:cafe::17]";
    int port = 4711;

    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals(host, rc.request().remoteAddress().host());
      assertEquals(port, rc.request().remoteAddress().port());
      rc.end();
    });

    testRequest("Forwarded", "for=\"" + host + ":" + port + "\"");
  }

  @Test
  public void testNoneMissingHostHeader(Checkpoint done) {
    testMissingHostHeader(done, router.allowForward(NONE).route("/"));
  }

  @Test
  public void testAllMissingHostHeader(Checkpoint done) {
    testMissingHostHeader(done, router.allowForward(ALL).route("/"));
  }

  @Test
  public void testForwardMissingHostHeader(Checkpoint done) {
    testMissingHostHeader(done, router.allowForward(FORWARD).route("/"));
  }

  @Test
  public void testXForwardMissingHostHeader(Checkpoint done) {
    testMissingHostHeader(done, router.allowForward(X_FORWARD).route("/"));
  }

  @Test
  public void testMissingHostHeader(Checkpoint done) {
    testMissingHostHeader(done, router.allowForward(ALL).route("/"));
  }

  private void testMissingHostHeader(Checkpoint done, Route route) {

    route.handler(rc -> {
      assertNull(rc.request().authority());
      rc.end();
    });

    NetClient tcpClient = vertx.createNetClient();
    tcpClient.connect(8080, "localhost").onComplete(TestUtils.onSuccess(so -> {
      so.write("GET / HTTP/1.1\r\n\r\n");
      so.handler(buff -> {
        done.flag();
      });
    }));
  }

  @Test
  public void testNoForwarded() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals("127.0.0.1", rc.request().remoteAddress().host());
      assertEquals("localhost:8080", rc.request().authority().toString());
      assertEquals("http", rc.request().scheme());
      assertFalse(rc.request().isSSL());
      rc.end();
    });

    testRequest();
  }

  @Test
  public void testForwardedDisabled() {
    router.allowForward(NONE).route("/").handler(rc -> {
      assertFalse(rc.request().isSSL());
      assertEquals("http", rc.request().scheme());
      rc.end();
    });

    testRequest("Forwarded", "proto=https");
  }


  private void testRequest(String... headers) {
    HttpRequest<Buffer> req = webClient.get("/");
    int i = 0;
    while (i < headers.length)
      req.putHeader(headers[i++], headers[i++]);
    testRequest(req, 200, "OK");
  }

  @Test
  public void testForwardedForAndWebSocket(Checkpoint done) {
    String host = "vertx.io:1234";
    String address = "1.2.3.4";
    router.allowForward(ALL).route("/ws").handler(rc -> {
      HttpServerRequest request = rc.request();
      assertTrue(request.canUpgradeToWebSocket());
      request
        .toWebSocket().onComplete(TestUtils.onSuccess(socket -> {
          assertEquals(host, socket.authority().toString());
          assertTrue(socket.isSsl());
          assertEquals(address, socket.remoteAddress().host());
          done.flag();
        }));
    });

    wsClient.connect(new WebSocketConnectOptions().setURI("/ws").addHeader("Forwarded", "host=" + host + ";proto=https" + ";for=" + address)).await();
  }

  @Test
  public void testForwardedForAndWebSocketWithUppercase(Checkpoint done) {
    String host = "vertx.io:1234";
    String address = "1.2.3.4";
    router.allowForward(ALL).route("/ws").handler(rc -> {
      HttpServerRequest request = rc.request();
      assertTrue(request.canUpgradeToWebSocket());
      request
        .toWebSocket()
        .onComplete(TestUtils.onSuccess(socket -> {
          assertEquals(host, socket.authority().toString());
          assertTrue(socket.isSsl());
          assertEquals(address, socket.remoteAddress().host());
          done.flag();
        }));
    });

    wsClient.connect(new WebSocketConnectOptions().setURI("/ws").addHeader("Forwarded", "Host=" + host + ";Proto=https" + ";For=" + address)).await();
  }
}
