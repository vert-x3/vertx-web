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
package io.vertx.ext.web.client.impl;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.CachingWebClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.impl.cache.CacheInterceptor;
import io.vertx.ext.web.client.spi.CacheStore;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public interface CachingWebClientImpl {

  static WebClient wrap(HttpClient client, CacheStore cacheStore, CachingWebClientOptions options) {
    WebClientInternal internal = new WebClientBase(client, options);
    internal.addInterceptor(new CacheInterceptor(cacheStore, options));
    return internal;
  }

  static WebClient wrap(WebClient webClient, CacheStore cacheStore, CachingWebClientOptions options) {
    WebClientInternal internal = new WebClientBase((WebClientBase) webClient);
    internal.addInterceptor(new CacheInterceptor(cacheStore, options));
    return internal;
  }
}
