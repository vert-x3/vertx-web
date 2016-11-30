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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.streams.ReadStream;

/**
 * A template for configuring client-side HTTP requests.
 * <p>
 * Instances are created by an {@link HttpClient} instance, via one of the methods {@code createXXX} corresponding to the
 * specific HTTP methods.
 * <p>
 * The request template shall be configured prior making a request, the template is immutable and when a configuration method
 * is called, a new template is returned allowing to expose the template and apply further customization.
 * <p>
 * After the request template has been configured, the methods
 * <ul>
 *   <li>{@link #send(Handler)}</li>
 *   <li>{@link #sendStream(ReadStream, Handler)}</li>
 *   <li>{@link #bufferBody()}</li>
 * </ul>
 * can be called.
 * <p>
 * The {@code #bufferBody} configures the template to buffer the entire HTTP response body and returns a
 * {@link HttpResponseTemplate} for configuring the response body.
 * <p>
 * The {@code send} methods perform the actual request, they can be used multiple times to perform HTTP requests.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface HttpRequestTemplate {

  /**
   * Configure the template to use a new method {@code value}.
   *
   * @return a new {@code HttpRequestTemplate} instance with the specified method {@code value}
   */
  HttpRequestTemplate method(HttpMethod value);

  /**
   * Configure the template to use a new port {@code value}.
   *
   * @return a new {@code HttpRequestTemplate} instance with the specified port {@code value}
   */
  HttpRequestTemplate port(int value);

  /**
   * Configure the template to use a new host {@code value}.
   *
   * @return a new {@code HttpRequestTemplate} instance with the specified host {@code value}
   */
  HttpRequestTemplate host(String value);

  /**
   * Configure the template to use a new request URI {@code value}.
   *
   * @return a new {@code HttpRequestTemplate} instance with the specified request URI {@code value}
   */
  HttpRequestTemplate requestURI(String value);

  /**
   * Configure the template to add a new HTTP header.
   *
   * @param name the header name
   * @param value the header value
   * @return a new {@code HttpRequestTemplate} instance with the specified header
   */
  HttpRequestTemplate putHeader(String name, String value);

  /**
   * Configures the amount of time in milliseconds after which if the request does not return any data within the timeout
   * period an {@link java.util.concurrent.TimeoutException} fails the request.
   * <p>
   * Setting zero or a negative {@code value} disables the timeout.
   *
   * @param value The quantity of time in milliseconds.
   * @return a new {@code HttpRequestTemplate} instance with the specified timeout
   */
  HttpRequestTemplate timeout(long value);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} stream.
   *
   * @param body the body
   */
  void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Void>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} buffer.
   *
   * @param body the body
   */
  void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Void>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   */
  void sendJson(Object body, Handler<AsyncResult<HttpResponse<Void>>> handler);

  /**
   * Send a request, the {@code handler} will receive the response as an {@link HttpClientResponse}.
   */
  void send(Handler<AsyncResult<HttpResponse<Void>>> handler);

  /**
   * Configure to buffer the body and returns a {@link HttpResponseTemplate < Buffer >} for further configuration of
   * the response or {@link HttpResponseTemplate#send(Handler) sending} the request.
   */
  HttpResponseTemplate<Buffer> bufferBody();

}
