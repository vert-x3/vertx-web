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
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.junit5.Checkpoint;
import io.vertx.test.core.TestUtils;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static io.vertx.ext.web.AllowForwardHeaders.*;

public class ForwardedTest extends WebTestBase {

  private NetClient netClient;

  @AfterEach
  void cleanup() {
    if (netClient != null) {
      netClient.close().await();
    }
  }

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
  public void testXForwardedForWithPortOutOfRange() {
    testForwardedForWithPortOutOfRange("X-Forwarded-For", "1.2.3.4:70000");
  }

  @Test
  public void testForwardedForWithPortOutOfRange() {
    testForwardedForWithPortOutOfRange("Forwarded", "for=\"1.2.3.4:70000\"");
  }

  @Test
  public void testXForwardedForWithNegativePort() {
    testForwardedForWithPortOutOfRange("X-Forwarded-For", "1.2.3.4:-1");
  }

  private void testForwardedForWithPortOutOfRange(String header, String value) {
    router.allowForward(ALL).route("/").handler(rc -> {
      HttpServerRequest request = rc.request();
      assertEquals("1.2.3.4", request.remoteAddress().host());
      assertEquals(request.connection().remoteAddress().port(), request.remoteAddress().port());
      rc.end();
    });

    testRequest(header, value);
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_16, disabledReason = "Domain sockets require JDK 16 or a native transport")
  public void testXForwardedForWithDomainSocket(@TempDir Path tempDir) {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals("1.2.3.4", rc.request().remoteAddress().host());
      assertEquals(0, rc.request().remoteAddress().port());
      rc.end();
    });

    // the remote address of a server bound on a domain socket has no port
    SocketAddress address = SocketAddress.domainSocketAddress(tempDir.resolve("forwarded.sock").toString());
    HttpServer domainSocketServer = vertx.createHttpServer().requestHandler(router);
    domainSocketServer.listen(address).await();
    try {
      client.request(new RequestOptions().setServer(address).setHost("localhost").setPort(8080).setURI("/"))
        .compose(request -> request
          .putHeader("X-Forwarded-For", "1.2.3.4")
          .send()
          .expecting(HttpResponseExpectation.SC_OK)
          .compose(HttpClientResponse::body))
        .await();
    } finally {
      domainSocketServer.close().await();
    }
  }

  @Test
  public void testXForwardedForWithWhitespace() {
    router.allowForward(ALL).route("/").handler(rc -> {
      assertEquals("1.2.3.4", rc.request().remoteAddress().host());
      rc.end();
    });

    testRequest("X-Forwarded-For", "1.2.3.4 , 5.6.7.8");
  }

  @Test
  public void testXForwardedForEmpty() {
    testMalformedValueIsIgnored("X-Forwarded-For", "");
  }

  @Test
  public void testXForwardedForEmptyFirstElement() {
    testMalformedValueIsIgnored("X-Forwarded-For", ", 5.6.7.8");
  }

  @Test
  public void testXForwardedForWithoutElement() {
    testMalformedValueIsIgnored("X-Forwarded-For", ",");
  }

  @Test
  public void testXForwardedProtoWithoutElement() {
    testMalformedValueIsIgnored("X-Forwarded-Proto", ",");
  }

  @Test
  public void testXForwardedHostWithoutElement() {
    testMalformedValueIsIgnored("X-Forwarded-Host", ",");
  }

  @Test
  public void testXForwardedHostInvalid() {
    testMalformedValueIsIgnored("X-Forwarded-Host", "not a host");
  }

  @Test
  public void testXForwardedHostWithPortOutOfRange() {
    testMalformedValueIsIgnored("X-Forwarded-Host", "localhost:70000");
  }

  @Test
  public void testXForwardedPortWithoutElement() {
    testMalformedValueIsIgnored("X-Forwarded-Port", ",");
  }

  @Test
  public void testXForwardedPortOutOfRange() {
    testMalformedValueIsIgnored("X-Forwarded-Port", "70000");
  }

  @Test
  public void testForwardedWithoutElement() {
    testMalformedValueIsIgnored("Forwarded", ",");
  }

  @Test
  public void testForwardedHostInvalid() {
    testMalformedValueIsIgnored("Forwarded", "host=\"not a host\"");
  }

  @Test
  public void testForwardedForBlank() {
    testMalformedValueIsIgnored("Forwarded", "for= ;by=203.0.113.43");
  }

  @Test
  public void testForwardedProtoBlank() {
    testMalformedValueIsIgnored("Forwarded", "proto= ;host=localhost:8080");
  }

  private void testMalformedValueIsIgnored(String header, String value) {
    router.allowForward(ALL).route("/").handler(rc -> {
      HttpServerRequest request = rc.request();
      assertEquals("http", request.scheme());
      assertEquals("localhost", request.authority().host());
      assertEquals(8080, request.authority().port());
      assertEquals(request.connection().remoteAddress(), request.remoteAddress());
      rc.end();
    });

    testRequest(header, value);
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
    netClient = vertx.createNetClient();
    netClient.connect(8080, "localhost").onComplete(TestUtils.onSuccess(so -> {
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
