/*
 * Copyright 2018 Red Hat, Inc.
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
package io.vertx.ext.web.common;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Promise;

/**
 * Handle cache miss events. On a cache miss, the handler shall receive the key of the missing element and a Promise.
 * Implementations should fulfill the promise (either success or failure). On success the cache shall store it's result
 * as the cache value for the given key, on failure the failure bubbles up to the caller.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@VertxGen
@FunctionalInterface
public interface CacheMissHandler<K, V> {
  void handle(K key, Promise<V> promise);
}
