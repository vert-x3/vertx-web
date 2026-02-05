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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.uritemplate.ExpandOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
@DataObject
public class CachingWebClientConfig extends WebClientConfig {

  public static final Set<Integer> DEFAULT_CACHED_STATUS_CODES = buildDefaultStatusCodes();
  public static final Set<HttpMethod> DEFAULT_CACHED_METHODS = buildDefaultMethods();

  private boolean enableVaryCaching = false;
  private Set<Integer> cachedStatusCodes = DEFAULT_CACHED_STATUS_CODES;
  private Set<HttpMethod> cachedMethods = DEFAULT_CACHED_METHODS;

  public CachingWebClientConfig() {
  }

  CachingWebClientConfig(CachingWebClientOptions other) {
    super(other);

    init(other);
  }

  public CachingWebClientConfig(WebClientConfig other) {
    super(other);
  }

  void init(CachingWebClientConfig other) {
    super.init(other);
    this.enableVaryCaching = other.enableVaryCaching;
    this.cachedStatusCodes = other.cachedStatusCodes;
    this.cachedMethods = other.cachedMethods;
  }

  void init(CachingWebClientOptions other) {
    super.init(other);
    this.enableVaryCaching = other.isVaryCachingEnabled();
    this.cachedStatusCodes = other.getCachedStatusCodes();
    this.cachedMethods = other.getCachedMethods();
  }

  /**
   * Configure the client cache behavior for {@code Vary} responses.
   *
   * @param enabled true to enable caching varying responses
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig setEnableVaryCaching(boolean enabled) {
    this.enableVaryCaching = enabled;
    return this;
  }

  /**
   * @return the set of status codes to consider cacheable.
   */
  public Set<Integer> getCachedStatusCodes() {
    return cachedStatusCodes;
  }

  /**
   * Configure the status codes that can be cached.
   *
   * @param codes the cacheable status code numbers
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig setCachedStatusCodes(Set<Integer> codes) {
    this.cachedStatusCodes = codes;
    return this;
  }

  /**
   * Add a status code that is cacheable.
   *
   * @param code the additional code number
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig addCachedStatusCode(Integer code) {
    this.cachedStatusCodes.add(code);
    return this;
  }

  /**
   * Remove a status code that is cacheable.
   *
   * @param code the code number to remove
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig removeCachedStatusCode(Integer code) {
    this.cachedStatusCodes.remove(code);
    return this;
  }

  /**
   * @return the set of HTTP methods to consider cacheable.
   */
  public Set<HttpMethod> getCachedMethods() {
    return cachedMethods;
  }

  /**
   * Configure the HTTP methods that can be cached.
   *
   * @param methods the HTTP methods to cache
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig setCachedMethods(Set<HttpMethod> methods) {
    this.cachedMethods = methods;
    return this;
  }

  /**
   * Add an HTTP method that is cacheable.
   *
   * @param method the method to add
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig addCachedMethod(HttpMethod method) {
    this.cachedMethods.add(method);
    return this;
  }

  /**
   * Remove an HTTP method that is cacheable.
   *
   * @param method the method to remove
   * @return a reference to this, so the API can be used fluently
   */
  public CachingWebClientConfig removeCachedMethod(HttpMethod method) {
    this.cachedMethods.remove(method);
    return this;
  }

  /**
   * @return true if the client will cache responses with the {@code Vary} header, false otherwise
   */
  public boolean isVaryCachingEnabled() {
    return enableVaryCaching;
  }

  @Override
  public CachingWebClientConfig setUserAgentEnabled(boolean userAgentEnabled) {
    return (CachingWebClientConfig) super.setUserAgentEnabled(userAgentEnabled);
  }

  @Override
  public CachingWebClientConfig setUserAgent(String userAgent) {
    return (CachingWebClientConfig) super.setUserAgent(userAgent);
  }

  @Override
  public CachingWebClientConfig setFollowRedirects(boolean followRedirects) {
    return (CachingWebClientConfig) super.setFollowRedirects(followRedirects);
  }

  @Override
  public CachingWebClientConfig setTemplateExpandOptions(ExpandOptions templateExpandOptions) {
    return (CachingWebClientConfig) super.setTemplateExpandOptions(templateExpandOptions);
  }

  @Override
  public CachingWebClientConfig setSslOptions(ClientSSLOptions sslOptions) {
    return (CachingWebClientConfig) super.setSslOptions(sslOptions);
  }

  @Override
  public CachingWebClientConfig setMetricsName(String metricsName) {
    return (CachingWebClientConfig) super.setMetricsName(metricsName);
  }

  @Override
  public CachingWebClientConfig setConnectTimeout(Duration connectTimeout) {
    return (CachingWebClientConfig) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public CachingWebClientConfig setIdleTimeout(Duration idleTimeout) {
    return (CachingWebClientConfig) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public CachingWebClientConfig setReadIdleTimeout(Duration idleTimeout) {
    return (CachingWebClientConfig) super.setReadIdleTimeout(idleTimeout);
  }

  @Override
  public CachingWebClientConfig setWriteIdleTimeout(Duration idleTimeout) {
    return (CachingWebClientConfig) super.setWriteIdleTimeout(idleTimeout);
  }

  @Override
  public CachingWebClientConfig setSsl(boolean ssl) {
    return (CachingWebClientConfig) super.setSsl(ssl);
  }

  @Override
  public CachingWebClientConfig setVersions(List<HttpVersion> versions) {
    return (CachingWebClientConfig) super.setVersions(versions);
  }

  @Override
  public CachingWebClientConfig setHttp1Config(Http1ClientConfig config) {
    return (CachingWebClientConfig) super.setHttp1Config(config);
  }

  @Override
  public CachingWebClientConfig setHttp2Config(Http2ClientConfig config) {
    return (CachingWebClientConfig) super.setHttp2Config(config);
  }

  @Override
  public CachingWebClientConfig setHttp3Config(Http3ClientConfig config) {
    return (CachingWebClientConfig) super.setHttp3Config(config);
  }

  @Override
  public CachingWebClientConfig setVerifyHost(boolean verifyHost) {
    return (CachingWebClientConfig) super.setVerifyHost(verifyHost);
  }

  @Override
  public CachingWebClientConfig setDecompressionEnabled(boolean decompressionEnabled) {
    return (CachingWebClientConfig) super.setDecompressionEnabled(decompressionEnabled);
  }

  @Override
  public CachingWebClientConfig setDefaultHost(String defaultHost) {
    return (CachingWebClientConfig) super.setDefaultHost(defaultHost);
  }

  @Override
  public CachingWebClientConfig setDefaultPort(int defaultPort) {
    return (CachingWebClientConfig) super.setDefaultPort(defaultPort);
  }

  @Override
  public CachingWebClientConfig setMaxRedirects(int maxRedirects) {
    return (CachingWebClientConfig) super.setMaxRedirects(maxRedirects);
  }

  @Override
  public CachingWebClientConfig setForceSni(boolean forceSni) {
    return (CachingWebClientConfig) super.setForceSni(forceSni);
  }

  @Override
  public CachingWebClientConfig setTracingPolicy(TracingPolicy tracingPolicy) {
    return (CachingWebClientConfig) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public CachingWebClientConfig setShared(boolean shared) {
    return (CachingWebClientConfig) super.setShared(shared);
  }

  @Override
  public CachingWebClientConfig setName(String name) {
    return (CachingWebClientConfig) super.setName(name);
  }

  @Override
  public CachingWebClientConfig setFollowAlternativeServices(boolean followAlternativeServices) {
    return (CachingWebClientConfig) super.setFollowAlternativeServices(followAlternativeServices);
  }

  private static Set<Integer> buildDefaultStatusCodes() {
    Set<Integer> codes = new HashSet<>(3);
    Collections.addAll(codes, 200, 301, 404);
    return codes;
  }

  private static Set<HttpMethod> buildDefaultMethods() {
    Set<HttpMethod> methods = new HashSet<>(1);
    methods.add(HttpMethod.GET);
    return methods;
  }
}
