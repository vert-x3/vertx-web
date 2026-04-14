/*
 * Copyright 2023 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.tests.impl;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.tests.WebTestBase2;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbsoluteUriTest extends WebTestBase2 {

  static Stream<Arguments> data() {
    return Stream.of(
      // absolute URI, no explicit authority, no query -> the authority in the URI is ignored
      Arguments.of("http://www.example.com:1234/some/path", null, null, "http://localhost:8080/some/path"),
      // absolute URI, no explicit authority, query present  -> the authority in the URI is ignored
      Arguments.of("http://www.example.com:1234/some/path", null, "a=1&b=2", "http://localhost:8080/some/path?a=1&b=2"),
      // absolute URI, same explicit authority, no query
      Arguments.of("http://www.example.com:1234/some/path", "www.example.com:1234", null, "http://www.example.com:1234/some/path"),
      // absolute URI, same explicit authority, query present
      Arguments.of("http://www.example.com:1234/some/path", "www.example.com:1234", "a=1&b=2", "http://www.example.com:1234/some/path?a=1&b=2"),
      // absolute URI, different explicit authority, no query
      Arguments.of("http://www.example.com:1234/some/path", "another-host.com:5678", null, "http://another-host.com:5678/some/path"),
      // absolute URI, different explicit authority, query present
      Arguments.of("http://www.example.com:1234/some/path", "another-host.com:5678", "a=1&b=2", "http://another-host.com:5678/some/path?a=1&b=2"),
      // relative URI, no query
      Arguments.of("/some/path", "www.example.com:1234", null, "http://www.example.com:1234/some/path"),
      // relative URI, query present
      Arguments.of("/some/path", "www.example.com:1234", "a=1&b=2", "http://www.example.com:1234/some/path?a=1&b=2")
    );
  }

  @ParameterizedTest(name = "target={0}, host={1}, query={2}")
  @MethodSource("data")
  public void testAbsoluteUri(String target, String authority, String query, String expectedAbsoluteUri) {
    String uri = target + (query != null ? "?" + query : "");

    router.route().handler(event -> {
      assertEquals(expectedAbsoluteUri, event.request().absoluteURI());
      assertEquals(uri, event.request().uri());
      event.response().end();
    });

    client.request(HttpMethod.GET, 8080, "localhost", uri)
      .compose(request -> {
        if (authority != null) {
          request.authority(HostAndPort.parseAuthority(authority, 80));
        }
        return request
          .send()
          .expecting(HttpResponseExpectation.SC_OK)
          .compose(HttpClientResponse::body);
      }).await();
  }
}
