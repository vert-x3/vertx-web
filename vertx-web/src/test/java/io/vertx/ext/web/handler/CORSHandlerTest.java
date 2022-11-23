/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CORSHandlerTest extends WebTestBase {

  @Test(expected = NullPointerException.class)
  public void testNullAllowedOrigin() {
    CorsHandler.create(null);
  }

  /*
  If there is no origin then its not a CORS request so the CORS handler should just call the next handler without
  adding any headers to the response
   */
  @Test
  public void testNotCORSRequest() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> checkHeaders(resp, null, null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptAllAllowedOrigin() throws Exception {
    router.route().handler(CorsHandler.create("*"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "*", null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOrigin() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "http://vertx.io", null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOriginDenied1() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://foo.io"), resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptConstantOriginDenied2() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> {
      // Make sure the '.' doesn't match like a regex
      req.headers().add("origin", "fooxio");
    }, resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptDotisAnyCharacter1() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertxxio"), resp -> checkHeaders(resp, "http://vertxxio", null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptDotisAnyCharacter2() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "http://vertx.io", null, null, null), 200, "OK", null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAcceptConstantOriginDeniedErrorHandler() throws Exception {
    Consumer<RoutingContext> handler = mock(Consumer.class);

    router.route().handler(CorsHandler.create("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    router.errorHandler(403, handler::accept);
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://foo.io"), resp -> verify(handler).accept(any()), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptPattern() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.create("http://.*\\.vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://foo.vertx.io"), resp -> checkHeaders(resp, "http://foo.vertx.io", null, null, null), 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://bar.vertx.io"), resp -> checkHeaders(resp, "http://bar.vertx.io", null, null, null), 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://wibble.bar.vertx.io"), resp -> checkHeaders(resp, "http://wibble.bar.vertx.io", null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptPatternDenied() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.create("http://.*\\.vertx\\.io"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://foo.vertx.com"), resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://barxvertxxio"), resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testPreflightSimple() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null), 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowedHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> allowedHeaders = new LinkedHashSet<>(Arrays.asList("X-wibble", "X-blah"));
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
      req.headers().add("access-control-request-headers", allowedHeaders);
    }, resp -> checkHeaders(resp, "http://vertx.io", "PUT,DELETE", "X-wibble,X-blah", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightNoExposeHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> exposeHeaders = new LinkedHashSet<>(Arrays.asList("X-floob", "X-blurp"));
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods).exposedHeaders(exposeHeaders));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      // Note expose headers header is never provided in response of pre-flight request
      checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, "true", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    // Make sure * isn't returned in access-control-allow-origin for credentials
    router.route().handler(CorsHandler.create("http://vertx.*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, "true", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightMaxAge() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    int maxAge = 131233;
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods).maxAgeSeconds(maxAge));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, null, String.valueOf(maxAge)), 204, "No Content", null);
  }

  @Test
  public void testRealRequestAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create("http://vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "http://vertx.io", null, null, null, "true", null), 200, "OK", null);
  }

  @Test
  public void testRealRequestCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create("http://vertx.*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "http://vertx.io", null, null, null, "true", null), 200, "OK", null);
  }

  @Test
  public void testRealRequestCredentialsWildcard() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create("*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "http://vertx.io"), resp -> checkHeaders(resp, "http://vertx.io", null, null, null, "true", null), 200, "OK", null);
  }

  @Test
  public void testChaining() throws Exception {
    CorsHandler cors = CorsHandler.create("*");
    assertNotNull(cors);
    assertSame(cors, cors.allowedMethod(HttpMethod.POST));
    assertSame(cors, cors.allowedMethod(HttpMethod.DELETE));
    assertSame(cors, cors.allowedMethods(new HashSet<>()));
    assertSame(cors, cors.allowedHeader("X-foo"));
    assertSame(cors, cors.allowedHeader("X-bar"));
    assertSame(cors, cors.allowedHeaders(new HashSet<>()));
    assertSame(cors, cors.exposedHeader("X-wibble"));
    assertSame(cors, cors.exposedHeader("X-blah"));
    assertSame(cors, cors.exposedHeaders(new HashSet<>()));
  }

  private void checkHeaders(HttpClientResponse resp, String accessControlAllowOrigin,
                            String accessControlAllowMethods, String accessControlAllowHeaders,
                            String accessControlExposeHeaders) {
    checkHeaders(resp, accessControlAllowOrigin, accessControlAllowMethods, accessControlAllowHeaders,
      accessControlExposeHeaders, null, null);
  }

  private void checkHeaders(HttpClientResponse resp, String accessControlAllowOrigin,
                            String accessControlAllowMethods, String accessControlAllowHeaders,
                            String accessControlExposeHeaders, String allowCredentials,
                            String maxAgeSeconds) {
    checkHeaders(resp, accessControlAllowOrigin, accessControlAllowMethods, accessControlAllowHeaders,
      accessControlExposeHeaders, allowCredentials, maxAgeSeconds, null);
  }

  private void checkHeaders(HttpClientResponse resp, String accessControlAllowOrigin,
                            String accessControlAllowMethods, String accessControlAllowHeaders,
                            String accessControlExposeHeaders, String allowCredentials,
                            String maxAgeSeconds, String privateNetwork) {
    assertEquals(accessControlAllowOrigin, resp.headers().get("access-control-allow-origin"));
    assertEquals(accessControlAllowMethods, resp.headers().get("access-control-allow-methods"));
    assertEquals(accessControlAllowHeaders, resp.headers().get("access-control-allow-headers"));
    assertEquals(accessControlExposeHeaders, resp.headers().get("access-control-expose-headers"));
    assertEquals(allowCredentials, resp.headers().get("access-control-allow-credentials"));
    assertEquals(maxAgeSeconds, resp.headers().get("access-control-max-age"));
    assertEquals(privateNetwork, resp.headers().get("access-control-allow-private-network"));
  }

  @Test
  public void testIncludesVaryHeaderForSpecificOrigins() throws Exception {
    router.route().handler(CorsHandler.create("http://example.com"));
    router.route().handler(context -> context.response().end());
    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.headers().add("origin", "http://example.com"),
      resp -> {
        assertEquals("origin", resp.getHeader("Vary"));
      }, 200, "OK", null);
  }

  @Test
  public void testAppendsVaryHeaderForSpecificOriginsWhenVaryIsDefined() throws Exception {
    router.route().handler(context -> {
      context.response().putHeader("Vary", "Foo");
      context.next();
    });
    router.route().handler(CorsHandler.create("http://example.com"));
    router.route().handler(context -> context.response().end());
    testRequest(
      HttpMethod.GET,
      "/",
      req -> req.headers().add("origin", "http://example.com"),
      resp -> {
        assertEquals("Foo,origin", resp.getHeader("Vary"));
      }, 200, "OK", null);
  }

  @Test
  public void testCanSpecifyAllowedHeaders() throws Exception {
    router.route().handler(CorsHandler.create("http://example.com").allowedHeader("header1").allowedHeader("header2"));
    router.route().handler(context -> context.response().end());
    testRequest(
      HttpMethod.OPTIONS,
      "/",
      req -> req.headers()
        .add("origin", "http://example.com")
        .add("access-control-request-method", "POST")
        .add("access-control-request-headers", "x-header-1, x-header-2"),
      resp -> {
        assertEquals("header1,header2", resp.getHeader("Access-Control-Allow-Headers"));
        assertNull(resp.getHeader("Vary"));
      }, 204, "No Content", null);
  }

  @Test
  public void testMirrorAllowedHeaders() throws Exception {
    router.route().handler(CorsHandler.create("http://example.com"));
    router.route().handler(context -> context.response().end());
    testRequest(
      HttpMethod.OPTIONS,
      "/",
      req -> req.headers()
        .add("origin", "http://example.com")
        .add("access-control-request-method", "POST")
        .add("access-control-request-headers", "x-header-1, x-header-2"),
      resp -> {
        assertEquals("x-header-1, x-header-2", resp.getHeader("Access-Control-Allow-Headers"));
        assertEquals("access-control-request-headers", resp.getHeader("Vary"));
      }, 204, "No Content", null);
  }

  @Test
  public void testMDNExample() throws Exception {
    router.route().handler(
      CorsHandler
        .create("http://foo.example")
        .allowedHeader("X-PINGOTHER")
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.OPTIONS)
        .maxAgeSeconds(86400)
    );
    router.route().handler(context -> context.response().end());

    // preflight
    testRequest(
      HttpMethod.OPTIONS,
      "/",
      req -> req.headers()
        .add("origin", "http://foo.example")
        .add("access-control-request-method", "POST")
        .add("access-control-request-headers", "X-PINGOTHER, Content-Type"),
      resp -> {
        assertEquals("http://foo.example", resp.getHeader("Access-Control-Allow-Origin"));
        assertEquals("POST,GET,OPTIONS", resp.getHeader("Access-Control-Allow-Methods"));
        assertEquals("X-PINGOTHER,Content-Type", resp.getHeader("access-control-allow-headers"));
        assertEquals("86400", resp.getHeader("access-control-max-age"));
      }, 204, "No Content", null);
    // real request
    testRequest(
      HttpMethod.POST,
      "/",
      req -> req.headers()
        .add("origin", "http://foo.example")
        .add("X-PINGOTHER", "pingother")
        .add("Content-Type", "text/xml; charset=UTF-8"),
      resp -> {
        assertEquals("http://foo.example", resp.getHeader("Access-Control-Allow-Origin"));
        assertEquals("origin", resp.getHeader("Vary"));
      }, 200, "OK", null);
  }

  @Test
  public void testNotCORSRequestMultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create()
      .addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));

    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> checkHeaders(resp, null, null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOriginMultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")  ));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "https://www.vertx.io"), resp -> checkHeaders(resp, "https://www.vertx.io", null, null, null), 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOriginDenied1MultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "foo.io"), resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptConstantOriginDenied2MultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> {
      // Make sure the '.' doesn't match like a regex
      req.headers().add("origin", "fooxio");
    }, resp -> checkHeaders(resp, null, null, null, null), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  @SuppressWarnings ("unchecked")
  public void testAcceptConstantOriginDeniedErrorHandlerMultiOrigin() throws Exception {
    Consumer<RoutingContext> handler = mock(Consumer.class);

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    router.errorHandler(403, handler::accept);
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "foo.io"), resp -> verify(handler).accept(any()), 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testPreflightSimpleMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null), 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowedHeadersMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> allowedHeaders = new LinkedHashSet<>(Arrays.asList("X-wibble", "X-blah"));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
      req.headers().add("access-control-request-headers", allowedHeaders);
    }, resp -> checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", "X-wibble,X-blah", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightNoExposeHeadersMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> exposeHeaders = new LinkedHashSet<>(Arrays.asList("X-floob", "X-blurp"));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).exposedHeaders(exposeHeaders));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      // Note expose headers header is never provided in response of pre-flight request
      checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentialsMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));

    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, "true", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentialsNoWildcardOriginMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    // Make sure * isn't returned in access-control-allow-origin for credentials

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, "true", null), 204, "No Content", null);
  }

  @Test
  public void testPreflightMaxAgeMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    int maxAge = 131233;
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).maxAgeSeconds(maxAge));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "https://www.vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, null, String.valueOf(maxAge)), 204, "No Content", null);
  }

  @Test
  public void testRealRequestAllowCredentialsMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "https://www.vertx.io"), resp -> checkHeaders(resp, "https://www.vertx.io", null, null, null, "true", null), 200, "OK", null);
  }

  @Test
  public void testAcceptNullOrigin() throws Exception {
    router.route().handler(CorsHandler.create().addOrigin("*"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", req -> req.headers().add("origin", "null"), resp -> checkHeaders(resp, "*", null, null, null), 200, "OK", null);
  }

  @Test
  public void testPreflightAllowPrivateNetwork() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx.*").allowedMethod(HttpMethod.GET).allowPrivateNetwork(true));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "GET");
      req.headers().add("access-control-request-private-network", "true");
    }, resp -> checkHeaders(resp, "http://vertx.io", "GET", null, null, null, null, "true"), 204, "No Content", null);
  }

  @Test
  public void testPreflightDenyPrivateNetwork() throws Exception {
    router.route().handler(CorsHandler.create("http://vertx.*").allowedMethod(HttpMethod.GET).allowPrivateNetwork(false));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "http://vertx.io");
      req.headers().add("access-control-request-method", "GET");
      req.headers().add("access-control-request-private-network", "true");
    }, resp -> checkHeaders(resp, "http://vertx.io", "GET", null, null, null, null, null), 204, "No Content", null);
  }

  @Test
  public void testCORSSetup() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addOrigin("https://mydomain.org:3000")
        .addOrigin("https://mydomain.org:9443")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(context -> context.response().end());

    testRequest(HttpMethod.POST, "/", req -> {
      req.headers().add("origin", "https://mydomain.org:3000");
    }, resp -> {
      assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
      assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/", req -> {
      req.headers().add("origin", "https://mydomain.org:3000");
      req.send("abc");
    }, resp -> {
      assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
      assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }, 413, "Request Entity Too Large", null);
  }

  @Test
  public void testCORSSetupSingleOrigin() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addOrigin("https://mydomain.org:3000")
        .addOrigin("https://mydomain.org:9443")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(context -> context.response().end());

    testRequest(HttpMethod.POST, "/", req -> {
      req.headers().add("origin", "https://mydomain.org:3000");
    }, resp -> {
      String cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
      String orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
      assertNotNull(cred);
      assertNotNull(orig);
      assertEquals("https://mydomain.org:3000", orig);
    }, 200, "OK", null);
  }

  @Test
  public void testCORSSetupSingleRelativeOrigin() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addRelativeOrigin("https://.*:3000")
        .addRelativeOrigin("https://.*:9443")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(context -> context.response().end());

    testRequest(HttpMethod.POST, "/", req -> {
      req.headers().add("origin", "https://mydomain.org:3000");
    }, resp -> {
      String cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
      String orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
      assertNotNull(cred);
      assertNotNull(orig);
      assertEquals("https://mydomain.org:3000", orig);
    }, 200, "OK", null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCORSSetupMixedOrigin() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addRelativeOrigin("https://.*:3000")
        .addOrigin("https://foo:9443")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(context -> context.response().end());
  }
}
