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
package io.vertx.ext.web.client.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.impl.BodyCodecImpl;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpResponseImpl<T> implements HttpResponse<T> {

  private final HttpVersion version;
  private final int statusCode;
  private final String statusMessage;
  private final MultiMap headers;
  private final MultiMap trailers;
  private final List<String> cookies;
  private final T body;

  public HttpResponseImpl(HttpVersion version,
                          int statusCode,
                          String statusMessage,
                          MultiMap headers,
                          MultiMap trailers,
                          List<String> cookies,
                          T body) {
    this.version = version;
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.headers = headers;
    this.trailers = trailers;
    this.cookies = cookies;
    this.body = body;
  }

  @Override
  public HttpVersion version() {
    return version;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public String statusMessage() {
    return statusMessage;
  }

  @Override
  public String getHeader(String headerName) {
    return headers.get(headerName);
  }

  @Override
  public MultiMap trailers() {
    return trailers;
  }

  @Override
  public String getTrailer(String trailerName) {
    return trailers.get(trailerName);
  }

  @Override
  public List<String> cookies() {
    return cookies;
  }

  @Override
  public MultiMap headers() {
    return headers;
  }

  @Override
  public T body() {
    return body;
  }

  @Override
  public Buffer bodyAsBuffer() {
    return body instanceof Buffer ? (Buffer) body : null;
  }

  @Override
  public JsonArray bodyAsJsonArray() {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.JSON_ARRAY_DECODER.apply(b) : null;
  }
}
