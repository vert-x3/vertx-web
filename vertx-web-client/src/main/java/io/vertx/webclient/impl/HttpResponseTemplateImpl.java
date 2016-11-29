/*
 * Copyright (c) 2011-2013 The original author or authors
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
package io.vertx.webclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.webclient.HttpRequestTemplate;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.HttpResponseTemplate;

import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpResponseTemplateImpl<T> implements HttpResponseTemplate<T> {

  private static final Function<Buffer, JsonObject> jsonObjectUnmarshaller = buff -> new JsonObject(buff.toString());
  private static final Function<Buffer, String> utf8Unmarshaller = Buffer::toString;

  private static Function<Buffer, String> stringUnmarshaller(String encoding) {
    return buff -> buff.toString(encoding);
  }

  private static <R> Function<Buffer, R> jsonUnmarshaller(Class<R> type) {
    return buff -> Json.decodeValue(buff.toString(), type);
  }

  private final HttpRequestTemplate requestTemplate;
  private final Function<Buffer, T> bodyUnmarshaller;

  HttpResponseTemplateImpl(HttpRequestTemplate requestTemplate, Function<Buffer, T> bodyUnmarshaller) {
    this.requestTemplate = requestTemplate;
    this.bodyUnmarshaller = bodyUnmarshaller;
  }

  @Override
  public HttpResponseTemplate<String> asString() {
    return new HttpResponseTemplateImpl<>(requestTemplate, utf8Unmarshaller);
  }

  @Override
  public HttpResponseTemplate<String> asString(String encoding) {
    return new HttpResponseTemplateImpl<>(requestTemplate, stringUnmarshaller(encoding));
  }

  @Override
  public HttpResponseTemplate<JsonObject> asJsonObject() {
    return new HttpResponseTemplateImpl<>(requestTemplate, jsonObjectUnmarshaller);
  }

  @Override
  public <R> HttpResponseTemplate<R> as(Class<R> type) {
    return new HttpResponseTemplateImpl<>(requestTemplate, jsonUnmarshaller(type));
  }

  private Handler<AsyncResult<HttpClientResponse>> createClientResponseHandler(Future<HttpResponse<T>> fut) {
    return ar -> {
      if (ar.succeeded()) {
        HttpClientResponse resp = ar.result();
        resp.exceptionHandler(err -> {
          if (!fut.isComplete()) {
            fut.fail(err);
          }
        });
        resp.bodyHandler(buff -> {
          T body;
          try {
            body = bodyUnmarshaller.apply(buff);
          } catch (Throwable err) {
            if (!fut.failed()) {
              fut.fail(err);
            }
            return;
          }
          if (!fut.failed()) {
            fut.complete(new HttpResponse<T>() {
              @Override
              public HttpVersion version() {
                return resp.version();
              }
              @Override
              public int statusCode() {
                return resp.statusCode();
              }
              @Override
              public String statusMessage() {
                return resp.statusMessage();
              }
              @Override
              public String getHeader(String headerName) {
                return resp.getHeader(headerName);
              }
              @Override
              public MultiMap trailers() {
                return resp.trailers();
              }
              @Override
              public String getTrailer(String trailerName) {
                return resp.getTrailer(trailerName);
              }
              @Override
              public List<String> cookies() {
                return resp.cookies();
              }
              @Override
              public MultiMap headers() {
                return resp.headers();
              }
              @Override
              public T body() {
                return body;
              }
              @Override
              public Buffer bodyAsBuffer() {
                return buff;
              }
              @Override
              public String bodyAsString() {
                return utf8Unmarshaller.apply(buff);
              }
              @Override
              public String bodyAsString(String encoding) {
                return buff.toString(encoding);
              }
              @Override
              public JsonObject bodyAsJsonObject() {
                return jsonObjectUnmarshaller.apply(buff);
              }
              @Override
              public <R> R bodyAs(Class<R> type) {
                return jsonUnmarshaller(type).apply(buff);
              }
            });
          }
        });
      } else {
        fut.fail(ar.cause());
      }
    };
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    requestTemplate.sendStream(body, createClientResponseHandler(Future.<HttpResponse<T>>future().setHandler(handler)));
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    requestTemplate.sendBuffer(body, createClientResponseHandler(Future.<HttpResponse<T>>future().setHandler(handler)));
  }

  @Override
  public void sendJson(Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    requestTemplate.sendJson(body, createClientResponseHandler(Future.<HttpResponse<T>>future().setHandler(handler)));
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<T>>> handler) {
    requestTemplate.send(createClientResponseHandler(Future.<HttpResponse<T>>future().setHandler(handler)));
  }
}
