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

import io.vertx.core.MultiMap;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
class CacheVariationsKey {

  protected final String host;
  protected final int port;
  protected final String path;
  protected final String queryString;

  CacheVariationsKey(HttpRequest<?> request) {
    HttpRequestImpl<?> impl = (HttpRequestImpl<?>) request;
    this.host = impl.host();
    this.port = impl.port();
    this.path = impl.uri();
    this.queryString = queryString(impl.queryParams());
  }

  @Override
  public String toString() {
    return host + ":" + port + path + "?" + queryString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CacheVariationsKey that = (CacheVariationsKey) o;
    return port == that.port
      && host.equals(that.host)
      && path.equals(that.path)
      && queryString.equals(that.queryString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, path, queryString);
  }

  private String queryString(MultiMap queryParams) {
    return queryParams.entries()
      .stream()
      .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
      .map(e -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&"));
  }
}
