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

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class WebClientCacheOptions extends WebClientOptions {

  private boolean enablePublicCaching = false;
  private boolean enablePrivateCaching = false;

  public WebClientCacheOptions() {
  }

  public WebClientCacheOptions(WebClientOptions other) {
    super(other);
  }

  void init(WebClientCacheOptions other) {
    super.init(other);
    this.enablePublicCaching = other.enablePublicCaching;
    this.enablePrivateCaching = other.enablePrivateCaching;
  }

  /**
   * Configure the client to cache {@code Cache-Control: public} responses.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientCacheOptions enableCaching() {
    this.enablePublicCaching = true;
    return this;
  }

  /**
   * Configure the client to cache publicly, and optionally privately, cacheable responses.
   *
   * @param includePrivate true to enable caching private responses
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientCacheOptions enableCaching(boolean includePrivate) {
    this.enablePublicCaching = true;
    this.enablePrivateCaching = includePrivate;
    return this;
  }

  /**
   * Configure the client to cache {@code Cache-Control: private} responses.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientCacheOptions enablePrivateCaching() {
    this.enablePrivateCaching = true;
    return this;
  }

  /**
   * Configure the client cache behavior for {@code Cache-Control: public} responses.
   *
   * @param enabled true to enable caching responses
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientCacheOptions setEnablePublicCaching(boolean enabled) {
    this.enablePublicCaching = enabled;
    return this;
  }

  /**
   * Configure the client cache behavior for {@code Cache-Control: private} responses.
   *
   * @param enabled true to enable caching responses
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientCacheOptions setEnablePrivateCaching(boolean enabled) {
    this.enablePrivateCaching = enabled;
    return this;
  }

  /**
   * @return true if the client will cache {@code Cache-Control: public} responses, false otherwise
   */
  public boolean isPublicCachingEnabled() {
    return enablePublicCaching;
  }

  /**
   * @return true if the client will cache {@code Cache-Control: private} responses, false otherwise
   */
  public boolean isPrivateCachingEnabled() {
    return enablePrivateCaching;
  }

  /**
   * @return true if the client will cache {@code Cache-Control: public} or {@code Cache-Control: private} responses, false otherwise
   */
  public boolean isCachingEnabled() {
    return isPublicCachingEnabled() || isPrivateCachingEnabled();
  }
}
