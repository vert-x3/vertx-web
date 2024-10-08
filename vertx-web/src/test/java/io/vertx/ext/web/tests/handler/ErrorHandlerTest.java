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
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.tests.WebTestBase;

import org.junit.Test;

import static io.vertx.ext.web.common.WebEnvironment.SYSTEM_PROPERTY_NAME;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ErrorHandlerTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().failureHandler(ErrorHandler.create(vertx));
  }

  @Test
  public void testFailWithStatusCodeSetContentTypeTextHtml() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
      rc.fail(statusCode);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkHtmlResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeSetContentTypeApplicationJson() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      rc.fail(statusCode);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkJsonResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeSetContentTypeTextPlain() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
      rc.fail(statusCode);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkTextResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeInferContentTypeTextHtml() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> rc.fail(statusCode));
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("accept", "text/html"), resp -> resp.bodyHandler(buff -> {
      checkHtmlResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeInferContentTypeApplicationJson() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> rc.fail(statusCode));
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("accept", "application/json"), resp -> resp.bodyHandler(buff -> {
      checkJsonResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeInferContentTypeTextPlain() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> rc.fail(statusCode));
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("accept", "text/plain"), resp -> resp.bodyHandler(buff -> {
      checkTextResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithStatusCodeDefaultContentType() throws Exception {
    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> rc.fail(statusCode));
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkTextResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithException() throws Exception {
    System.setProperty(SYSTEM_PROPERTY_NAME, "dev");
    router
      // clear the previous setup
      .clear()
      // new handler should use development mode
      .route().failureHandler(ErrorHandler.create(vertx));
    // unset the system property
    System.setProperty(SYSTEM_PROPERTY_NAME, "test");

    int statusCode = 500;
    String errorMessage = "Something happened!";
    Exception e = new Exception(errorMessage);
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
      rc.fail(e);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkHtmlResponse(buff, resp, statusCode, errorMessage, e);
      testComplete();
    }), statusCode, "Internal Server Error", null);
    await();
  }


  @Test
  public void testFailOnNoDisplayExceptionDetailsAndNoException() throws Exception {
    router
      // clear the previous setup
      .clear()
      // new handler should use development mode
      .route().failureHandler(ErrorHandler.create(vertx, true));
    // unset the system property


    int statusCode = 404;
    String statusMessage = "Not Found";
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
      rc.fail(statusCode);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkHtmlResponse(buff, resp, statusCode, statusMessage);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }

  @Test
  public void testFailWithExceptionNoExceptionDetails() throws Exception {
    router.clear();
    router.route().failureHandler(ErrorHandler.create(vertx));
    int statusCode = 500;
    String statusMessage = "Something happened!";
    Exception e = new Exception(statusMessage);
    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
      rc.fail(e);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      checkHtmlResponse(buff, resp, statusCode, "Internal Server Error", e, false);
      testComplete();
    }), statusCode, "Internal Server Error", null);
    await();
  }


  @Test
  public void testSpecifyTemplate() throws Exception {
    router.clear();
    router.route().failureHandler(ErrorHandler.create(vertx, "test-error-template.html"));
    int statusCode = 404;
    String statusMessage = "Not Found";

    router.route().handler(rc -> {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
      rc.fail(statusCode);
    });
    testRequest(HttpMethod.GET, "/", null, resp -> resp.bodyHandler(buff -> {
      String page = buff.toString();
      assertEquals("<html><body>An unexpected error occurred.404.Not Found</body></html>", page);
      testComplete();
    }), statusCode, statusMessage, null);
    await();
  }


  private void checkHtmlResponse(Buffer buff, HttpClientResponse resp, int statusCode, String statusMessage) {
    checkHtmlResponse(buff, resp, statusCode, statusMessage, null);
  }

  private void checkHtmlResponse(Buffer buff, HttpClientResponse resp, int statusCode, String statusMessage, Exception e) {
    checkHtmlResponse(buff, resp, statusCode, statusMessage, e, true);
  }

  private void checkHtmlResponse(Buffer buff, HttpClientResponse resp, int statusCode, String statusMessage,
                                 Exception e, boolean displayExceptionDetails) {
    assertEquals("text/html", resp.headers().get(HttpHeaders.CONTENT_TYPE));
    String page = buff.toString();
    assertTrue(page.startsWith("<html>"));
    assertTrue(page.contains(String.valueOf(statusCode)));
    assertTrue(page.contains(statusMessage));
    assertTrue(page.contains("An unexpected error occurred"));
    if (e != null) {
      if (displayExceptionDetails) {
        // in this case the status message is replaced by the exception message
        assertTrue(page.contains(e.getMessage()));
        assertTrue(page.contains(e.getStackTrace()[0].toString()));
      } else {
        assertFalse(page.contains(e.getStackTrace()[0].toString()));
      }
    }
  }

  private void checkJsonResponse(Buffer buff, HttpClientResponse resp, int statusCode, String statusMessage) {
    assertEquals("application/json", resp.headers().get(HttpHeaders.CONTENT_TYPE));
    String page = buff.toString();
    assertEquals("{\"error\":{\"code\":" + statusCode +",\"message\":\"" + statusMessage + "\"}}", page);
  }

  private void checkTextResponse(Buffer buff, HttpClientResponse resp, int statusCode, String statusMessage) {
    assertEquals("text/plain", resp.headers().get(HttpHeaders.CONTENT_TYPE));
    String page = buff.toString();
    assertEquals("Error " + statusCode + ": " + statusMessage, page);
  }

}

