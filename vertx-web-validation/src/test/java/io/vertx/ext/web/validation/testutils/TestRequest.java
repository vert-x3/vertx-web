/*
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.ext.web.validation.testutils;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is a wrapper around {@link WebClient} that simplifies the creation of Http requests and the asserts on responses.
 *
 * @author <a href="https://slinkydeveloper.com">Francesco Guardiani</a>
 */
public class TestRequest {

  final HttpRequest<Buffer> req;
  final List<Consumer<HttpRequest<Buffer>>> requestTranformations;
  final List<Consumer<HttpResponse<Buffer>>> responseAsserts;

  private TestRequest(HttpRequest<Buffer> req) {
    this.req = req;
    this.requestTranformations = new ArrayList<>();
    this.responseAsserts = new ArrayList<>();
  }

  /**
   * Add one or more transformations to the TestRequest.<br/>
   * Note: this transformations are evaluated when one of the send methods is called
   *
   * @param transformations
   * @return a reference to this, so the API can be used fluently
   */
  @SafeVarargs
  public final TestRequest with(Consumer<HttpRequest<Buffer>>... transformations) {
    requestTranformations.addAll(Arrays.asList(transformations));
    return this;
  }

  /**
   * Add one or more response asserts to the TestRequest.
   *
   * @param asserts
   * @return a reference to this, so the API can be used fluently
   */
  @SafeVarargs
  public final TestRequest expect(Consumer<HttpResponse<Buffer>>... asserts) {
    responseAsserts.addAll(Arrays.asList(asserts));
    return this;
  }

  /**
   * Send and flag the provided checkpoint with {@link Checkpoint#flag()} when request is completed and no assertion fails
   *
   * @param testContext
   * @param checkpoint
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> send(VertxTestContext testContext, Checkpoint checkpoint) {
    return internalSend(testContext, h -> req.send(h), checkpoint::flag);
  }

  /**
   * Send and complete test context with {@link VertxTestContext#completeNow()} when request is completed and no assertion fails
   *
   * @param testContext
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> send(VertxTestContext testContext) {
    return internalSend(testContext, h -> req.send(h), testContext::completeNow);
  }

  /**
   * Send and execute {@code onEnd} code block wrapped in {@link VertxTestContext#verify(VertxTestContext.ExecutionBlock)}
   * when request is completed and no assertion fails
   *
   * @param testContext
   * @param onEnd
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> send(VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd) {
    return internalSend(testContext, h -> req.send(h), onEnd);
  }

  /**
   * Send a json and flag the provided checkpoint with {@link Checkpoint#flag()} when request is completed and no assertion fails
   *
   * @param json
   * @param testContext
   * @param checkpoint
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendJson(Object json, VertxTestContext testContext, Checkpoint checkpoint) {
    return internalSend(testContext, h -> req.sendJson(json, h), checkpoint::flag);
  }

  /**
   * Send a json and complete test context with {@link VertxTestContext#completeNow()} when request is completed and no assertion fails
   *
   * @param json
   * @param testContext
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendJson(Object json, VertxTestContext testContext) {
    return internalSend(testContext, h -> req.sendJson(json, h), testContext::completeNow);
  }

  /**
   * Send a json and execute {@code onEnd} code block wrapped in {@link VertxTestContext#verify(VertxTestContext.ExecutionBlock)}
   * when request is completed and no assertion fails
   *
   * @param json
   * @param testContext
   * @param onEnd
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendJson(Object json, VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd) {
    return internalSend(testContext, h -> req.sendJson(json, h), onEnd);
  }

  /**
   * Send a {@link Buffer} and flag the provided checkpoint with {@link Checkpoint#flag()} when request is completed and no assertion fails
   *
   * @param buf
   * @param testContext
   * @param checkpoint
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendBuffer(Buffer buf, VertxTestContext testContext, Checkpoint checkpoint) {
    return internalSend(testContext, h -> req.sendBuffer(buf, h), checkpoint::flag);
  }

  /**
   * Send a {@link Buffer} and complete test context with {@link VertxTestContext#completeNow()} when request is completed and no assertion fails
   *
   * @param buf
   * @param testContext
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendBuffer(Buffer buf, VertxTestContext testContext) {
    return internalSend(testContext, h -> req.sendBuffer(buf, h), testContext::completeNow);
  }

  /**
   * Send a {@link Buffer} and execute {@code onEnd} code block wrapped in {@link VertxTestContext#verify(VertxTestContext.ExecutionBlock)}
   * when request is completed and no assertion fails
   *
   * @param buf
   * @param testContext
   * @param onEnd
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendBuffer(Buffer buf, VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd) {
    return internalSend(testContext, h -> req.sendBuffer(buf, h), onEnd);
  }

  /**
   * Send an URL Encoded form and flag the provided checkpoint with {@link Checkpoint#flag()} when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @param checkpoint
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendURLEncodedForm(MultiMap form, VertxTestContext testContext, Checkpoint checkpoint) {
    return internalSend(testContext, h -> req.sendForm(form, h), checkpoint::flag);
  }

  /**
   * Send an URL Encoded form and complete test context with {@link VertxTestContext#completeNow()} when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendURLEncodedForm(MultiMap form, VertxTestContext testContext) {
    return internalSend(testContext, h -> req.sendForm(form, h), testContext::completeNow);
  }

  /**
   * Send an URL Encoded form and execute {@code onEnd} code block wrapped in {@link VertxTestContext#verify(VertxTestContext.ExecutionBlock)}
   * when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @param onEnd
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendURLEncodedForm(MultiMap form, VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd) {
    return internalSend(testContext, h -> req.sendForm(form, h), onEnd);
  }

  /**
   * Send a multipart form and flag the provided checkpoint with {@link Checkpoint#flag()} when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @param checkpoint
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendMultipartForm(MultipartForm form, VertxTestContext testContext, Checkpoint checkpoint) {
    return internalSend(testContext, h -> req.sendMultipartForm(form, h), checkpoint::flag);
  }

  /**
   * Send a multipart form and complete test context with {@link VertxTestContext#completeNow()} when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendMultipartForm(MultipartForm form, VertxTestContext testContext) {
    return internalSend(testContext, h -> req.sendMultipartForm(form, h), testContext::completeNow);
  }

  /**
   * Send a multipart form and execute {@code onEnd} code block wrapped in {@link VertxTestContext#verify(VertxTestContext.ExecutionBlock)}
   * when request is completed and no assertion fails
   *
   * @param form
   * @param testContext
   * @param onEnd
   * @return a future that will be completed when the response is ready and no response assertion fails
   */
  public Future<HttpResponse<Buffer>> sendMultipartForm(MultipartForm form, VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd) {
    return internalSend(testContext, h -> req.sendMultipartForm(form, h), onEnd);
  }

  private Handler<AsyncResult<HttpResponse<Buffer>>> generateHandleResponse(VertxTestContext testContext, VertxTestContext.ExecutionBlock onEnd, Promise<HttpResponse<Buffer>> fut, StackTraceElement[] stackTrace) {
    return ar -> {
      if (ar.failed()) {
        testContext.failNow(ar.cause());
      } else {
        testContext.verify(() -> {
          try {
            this.responseAsserts.forEach(c -> c.accept(ar.result()));
          } catch (AssertionError e) {
            AssertionError newE = new AssertionError("Assertion error in response: " + e.getMessage(), e);
            newE.setStackTrace(stackTrace);
            throw newE;
          }
          onEnd.apply();
        });
        fut.complete(ar.result());
      }
    };
  }

  private Future<HttpResponse<Buffer>> internalSend(VertxTestContext testContext, Consumer<Handler<AsyncResult<HttpResponse<Buffer>>>> reqSendFunction, VertxTestContext.ExecutionBlock onEnd) {
    Promise<HttpResponse<Buffer>> promise = Promise.promise();
    this.requestTranformations.forEach(c -> c.accept(req));
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    reqSendFunction.accept(generateHandleResponse(testContext, onEnd, promise, Arrays.copyOfRange(
      stackTrace,
      3,
      stackTrace.length
    )));
    return promise.future();
  }

  /**
   * Creates a new {@link TestRequest} object with provided client, method and path
   *
   * @param client
   * @param method
   * @param path
   * @return
   */
  public static TestRequest testRequest(WebClient client, HttpMethod method, String path) {
    return new TestRequest(client.request(method, path));
  }

  /**
   * Wraps {@link HttpRequest} in a new {@link TestRequest}
   *
   * @param request
   * @return
   */
  public static TestRequest testRequest(HttpRequest<Buffer> request) {
    return new TestRequest(request);
  }

  /**
   * Add an header to the request
   *
   * @param key
   * @param value
   * @return
   */
  public static Consumer<HttpRequest<Buffer>> requestHeader(String key, String value) {
    return req -> req.putHeader(key, value);
  }

  /**
   *
   * @param encoder
   * @return
   */
  public static Consumer<HttpRequest<Buffer>> cookie(QueryStringEncoder encoder) {
    return req -> {
      try {
        String rawQuery = encoder.toUri().getRawQuery();
        if (rawQuery != null && !rawQuery.isEmpty()) {
          req.putHeader("cookie", encoder.toUri().getRawQuery());
        }
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    };
  }

  public static Consumer<HttpRequest<Buffer>> queryParam(String key, String value) {
    return req -> req.addQueryParam(key, value);
  }

  public static Consumer<HttpResponse<Buffer>> statusCode(int statusCode) {
    return res -> assertEquals(statusCode, res.statusCode());
  }

  public static Consumer<HttpResponse<Buffer>> statusMessage(String statusMessage) {
    return res -> assertEquals(statusMessage, res.statusMessage());
  }

  public static Consumer<HttpResponse<Buffer>> jsonBodyResponse(Object expected) {
    return res -> {
      String ctHeader = res.getHeader("content-type");
      assertNotNull(ctHeader, "Content-type must not be null");
      assertTrue(ctHeader.contains("application/json"), "Expected application/json Content-type, Actual: " + ctHeader);
      Object json = Json.decodeValue(res.bodyAsBuffer());
      assertEquals(expected, json);
    };
  }

  public static Consumer<HttpResponse<Buffer>> bodyResponse(Buffer expected, String expectedContentType) {
    return res -> {
      assertEquals(expectedContentType, res.getHeader("content-type"));
      assertEquals(expected, res.bodyAsBuffer());
    };
  }

  public static Consumer<HttpResponse<Buffer>> responseHeader(String headerName, String headerValue) {
    return res -> assertEquals(headerValue, res.getHeader(headerName));
  }

  public static Consumer<HttpResponse<Buffer>> stringBody(Consumer<String> assertBody) {
    return res -> assertBody.accept(res.bodyAsString());
  }

  public static Consumer<HttpResponse<Buffer>> emptyResponse() {
    return res -> assertNull(res.body());
  }

  public static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

}
