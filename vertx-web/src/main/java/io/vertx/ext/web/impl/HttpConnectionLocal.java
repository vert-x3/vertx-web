/*
 * Copyright 2026 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.impl;

import io.vertx.core.http.HttpConnection;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Storage scoped to the lifetime of an {@link HttpConnection}, without retaining connections after they become unreachable.
 */
public class HttpConnectionLocal<T> {

  private final Map<HttpConnection, T> values = new WeakHashMap<>();

  public T getOrCreate(HttpConnection connection, Supplier<T> supplier) {
    synchronized (values) {
      T value = values.get(connection);
      if (value == null) {
        value = supplier.get();
        values.put(connection, value);
      }
      return value;
    }
  }
}
