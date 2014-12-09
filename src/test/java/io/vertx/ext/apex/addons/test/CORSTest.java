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

package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.CorsHandler;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CORSTest extends ApexTestBase {

  @Test(expected=NullPointerException.class)
  public void testNullAllowedOrigin() throws Exception {
    CorsHandler.cors(null);
  }


  @Test
  public void testAcceptAllAllowedOrigin() throws Exception {
    router.route().handler(CorsHandler.cors("*"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertx.io");
    }, resp -> {
      checkHeaders(resp, "*", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptAllAllowedOriginNoOriginHeader() throws Exception {
    router.route().handler(CorsHandler.cors("*"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      checkHeaders(resp, "*", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOrigin() throws Exception {
    router.route().handler(CorsHandler.cors("vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertx.io");
    }, resp -> {
      checkHeaders(resp, "vertx.io", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptConstantOriginDenied1() throws Exception {
    router.route().handler(CorsHandler.cors("vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "foo.io");
    }, resp -> {
      checkHeaders(resp, null, null, null, null);
    }, 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptConstantOriginDenied2() throws Exception {
    router.route().handler(CorsHandler.cors("vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      // Make sure the '.' doesn't match like a regex
      req.headers().add("origin", "fooxio");
    }, resp -> {
      checkHeaders(resp, null, null, null, null);
    }, 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptConstantOriginNoOriginHeaderDenied() throws Exception {
    router.route().handler(CorsHandler.cors("vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      checkHeaders(resp, null, null, null, null);
    }, 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testAcceptDotisAnyCharacter1() throws Exception {
    router.route().handler(CorsHandler.cors("vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertxxio");
    }, resp -> {
      checkHeaders(resp, "vertxxio", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptDotisAnyCharacter2() throws Exception {
    router.route().handler(CorsHandler.cors("vertx.io")); // dot matches any character - watch out!
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertx.io");
    }, resp -> {
      checkHeaders(resp, "vertx.io", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptPattern() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.cors(".*\\.vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "foo.vertx.io");
    }, resp -> {
      checkHeaders(resp, "foo.vertx.io", null, null, null);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "bar.vertx.io");
    }, resp -> {
      checkHeaders(resp, "bar.vertx.io", null, null, null);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "wibble.bar.vertx.io");
    }, resp -> {
      checkHeaders(resp, "wibble.bar.vertx.io", null, null, null);
    }, 200, "OK", null);
  }

  @Test
  public void testAcceptPatternDenied() throws Exception {
    // Any subdomains of vertx.io
    router.route().handler(CorsHandler.cors(".*\\.vertx\\.io"));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "foo.vertx.com");
    }, resp -> {
      checkHeaders(resp, null, null, null, null);
    }, 403, "CORS Rejected - Invalid origin", null);
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "barxvertxxio");
    }, resp -> {
      checkHeaders(resp, null, null, null, null);
    }, 403, "CORS Rejected - Invalid origin", null);
  }

  @Test
  public void testPreflightSimple() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      checkHeaders(resp, "vertx.io", "PUT,DELETE", null, null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowedHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> allowedHeaders = new LinkedHashSet<>(Arrays.asList("X-wibble", "X-blah"));
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods).allowedHeaders(allowedHeaders));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
      req.headers().add("access-control-request-headers", allowedHeaders);
    }, resp -> {
      checkHeaders(resp, "vertx.io", "PUT,DELETE", "X-wibble,X-blah", null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightNoExposeHeaders() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    Set<String> exposeHeaders = new LinkedHashSet<>(Arrays.asList("X-floob", "X-blurp"));
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods).exposedHeaders(exposeHeaders));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      // Note expose headers header is never provided in response of pre-flight request
      checkHeaders(resp, "vertx.io", "PUT,DELETE", null, null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      checkHeaders(resp, "vertx.io", "PUT,DELETE", null, null, "true", null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightAllowCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    // Make sure * isn't returned in access-control-allow-origin for credentials
    router.route().handler(CorsHandler.cors("*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      checkHeaders(resp, "vertx.io", "PUT,DELETE", null, null, "true", null);
    }, 204, "No Content", null);
  }

  @Test
  public void testPreflightMaxAge() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    int maxAge = 131233;
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods).maxAgeSeconds(maxAge));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.OPTIONS, "/", req -> {
      req.headers().add("origin", "vertx.io");
      req.headers().add("access-control-request-method", "PUT,DELETE");
    }, resp -> {
      checkHeaders(resp, "vertx.io", "PUT,DELETE", null, null, null, String.valueOf(maxAge));
    }, 204, "No Content", null);
  }

  @Test
  public void testRealRequestAllowCredentials() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.cors("vertx\\.io").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertx.io");
    }, resp -> {
      checkHeaders(resp, "vertx.io", null, null, null, "true", null);
    }, 200, "OK", null);
  }

  @Test
  public void testRealRequestCredentialsNoWildcardOrigin() throws Exception {
    Set<HttpMethod> allowedMethods = new LinkedHashSet<>(Arrays.asList(HttpMethod.PUT, HttpMethod.DELETE));
    router.route().handler(CorsHandler.cors("*").allowedMethods(allowedMethods).allowCredentials(true));
    router.route().handler(context -> {
      context.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().add("origin", "vertx.io");
    }, resp -> {
      checkHeaders(resp, "vertx.io", null, null, null, "true", null);
    }, 200, "OK", null);
  }

  @Test
  public void testChaining() throws Exception {
    CorsHandler cors = CorsHandler.cors("*");
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
    assertSame(cors, cors.allowCredentials(true));
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
    assertEquals(accessControlAllowOrigin, resp.headers().get("access-control-allow-origin"));
    assertEquals(accessControlAllowMethods, resp.headers().get("access-control-allow-methods"));
    assertEquals(accessControlAllowHeaders, resp.headers().get("access-control-allow-headers"));
    assertEquals(accessControlExposeHeaders, resp.headers().get("access-control-expose-headers"));
    assertEquals(allowCredentials, resp.headers().get("access-control-allow-credentials"));
    assertEquals(maxAgeSeconds, resp.headers().get("access-control-max-age"));
  }

}
