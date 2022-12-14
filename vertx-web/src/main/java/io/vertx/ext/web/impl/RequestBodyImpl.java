/*
 * Copyright 2022 Red Hat, Inc.
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
package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.ParsedHeaderValues;
import io.vertx.ext.web.RoutingContext;

/**
 * Implementation of the Cacheable Request Body
 *
 * @author Paulo Lopes
 */
public class RequestBodyImpl implements RequestBody {

  private final RoutingContext ctx;

  private Buffer body;

  // caches
  private String string;
  private JsonObject jsonObject;
  private JsonArray jsonArray;

  public RequestBodyImpl(RoutingContext ctx) {
    this.ctx = ctx;
  }

  public void setBuffer(Buffer body) {
    this.body = body;
    // reset caches
    string = null;
    jsonObject = null;
    jsonArray = null;
  }

  @Override
  public @Nullable String asString() {
    if (body == null) {
      return null;
    } else {
      if (string == null) {
        ParsedHeaderValues parsedHeaders = ctx.parsedHeaders();
        if (parsedHeaders != null) {
          MIMEHeader contentType = parsedHeaders.contentType();
          if (contentType != null) {
            String charset = contentType.parameter("charset");
            if (charset != null) {
              string = body.toString(charset);
              return string;
            }
          }
        }
        string = body.toString();
      }
      return string;
    }
  }

  @Override
  public @Nullable String asString(String encoding) {
    if (body == null) {
      return null;
    } else {
      return body.toString(encoding);
    }
  }

  @Override
  public @Nullable JsonObject asJsonObject(int maxAllowedLength) {
    if (body == null) {
      return null;
    } else {
      if (jsonObject == null) {
        if (maxAllowedLength >= 0 && body.length() > maxAllowedLength) {
          throw new IllegalStateException("RoutingContext body size exceeds the allowed limit");
        }
        jsonObject = (JsonObject) Json.decodeValue(body);
      }
      return jsonObject;
    }
  }

  @Override
  public @Nullable JsonArray asJsonArray(int maxAllowedLength) {
    if (body == null) {
      return null;
    } else {
      if (jsonArray == null) {
        if (maxAllowedLength >= 0 && body.length() > maxAllowedLength) {
          throw new IllegalStateException("RoutingContext body size exceeds the allowed limit");
        }
        jsonArray = (JsonArray) Json.decodeValue(body);
      }
      return jsonArray;
    }
  }

  @Override
  public <R> @Nullable R asPojo(Class<R> clazz, int maxAllowedLength) {
    if (body == null) {
      return null;
    } else {
      if (maxAllowedLength >= 0 && body.length() > maxAllowedLength) {
        throw new IllegalStateException("RoutingContext body size exceeds the allowed limit");
      }
      return Json.decodeValue(body, clazz);
    }
  }

  @Override
  public @Nullable Buffer buffer() {
    return body;
  }

  @Override
  public int length() {
    if (body == null) {
      return -1;
    } else {
      return body.length();
    }
  }

  @Override
  public boolean available() {
    return ((RoutingContextInternal) ctx).seenHandler(RoutingContextInternal.BODY_HANDLER);
  }
}
