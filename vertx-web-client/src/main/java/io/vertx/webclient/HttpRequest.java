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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.streams.ReadStream;

/**
 * A client-side HTTP request.
 * <p>
 * Instances are created by an {@link WebClient} instance, via one of the methods corresponding to the specific
 * HTTP methods such as {@link WebClient#get}, etc...
 * <p>
 * The request shall be configured prior sending, the request is immutable and when a mutator method
 * is called, a new request is returned allowing to expose the request in a public API and apply further customization.
 * <p>
 * After the request has been configured, the methods
 * <ul>
 *   <li>{@link #send(Handler)}</li>
 *   <li>{@link #sendStream(ReadStream, Handler)}</li>
 *   <li>{@link #sendJson(Object, Handler)} ()}</li>
 *   <li>{@link #send(BodyCodec, Handler)} (Handler)}</li>
 * </ul>
 * can be called.
 * The {@code sendXXX} methods perform the actual request, they can be used multiple times to perform multiple HTTP requests.
 * <p>
 * The handler is called back with
 * <ul>
 *   <li>an {@link HttpResponse} instance when the HTTP response has been received</li>
 *   <li>a failure when the HTTP request failed (like a connection error) or when the HTTP response could
 *   not be obtained (like connection or unmarshalling errors)</li>
 * </ul>
 * <p>
 * Most of the time, this client will buffer the HTTP response fully unless a specific {@link BodyCodec} is used
 * such as {@link BodyCodec#stream(Handler)}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface HttpRequest {

  /**
   * Configure the request to use a new method {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest method(HttpMethod value);

  /**
   * Configure the request to use a new port {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest port(int value);

  /**
   * Configure the request to use a new host {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest host(String value);

  /**
   * Configure the request to use a new request URI {@code value}.
   * <p>
   * When the uri has query parameters, they are set in the {@link #queryParams()} multimap, overwritting
   * any parameters previously set.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest uri(String value);

  /**
   * Configure the request to add a new HTTP header.
   *
   * @param name the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest putHeader(String name, String value);

  /**
   * Configures the amount of time in milliseconds after which if the request does not return any data within the timeout
   * period an {@link java.util.concurrent.TimeoutException} fails the request.
   * <p>
   * Setting zero or a negative {@code value} disables the timeout.
   *
   * @param value The quantity of time in milliseconds.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest timeout(long value);

  /**
   * Add a query parameter to the request.
   *
   * @param paramName the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest addQueryParam(String paramName, String paramValue);

  /**
   * Set a query parameter to the request.
   *
   * @param paramName the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest setQueryParam(String paramName, String paramValue);

  /**
   * Return the current query parameters.
   *
   * @return the current query parameters
   */
  MultiMap queryParams();

  /**
   * Copy this request
   *
   * @return a copy of this request
   */
  HttpRequest copy();

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} stream.
   *
   * @param body the body
   */
  void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<Buffer>>> handler);

  /**
   * Like {@link #send(BodyCodec, Handler)} but with an HTTP request {@code body} stream.
   *
   * @param body the body
   * @param responseCodec the codec to decode the response
   */
  <R> void sendStream(ReadStream<Buffer> body, BodyCodec<R> responseCodec, Handler<AsyncResult<HttpResponse<R>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} buffer.
   *
   * @param body the body
   */
  void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<Buffer>>> handler);

  /**
   * Like {@link #send(BodyCodec, Handler)} but with an HTTP request {@code body} buffer.
   *
   * @param body the body
   * @param responseCodec the codec to decode the response
   */
  <R> void sendBuffer(Buffer body, BodyCodec<R> responseCodec, Handler<AsyncResult<HttpResponse<R>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   */
  void sendJson(Object body, Handler<AsyncResult<HttpResponse<Buffer>>> handler);

  /**
   * Like {@link #send(BodyCodec, Handler)} but with an HTTP request {@code body} object encoded as json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   * @param responseCodec the codec to decode the response
   */
  <R> void sendJson(Object body, BodyCodec<R> responseCodec, Handler<AsyncResult<HttpResponse<R>>> handler);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
   * set to {@code application/x-www-form-urlencoded}.
   * <p>
   * When the content type header is previously set to {@code multipart/form-data} it will be used instead.
   *
   * @param body the body
   */
  void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<Buffer>>> handler);

  /**
   * Like {@link #send(BodyCodec, Handler)} but with an HTTP request {@code body} multimap encoded as a form and the content type
   * set to {@code application/x-www-form-urlencoded}.
   * <p>
   * When the content type header is previously set to {@code multipart/form-data} it will be used instead.
   *
   * @param body the body
   * @param responseCodec the codec to decode the response
   */
  <R> void sendForm(MultiMap body, BodyCodec<R> responseCodec, Handler<AsyncResult<HttpResponse<R>>> handler);

  /**
   * Send a request, the {@code handler} will receive the response as an {@link HttpResponse}.
   */
  void send(Handler<AsyncResult<HttpResponse<Buffer>>> handler);

  /**
   * Send a request, the {@code handler} will receive the response as an {@link HttpResponse} decoded using
   * the provided {@code responseCodec}.
   *
   * @param responseCodec the codec to decode the response
   */
  <R> void send(BodyCodec<R> responseCodec, Handler<AsyncResult<HttpResponse<R>>> handler);

}
