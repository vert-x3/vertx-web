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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.PayloadCodec;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PayloadCodecImpl<T> implements PayloadCodec<T> {

  public static final Function<Buffer, JsonObject> jsonObjectUnmarshaller = buff -> new JsonObject(buff.toString());
  public static final Function<Buffer, String> utf8Unmarshaller = Buffer::toString;

  public static Function<Buffer, String> stringUnmarshaller(String encoding) {
    return buff -> buff.toString(encoding);
  }

  public static <R> Function<Buffer, R> jsonUnmarshaller(Class<R> type) {
    return buff -> Json.decodeValue(buff.toString(), type);
  }

  private final Function<Buffer, T> unmarshaller;

  public PayloadCodecImpl(Function<Buffer, T> unmarshaller) {
    this.unmarshaller = unmarshaller;
  }

  @Override
  public Function<Buffer, T> unmarshaller() {
    return unmarshaller;
  }
}
