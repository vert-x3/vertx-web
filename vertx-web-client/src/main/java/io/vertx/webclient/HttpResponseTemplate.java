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
package io.vertx.webclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * A template for configuring client-side HTTP responses.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface HttpResponseTemplate<T> {

  /**
   * Send a request, the {@code handler} will receive the response as an {@link HttpResponse}.
   */
  void send(Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} stream.
   *
   * @param body the body
   */
  void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} buffer.
   *
   * @param body the body
   */
  void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   */
  void sendJson(Object body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * Configure the template to decode the response as a {@code String}.
   *
   * @return a new {@code HttpResponseTemplate} instance decoding the response as a {@code String}
   */
  HttpResponseTemplate<String> asString();

  /**
   * Like {@link #asString()} but with the specified {@code encoding} param.
   */
  HttpResponseTemplate<String> asString(String encoding);

  /**
   * Configure the template to decode the response as a Json object.
   *
   * @return a new {@code HttpResponseTemplate} instance decoding the response as a Json object
   */
  HttpResponseTemplate<JsonObject> asJsonObject();

  /**
   * Configure the template to decode the response using a specified {@code type} using the Jackson mapper.
   *
   * @return a new {@code HttpResponseTemplate} instance decoding the response as specified type
   */
  @GenIgnore
  <R> HttpResponseTemplate<R> as(Class<R> type);

}
