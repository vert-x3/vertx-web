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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CachingWebClientOptions extends WebClientOptions {

  public static final Set<Integer> DEFAULT_CACHED_STATUS_CODES = buildDefaultStatusCodes();

  private boolean enablePublicCaching = false;
  private boolean enablePrivateCaching = false;
  private Set<Integer> cachedStatusCodes = DEFAULT_CACHED_STATUS_CODES;

  public CachingWebClientOptions() {
  }

  public CachingWebClientOptions(WebClientOptions other) {
    super(other);
  }

  void init(CachingWebClientOptions other) {
    super.init(other);
    this.enablePublicCaching = other.enablePublicCaching;
    this.enablePrivateCaching = other.enablePrivateCaching;
    this.cachedStatusCodes = other.cachedStatusCodes;
  }

  /**
   * Configure the client to cache {@code Cache-Control: public} responses.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions enableCaching() {
    this.enablePublicCaching = true;
    return this;
  }

  /**
   * Configure the client to cache publicly, and optionally privately, cacheable responses.
   *
   * @param includePrivate true to enable caching private responses
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions enableCaching(boolean includePrivate) {
    this.enablePublicCaching = true;
    this.enablePrivateCaching = includePrivate;
    return this;
  }

  /**
   * Configure the client to cache {@code Cache-Control: private} responses.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions enablePrivateCaching() {
    this.enablePrivateCaching = true;
    return this;
  }

  /**
   * Configure the client cache behavior for {@code Cache-Control: public} responses.
   *
   * @param enabled true to enable caching responses
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions setEnablePublicCaching(boolean enabled) {
    this.enablePublicCaching = enabled;
    return this;
  }

  /**
   * Configure the client cache behavior for {@code Cache-Control: private} responses.
   *
   * @param enabled true to enable caching responses
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions setEnablePrivateCaching(boolean enabled) {
    this.enablePrivateCaching = enabled;
    return this;
  }

  /**
   * Configure the status codes that can be cached.
   *
   * @param codes the cacheable status code numbers
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions setCachedStatusCodes(Collection<Integer> codes) {
    this.cachedStatusCodes = new HashSet<>(codes);
    return this;
  }

  /**
   * Add an additional status code that is cacheable.
   *
   * @param code the additional code number
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions addCachedStatusCode(int code) {
    this.cachedStatusCodes.add(code);
    return this;
  }

  /**
   * Remove a status code that is cacheable.
   *
   * @param code the code number to remove
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientOptions removeCachedStatusCode(int code) {
    this.cachedStatusCodes.remove(code);
    return this;
  }

  /**
   * @return the set of status codes to consider cacheable.
   */
  public Set<Integer> cachedStatusCodes() {
    return cachedStatusCodes;
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

  private static Set<Integer> buildDefaultStatusCodes() {
    Set<Integer> codes = new HashSet<>(3);
    Collections.addAll(codes, 200, 301, 404);
    return codes;
  }
}
