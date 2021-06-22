/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.client.impl.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CacheControlDirective {
  PUBLIC("public"),
  PRIVATE("private"),
  NO_STORE("no-store"),
  NO_CACHE("no-cache"),
  SHARED_MAX_AGE("s-maxage"),
  MAX_AGE("max-age"),
  STALE_IF_ERROR("stale-if-error"),
  STALE_WHILE_REVALIDATE("stale-while-revalidate");

  private final String value;

  private static final Map<String, CacheControlDirective> VALUE_MAP =
    Arrays.stream(values()).collect(Collectors.toMap(d -> d.value, Function.identity()));

  public static Optional<CacheControlDirective> fromHeader(String headerValue) {
    if (VALUE_MAP.containsKey(headerValue)) {
      return Optional.of(VALUE_MAP.get(headerValue));
    } else {
      return Optional.empty();
    }
  }

  CacheControlDirective(String value) {
    this.value = value;
  }
}
