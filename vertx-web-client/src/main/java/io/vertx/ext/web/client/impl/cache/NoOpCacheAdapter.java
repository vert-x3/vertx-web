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

import io.vertx.core.Future;
import io.vertx.ext.web.client.cache.CacheAdapter;

/**
 * A {@link CacheAdapter} implementation that does nothing.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public final class NoOpCacheAdapter implements CacheAdapter {

  @Override
  public Future<CachedHttpResponse> get(CacheKey key) {
    return Future.failedFuture("NoOpCacheAdapter cannot return results");
  }

  @Override
  public Future<CachedHttpResponse> set(CacheKey key, CachedHttpResponse response) {
    return Future.succeededFuture(response);
  }

  @Override
  public Future<Void> delete(CacheKey key) {
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> flush() {
    return Future.succeededFuture();
  }
}
