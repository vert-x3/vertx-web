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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CORSHandlerTest extends WebTestBase2 {

  @Test
  public void testNullAllowedOrigin() {
    assertThrows(NullPointerException.class, () -> CorsHandler.create().addOriginWithRegex(null));
  }

  /*
  If there is no origin then its not a CORS request so the CORS handler should just call the next handler without
  adding any headers to the response
   */
  @Test
  public void testNotCORSRequest() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testAcceptAllAllowedOrigin() throws Exception {
    router.route().handler(CorsHandler.create());
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "*", null, null, null);
  }

  @Test
  public void testAcceptConstantOrigin() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://vertx.io", null, null, null);
  }

  @Test
  public void testAcceptConstantOriginDenied1() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://foo.io").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testAcceptConstantOriginDenied2() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    // Make sure the '.' doesn't match like a regex
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "fooxio").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testAcceptDotisAnyCharacter1() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertxxio").send(), 200, "OK");
    checkHeaders(resp, "http://vertxxio", null, null, null);
  }

  @Test
  public void testAcceptDotisAnyCharacter2() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://vertx.io", null, null, null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAcceptConstantOriginDeniedErrorHandler() throws Exception {
    Consumer<RoutingContext> handler = mock(Consumer.class);

    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io"));
    router.route().handler(context -> context.response().end());
    router.errorHandler(403, handler::accept);
    testRequest(webClient.get("/").putHeader("origin", "http://foo.io").send(), 403, "CORS Rejected - Invalid origin");
    verify(handler).accept(any());
  }

  @Test
  public void testAcceptPattern() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://.*\\.vertx\\.io"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://foo.vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://foo.vertx.io", null, null, null);
    resp = testRequest(webClient.get("/").putHeader("origin", "http://bar.vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://bar.vertx.io", null, null, null);
    resp = testRequest(webClient.get("/").putHeader("origin", "http://wibble.bar.vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://wibble.bar.vertx.io", null, null, null);
  }

  @Test
  public void testAcceptPatternDenied() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://.*\\.vertx\\.io"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://foo.vertx.com").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
    resp = testRequest(webClient.get("/").putHeader("origin", "http://barxvertxxio").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testPreflightSimple() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null);
  }

  @Test
  public void testPreflightAllowedHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> allowedHeaders = new LinkedHashSet<>(Arrays.asList("X-wibble", "X-blah"));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .putHeader("access-control-request-headers", String.join(", ", allowedHeaders))
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", "X-wibble,X-blah", null);
  }

  @Test
  public void testPreflightNoExposeHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> exposeHeaders = new LinkedHashSet<>(Arrays.asList("X-floob", "X-blurp"));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods).exposedHeaders(exposeHeaders));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    // Note expose headers header is never provided in response of pre-flight request
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null);
  }

  @Test
  public void testPreflightAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, "true", null);
  }

  @Test
  public void testPreflightAllowCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    // Make sure * isn't returned in access-control-allow-origin for credentials
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, "true", null);
  }

  @Test
  public void testPreflightMaxAge() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    int maxAge = 131233;
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods).maxAgeSeconds(maxAge));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "PUT,DELETE", null, null, null, String.valueOf(maxAge));
  }

  @Test
  public void testRealRequestAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://vertx.io", null, null, null, "true", null);
  }

  @Test
  public void testRealRequestCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://vertx.io", null, null, null, "true", null);
  }

  @Test
  public void testRealRequestCredentialsWildcard() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://vertx.io").send(), 200, "OK");
    checkHeaders(resp, "http://vertx.io", null, null, null, "true", null);
  }

  @Test
  public void testChaining() throws Exception {
    CorsHandler cors = CorsHandler.create();
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

  private void checkHeaders(HttpResponse<Buffer> resp, String accessControlAllowOrigin,
                            String accessControlAllowMethods, String accessControlAllowHeaders,
                            String accessControlExposeHeaders) {
    checkHeaders(resp, accessControlAllowOrigin, accessControlAllowMethods, accessControlAllowHeaders,
      accessControlExposeHeaders, null, null);
  }

  private void checkHeaders(HttpResponse<Buffer> resp, String accessControlAllowOrigin,
                            String accessControlAllowMethods, String accessControlAllowHeaders,
                            String accessControlExposeHeaders, String allowCredentials,
                            String maxAgeSeconds) {
    checkHeaders(resp, accessControlAllowOrigin, accessControlAllowMethods, accessControlAllowHeaders,
      accessControlExposeHeaders, allowCredentials, maxAgeSeconds, null);
  }

  private void checkHeaders(HttpResponse<Buffer> resp, String accessControlAllowOrigin,
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
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://example.com"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://example.com").send(), 200, "OK");
    assertEquals("origin", resp.getHeader("Vary"));
  }

  @Test
  public void testAppendsVaryHeaderForSpecificOriginsWhenVaryIsDefined() throws Exception {
    router.route().handler(context -> {
      context.response().putHeader("Vary", "Foo");
      context.next();
    });
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://example.com"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "http://example.com").send(), 200, "OK");
    assertEquals("Foo,origin", resp.getHeader("Vary"));
  }

  @Test
  public void testCanSpecifyAllowedHeaders() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://example.com").allowedHeader("header1").allowedHeader("header2"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://example.com")
      .putHeader("access-control-request-method", "POST")
      .putHeader("access-control-request-headers", "x-header-1, x-header-2")
      .send(), 204, "No Content");
    assertEquals("header1,header2", resp.getHeader("Access-Control-Allow-Headers"));
    assertNull(resp.getHeader("Vary"));
  }

  @Test
  public void testMirrorAllowedHeaders() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://example.com"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://example.com")
      .putHeader("access-control-request-method", "POST")
      .putHeader("access-control-request-headers", "x-header-1, x-header-2")
      .send(), 204, "No Content");
    assertEquals("x-header-1, x-header-2", resp.getHeader("Access-Control-Allow-Headers"));
    assertEquals("access-control-request-headers", resp.getHeader("Vary"));
  }

  @Test
  public void testMDNExample() throws Exception {
    router.route().handler(
      CorsHandler
        .create().addOriginWithRegex("http://foo.example")
        .allowedHeader("X-PINGOTHER")
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.OPTIONS)
        .maxAgeSeconds(86400)
    );
    router.route().handler(context -> context.response().end());

    // preflight
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://foo.example")
      .putHeader("access-control-request-method", "POST")
      .putHeader("access-control-request-headers", "X-PINGOTHER, Content-Type")
      .send(), 204, "No Content");
    assertEquals("http://foo.example", resp.getHeader("Access-Control-Allow-Origin"));
    assertEquals("POST,GET,OPTIONS", resp.getHeader("Access-Control-Allow-Methods"));
    assertEquals("X-PINGOTHER,Content-Type", resp.getHeader("access-control-allow-headers"));
    assertEquals("86400", resp.getHeader("access-control-max-age"));
    // real request
    resp = testRequest(webClient.post("/")
      .putHeader("origin", "http://foo.example")
      .putHeader("X-PINGOTHER", "pingother")
      .putHeader("Content-Type", "text/xml; charset=UTF-8")
      .send(), 200, "OK");
    assertEquals("http://foo.example", resp.getHeader("Access-Control-Allow-Origin"));
    assertEquals("origin", resp.getHeader("Vary"));
  }

  @Test
  public void testNotCORSRequestMultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create()
      .addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));

    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testAcceptConstantOriginMultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")  ));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "https://www.vertx.io").send(), 200, "OK");
    checkHeaders(resp, "https://www.vertx.io", null, null, null);
  }

  @Test
  public void testAcceptConstantOriginDenied1MultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "foo.io").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  public void testAcceptConstantOriginDenied2MultiOrigins() throws Exception {
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    // Make sure the '.' doesn't match like a regex
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "fooxio").send(), 403, "CORS Rejected - Invalid origin");
    checkHeaders(resp, null, null, null, null);
  }

  @Test
  @SuppressWarnings ("unchecked")
  public void testAcceptConstantOriginDeniedErrorHandlerMultiOrigin() throws Exception {
    Consumer<RoutingContext> handler = mock(Consumer.class);

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")));
    router.route().handler(context -> context.response().end());
    router.errorHandler(403, handler::accept);
    testRequest(webClient.get("/").putHeader("origin", "foo.io").send(), 403, "CORS Rejected - Invalid origin");
    verify(handler).accept(any());
  }

  @Test
  public void testPreflightSimpleMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null);
  }

  @Test
  public void testPreflightAllowedHeadersMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> allowedHeaders = new LinkedHashSet<>(Arrays.asList("X-wibble", "X-blah"));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .putHeader("access-control-request-headers", String.join(", ", allowedHeaders))
      .send(), 204, "No Content");
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", "X-wibble,X-blah", null);
  }

  @Test
  public void testPreflightNoExposeHeadersMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> exposeHeaders = new LinkedHashSet<>(Arrays.asList("X-floob", "X-blurp"));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).exposedHeaders(exposeHeaders));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    // Note expose headers header is never provided in response of pre-flight request
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null);
  }

  @Test
  public void testPreflightAllowCredentialsMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));

    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, "true", null);
  }

  @Test
  public void testPreflightAllowCredentialsNoWildcardOriginMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    // Make sure * isn't returned in access-control-allow-origin for credentials

    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, "true", null);
  }

  @Test
  public void testPreflightMaxAgeMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    int maxAge = 131233;
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).maxAgeSeconds(maxAge));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "https://www.vertx.io")
      .putHeader("access-control-request-method", "PUT,DELETE")
      .send(), 204, "No Content");
    checkHeaders(resp, "https://www.vertx.io", "PUT,DELETE", null, null, null, String.valueOf(maxAge));
  }

  @Test
  public void testRealRequestAllowCredentialsMultiOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.create().addOrigins(Arrays.asList("http://www.example.com", "https://www.vertx.io")).allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "https://www.vertx.io").send(), 200, "OK");
    checkHeaders(resp, "https://www.vertx.io", null, null, null, "true", null);
  }

  @Test
  public void testAcceptNullOrigin() throws Exception {
    router.route().handler(CorsHandler.create().addOrigin("*"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "null").send(), 200, "OK");
    checkHeaders(resp, "*", null, null, null);
  }

  @Test
  public void testPreflightAllowPrivateNetwork() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.*").allowedMethod(HttpMethod.GET).allowPrivateNetwork(true));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "GET")
      .putHeader("access-control-request-private-network", "true")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "GET", null, null, null, null, "true");
  }

  @Test
  public void testPreflightDenyPrivateNetwork() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("http://vertx.*").allowedMethod(HttpMethod.GET).allowPrivateNetwork(false));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.request(HttpMethod.OPTIONS, "/")
      .putHeader("origin", "http://vertx.io")
      .putHeader("access-control-request-method", "GET")
      .putHeader("access-control-request-private-network", "true")
      .send(), 204, "No Content");
    checkHeaders(resp, "http://vertx.io", "GET", null, null, null, null, null);
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

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

    resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .sendBuffer(Buffer.buffer("abc")), 413, "Request Entity Too Large");
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
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

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    String cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
    String orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    assertNotNull(cred);
    assertNotNull(orig);
    assertEquals("https://mydomain.org:3000", orig);
  }

  @Test
  public void testCORSSetupSingleRelativeOrigin() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addOriginWithRegex("https://.*:3000")
        .addOriginWithRegex("https://.*:9443")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    String cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
    String orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    assertNotNull(cred);
    assertNotNull(orig);
    assertEquals("https://mydomain.org:3000", orig);
  }

  @Test
  public void testCORSSetupMixedOrigin() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addOriginWithRegex("https://f.*")
        .addOrigin("https://foo")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://foo")
      .send(), 200, "OK");
    String cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
    String orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    String vary = resp.getHeader(HttpHeaders.VARY);
    assertNotNull(cred);
    assertNotNull(orig);
    assertNotNull(vary);
    assertEquals("https://foo", orig);
    assertEquals("origin", vary);

    resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://foobar")
      .send(), 200, "OK");
    cred = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
    orig = resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    vary = resp.getHeader(HttpHeaders.VARY);
    assertNotNull(cred);
    assertNotNull(orig);
    assertNotNull(vary);
    assertEquals("https://foobar", orig);
    assertEquals("origin", vary);
  }

  @Test
  public void testCORSSetupSingleOriginShouldNotHaveVary() throws Exception {

    router
      .route()
      .handler(CorsHandler.create()
        .addOrigin("https://mydomain.org:3000")
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertNull(resp.getHeader(HttpHeaders.VARY));
  }

  @Test
  public void testCORSSetupStarOriginShouldNotHaveVary() throws Exception {
    // when we allow any origin, the response is not dependent on it, so we tell caches not to consider origin in
    // the cache key
    router
      .route()
      .handler(CorsHandler.create()
        .allowCredentials(false)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    assertNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertNull(resp.getHeader(HttpHeaders.VARY));
  }

  @Test
  public void testCORSSetupStarOriginWithAllowCredentialsShouldHaveVary() throws Exception {
    // When allow credentials is set with any origin, the response is dependent on the origin
    router
      .route()
      .handler(CorsHandler.create()
        .allowCredentials(true)
        .allowedHeader("Content-Type")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedMethod(HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Allow-Origin"))
      .handler(BodyHandler.create().setBodyLimit(1))
      .handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.post("/")
      .putHeader("origin", "https://mydomain.org:3000")
      .send(), 200, "OK");
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    assertNotNull(resp.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    assertEquals("origin", resp.getHeader(HttpHeaders.VARY));
  }

  @Test
  public void testAcceptChromeExtensionOrigin() throws Exception {
    router.route().handler(CorsHandler.create().addOrigin("*"));
    router.route().handler(context -> context.response().end());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", "chrome-extension://gmbgaklkmjakoegficnlkhebmhkjfich").send(), 200, "OK");
    checkHeaders(resp, "*", null, null, null);
  }

  @Test
  public void testAcceptMozExtensionOrigin() throws Exception {
    router.route().handler(CorsHandler.create().addOriginWithRegex("moz-extension://.*"));
    router.route().handler(context -> context.response().end());
    String origin = "moz-extension://" + UUID.randomUUID();
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("origin", origin).send(), 200, "OK");
    checkHeaders(resp, origin, null, null, null);
  }
}
