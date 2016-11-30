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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.webclient.HttpRequestTemplate;
import io.vertx.webclient.HttpResponse;
import io.vertx.webclient.HttpResponseTemplate;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpResponseTemplateImpl<T> implements HttpResponseTemplate<T> {

  static final Function<Buffer, JsonObject> jsonObjectUnmarshaller = buff -> new JsonObject(buff.toString());
  static final Function<Buffer, String> utf8Unmarshaller = Buffer::toString;

  static Function<Buffer, String> stringUnmarshaller(String encoding) {
    return buff -> buff.toString(encoding);
  }

  static <R> Function<Buffer, R> jsonUnmarshaller(Class<R> type) {
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

  private Handler<AsyncResult<HttpResponse<Void>>> createClientResponseHandler(Future<HttpResponse<T>> fut) {
    return ar -> {
      if (ar.succeeded()) {
        HttpResponse<Void> resp = ar.result();
        resp.bufferBody(ar2 -> {
          if (ar2.succeeded()) {
            Buffer buff = ar2.result();
            T body;
            try {
              body = bodyUnmarshaller.apply(buff);
            } catch (Throwable err) {
              fut.fail(err);
              return;
            }
            fut.complete(new HttpResponseImpl<T>(resp.httpClientResponse(), buff, body));
          } else {
            fut.fail(ar2.cause());
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
