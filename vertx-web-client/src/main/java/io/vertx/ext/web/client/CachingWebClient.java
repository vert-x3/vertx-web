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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.client.impl.CachingWebClientImpl;
import io.vertx.ext.web.client.impl.cache.LocalCacheStore;
import io.vertx.ext.web.client.spi.CacheStore;

/**
 * An asynchronous cache aware HTTP / HTTP/2 client called {@code CachingWebClient}.
 * <p>
 * This client wraps a {@link WebClient} and makes it cache aware by adding features to it:
 * <ul>
 *   <li>Cache-Control header parsing</li>
 *   <li>Freshness checking</li>
 * </ul>
 * <p>
 * The client honors the following cache headers:
 * <ul>
 *  <li>Cache-Control with the following properties understood:
 *    <ul>
 *      <li>public</li>
 *      <li>private</li>
 *      <li>no-cache</li>
 *      <li>no-store</li>
 *      <li>max-age</li>
 *      <li>s-maxage</li>
 *      <li>stale-if-error</li>
 *      <li>staile-while-revalidate</li>
 *      <li>must-revalidate</li>
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
@VertxGen
public interface CachingWebClient {

  /**
   * Create a cache aware web client using the provided {@link WebClient}.
   *
   * @param webClient the web client instance
   * @return the created web client
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static WebClient create(WebClient webClient) {
    return create(webClient, new LocalCacheStore());
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient}.
   *
   * @param webClient  the web client instance
   * @param cacheStore the cache adapter
   * @return the created web client
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static WebClient create(WebClient webClient, CacheStore cacheStore) {
    return create(webClient, cacheStore, new CachingWebClientOptions());
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient}.
   *
   * @param webClient  the web client instance
   * @param options    the caching web client options
   * @return the created web client
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static WebClient create(WebClient webClient, CachingWebClientOptions options) {
    return create(webClient, new LocalCacheStore(), options);
  }

  /**
   * Create a cache aware web client using the provided {@link WebClient}.
   *
   * @param webClient  the web client instance
   * @param cacheStore the cache adapter
   * @param options    the caching web client options
   * @return the created web client
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static WebClient create(WebClient webClient, CacheStore cacheStore, CachingWebClientOptions options) {
    return CachingWebClientImpl.wrap(webClient, cacheStore, options);
  }
}
