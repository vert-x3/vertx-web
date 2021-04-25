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
package io.vertx.ext.web.client;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.cache.CacheAdapter;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.impl.WebClientCacheAware;

/**
 * An asynchronous cache aware HTTP / HTTP/2 client called {@code WebClientCache}.
 * <p>
 * This client wraps a {@link WebClient} and makes it cache aware adding features to it:
 * <ul>
 *   <li>Cache-Control header parsing</li>
 *   <li>Response serialization and deserialization for the {@link CacheAdapter}</li>
 *   <li>Freshness checking</li>
 * </ul>
 * <p>
 * The client honors the cache headers:
 * <ul>
 *  <li>Cache-Control with the following properties understood:
 *    <ul>
 *      <li>public</li>
 *      <li>private</li>
 *      <li>no-cache</li>
 *      <li>no-store</li>
 *      <li>max-age</li>
 *      <li>s-maxage</li>
 *    </ul>
 *  </li>
 *  <li>Expires</li>
 *  <li>ETag</li>
 *  <li>Vary</li>
 * </ul>
 * <p/>
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public interface WebClientCache extends WebClient {

  /**
   * Create a cache aware web client using the provided {@link CacheAdapter}.
   *
   * @param vertx        the vertx instance
   * @param cacheAdapter the cache adapter
   * @return the created web client
   */
  static WebClientCache create(Vertx vertx, CacheAdapter cacheAdapter) {
    WebClientCacheOptions options = new WebClientCacheOptions();
    return create(vertx, cacheAdapter, options);
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient} and {@link CacheAdapter}.
   *
   * @param webClient    the web client instance
   * @param cacheAdapter the cache adapter
   * @return the created web client
   */
  static WebClientCache create(WebClient webClient, CacheAdapter cacheAdapter) {
    WebClientCacheOptions options = new WebClientCacheOptions();
    return create(webClient, cacheAdapter, options);
  }

  /**
   * Create a cache aware web client using the provided {@link CacheAdapter} and options.
   *
   * @param vertx        the vertx instance
   * @param cacheAdapter the cache adapter
   * @param options      the Web Client Cache options
   * @return the created web client
   */
  static WebClientCache create(Vertx vertx, CacheAdapter cacheAdapter, WebClientCacheOptions options) {
    WebClient webClient = WebClient.create(vertx, options);
    return create(webClient, cacheAdapter, options);
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient} and {@link CacheAdapter}.
   *
   * @param webClient    the web client instance
   * @param cacheAdapter the cache adapter
   * @param options      the Web Client Cache options
   * @return the created web client
   */
  static WebClientCache create(WebClient webClient, CacheAdapter cacheAdapter, WebClientCacheOptions options) {
    return new WebClientCacheAware((WebClientBase) webClient, cacheAdapter, options);
  }

  /**
   * Wrap a {@link HttpClient} with a web client and default options.
   *
   * @param httpClient   the http client to wrap
   * @param cacheAdapter the cache adapter
   * @return the created web client
   */
  static WebClientCache wrap(HttpClient httpClient, CacheAdapter cacheAdapter) {
    WebClientCacheOptions options = new WebClientCacheOptions();
    return wrap(httpClient, cacheAdapter, options);
  }

  /**
   * Wrap a {@link HttpClient} with a web client and custom options.
   *
   * @param httpClient   the http client to wrap
   * @param cacheAdapter the cache adapter
   * @param options      the Web Client Cache options
   * @return the created web client
   */
  static WebClientCache wrap(HttpClient httpClient, CacheAdapter cacheAdapter, WebClientCacheOptions options) {
    return new WebClientCacheAware(httpClient, cacheAdapter, options);
  }
}
