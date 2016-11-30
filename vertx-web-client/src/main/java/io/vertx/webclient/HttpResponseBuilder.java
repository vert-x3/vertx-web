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
 * A builder for configuring client-side HTTP responses.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface HttpResponseBuilder<T> {

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
   * Configure the builder to decode the response as a {@code String}.
   *
   * @return a new {@code HttpResponseBuilder} instance decoding the response as a {@code String}
   */
  HttpResponseBuilder<String> asString();

  /**
   * Like {@link #asString()} but with the specified {@code encoding} param.
   */
  HttpResponseBuilder<String> asString(String encoding);

  /**
   * Configure the builder to decode the response as a Json object.
   *
   * @return a new {@code HttpResponseBuilder} instance decoding the response as a Json object
   */
  HttpResponseBuilder<JsonObject> asJsonObject();

  /**
   * Configure the builder to decode the response using a specified {@code type} using the Jackson mapper.
   *
   * @return a new {@code HttpResponseBuilder} instance decoding the response as specified type
   */
  @GenIgnore
  <R> HttpResponseBuilder<R> as(Class<R> type);

}
