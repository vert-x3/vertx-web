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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.webclient.impl.PayloadCodecImpl;

import java.util.function.Function;

/**
 * A builder for configuring client-side HTTP responses.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PayloadCodec<T> {

  static PayloadCodec<String> string() {
    return new PayloadCodecImpl<>(Buffer::toString);
  }

  static PayloadCodec<String> string(String enc) {
    return new PayloadCodecImpl<>(PayloadCodecImpl.stringUnmarshaller(enc));
  }

  static PayloadCodec<Buffer> buffer() {
    return new PayloadCodecImpl<>(Function.identity());
  }

  static PayloadCodec<JsonObject> jsonObject() {
    return new PayloadCodecImpl<>(PayloadCodecImpl.jsonObjectUnmarshaller);
  }

  @GenIgnore
  static <U> PayloadCodec<U> json(Class<U> type) {
    return new PayloadCodecImpl<U>(PayloadCodecImpl.jsonUnmarshaller(type));
  }

  @GenIgnore
  Function<Buffer, T> unmarshaller();
}
