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
import io.vertx.ext.web.client.impl.cache.SharedDataCacheStore;
import io.vertx.ext.web.client.spi.CacheStore;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.client.impl.CachingWebClientImpl;
import io.vertx.ext.web.client.impl.cache.CacheManager;

/**
 * An asynchronous cache aware HTTP / HTTP/2 client called {@code CachingWebClient}.
 * <p>
 * This client wraps a {@link WebClient} and makes it cache aware by adding features to it:
 * <ul>
 *   <li>Cache-Control header parsing</li>
 *   <li>Response serialization and deserialization for the {@link CacheStore}</li>
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
public interface CachingWebClient {

  /**
   * Create a cache aware web client using an
   * {@link io.vertx.core.shareddata.AsyncMap} as the {@link CacheStore}.
   *
   * @param vertx the vertx instance
   * @return the creatd web client
   */
  static WebClient create(Vertx vertx) {
    CachingWebClientOptions options = new CachingWebClientOptions();
    return create(vertx, options);
  }

  /**
   * Create a cache aware web client using the options and an
   * {@link io.vertx.core.shareddata.AsyncMap} as the {@link CacheStore}.
   *
   * @param vertx   the vertx instance
   * @param options the caching web client options
   * @return the created web client
   */
  static WebClient create(Vertx vertx, CachingWebClientOptions options) {
    CacheStore store = new SharedDataCacheStore(vertx);
    return create(vertx, store, options);
  }

  /**
   * Create a cache aware web client using the provided {@link CacheStore}.
   *
   * @param vertx      the vertx instance
   * @param cacheStore the cache adapter
   * @return the created web client
   */
  static WebClient create(Vertx vertx, CacheStore cacheStore) {
    CachingWebClientOptions options = new CachingWebClientOptions();
    return create(vertx, cacheStore, options);
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient} and {@link CacheStore}.
   *
   * @param webClient  the web client instance
   * @param cacheStore the cache adapter
   * @return the created web client
   */
  static WebClient create(WebClient webClient, CacheStore cacheStore) {
    CachingWebClientOptions options = new CachingWebClientOptions();
    return create(webClient, cacheStore, options);
  }

  /**
   * Create a cache aware web client using the provided {@link CacheStore} and options.
   *
   * @param vertx      the vertx instance
   * @param cacheStore the cache adapter
   * @param options    the caching web client options
   * @return the created web client
   */
  static WebClient create(Vertx vertx, CacheStore cacheStore, CachingWebClientOptions options) {
    WebClient webClient = WebClient.create(vertx, options);
    return create(webClient, cacheStore, options);
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient} and {@link CacheStore}.
   *
   * @param webClient  the web client instance
   * @param cacheStore the cache adapter
   * @param options    the caching web client options
   * @return the created web client
   */
  static WebClient create(WebClient webClient, CacheStore cacheStore, CachingWebClientOptions options) {
    CacheManager cacheManager = new CacheManager(cacheStore, options);
    return new CachingWebClientImpl((WebClientBase) webClient, cacheManager, options);
  }

  /**
   * Wrap a {@link HttpClient} with a web client and default options.
   *
   * @param httpClient the http client to wrap
   * @param cacheStore the cache adapter
   * @return the created web client
   */
  static WebClient wrap(HttpClient httpClient, CacheStore cacheStore) {
    CachingWebClientOptions options = new CachingWebClientOptions();
    return wrap(httpClient, cacheStore, options);
  }

  /**
   * Wrap a {@link HttpClient} with a web client and custom options.
   *
   * @param httpClient the http client to wrap
   * @param cacheStore the cache adapter
   * @param options    the caching web client options
   * @return the created web client
   */
  static WebClient wrap(HttpClient httpClient, CacheStore cacheStore, CachingWebClientOptions options) {
    CacheManager cacheManager = new CacheManager(cacheStore, options);
    return new CachingWebClientImpl(httpClient, cacheManager, options);
  }
}
