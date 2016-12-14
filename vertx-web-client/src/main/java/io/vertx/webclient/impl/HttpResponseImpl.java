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
package io.vertx.webclient.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.HttpResponse;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class HttpResponseImpl<T> implements HttpResponse<T> {

  private final HttpClientResponse resp;
  private Buffer buff;
  private T body;

  HttpResponseImpl(HttpClientResponse resp, Buffer buff, T body) {
    this.resp = resp;
    this.buff = buff;
    this.body = body;
  }

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
    return buff != null ? buff : body instanceof Buffer ? (Buffer)body : null;
  }

  @Override
  public String bodyAsString() {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.UTF8_DECODER.apply(b) : null;
  }

  @Override
  public String bodyAsString(String encoding) {
    Buffer b = bodyAsBuffer();
    return b != null ? b.toString(encoding) : null;
  }

  @Override
  public JsonObject bodyAsJsonObject() {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.JSON_OBJECT_DECODER.apply(b) : null;
  }

  @Override
  public <R> R bodyAsJson(Class<R> type) {
    Buffer b = bodyAsBuffer();
    return b != null ? BodyCodecImpl.jsonDecoder(type).apply(b) : null;
  }
}
