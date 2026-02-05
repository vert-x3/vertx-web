/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.*;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.*;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.uritemplate.ExpandOptions;

import java.time.Duration;
import java.util.List;

/**
 * @author Thomas Segismont
 */
@DataObject
public class WebClientConfig extends HttpClientConfig {

  private boolean userAgentEnabled = WebClientOptions.DEFAULT_USER_AGENT_ENABLED;
  private String userAgent = WebClientOptions.DEFAULT_USER_AGENT;
  private boolean followRedirects = WebClientOptions.DEFAULT_FOLLOW_REDIRECTS;
  private ExpandOptions templateExpandOptions = WebClientOptions.DEFAULT_EXPAND_OPTIONS;

  public WebClientConfig() {
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public WebClientConfig(WebClientConfig other) {
    super(other);

    this.userAgentEnabled = other.userAgentEnabled;
    this.userAgent = other.userAgent;
    this.followRedirects = other.followRedirects;
    this.templateExpandOptions = other.templateExpandOptions != null ? new ExpandOptions(other.templateExpandOptions) : null;
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public WebClientConfig(HttpClientConfig other) {
    super(other);
  }

  WebClientConfig(WebClientOptions other) {
    super(other);
    init(other);
  }

  void init(WebClientOptions other) {
    this.userAgentEnabled = other.isUserAgentEnabled();
    this.userAgent = other.getUserAgent();
    this.followRedirects = other.isFollowRedirects();
    this.templateExpandOptions = other.getTemplateExpandOptions() != null ? new ExpandOptions(other.getTemplateExpandOptions()) : null;
  }

  void init(WebClientConfig other) {
    this.userAgentEnabled = other.isUserAgentEnabled();
    this.userAgent = other.getUserAgent();
    this.followRedirects = other.isFollowRedirects();
    this.templateExpandOptions = other.getTemplateExpandOptions() != null ? new ExpandOptions(other.getTemplateExpandOptions()) : null;
  }

  /**
   * @return true if the Web Client should send a user agent header, false otherwise
   */
  public boolean isUserAgentEnabled() {
    return userAgentEnabled;
  }

  /**
   * Sets whether the Web Client should send a user agent header. Defaults to true.
   *
   * @param userAgentEnabled true to send a user agent header, false otherwise
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientConfig setUserAgentEnabled(boolean userAgentEnabled) {
    this.userAgentEnabled = userAgentEnabled;
    return this;
  }

  /**
   * @return the user agent header string
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Sets the Web Client user agent header. Defaults to Vert.x-WebClient/&lt;version&gt;.
   *
   * @param userAgent user agent header value
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientConfig setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  /**
   * @return the default behavior of the client for following HTTP {@code 30x} redirections
   */
  public boolean isFollowRedirects() {
    return followRedirects;
  }

  /**
   * Configure the default behavior of the client to follow HTTP {@code 30x} redirections.
   *
   * @param followRedirects true when a redirect is followed
   * @return a reference to this, so the API can be used fluently
   */
  public WebClientConfig setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  public ExpandOptions getTemplateExpandOptions() {
    return templateExpandOptions;
  }

  public WebClientConfig setTemplateExpandOptions(ExpandOptions templateExpandOptions) {
    this.templateExpandOptions = templateExpandOptions;
    return this;
  }

  @Override
  public WebClientConfig setSslOptions(ClientSSLOptions sslOptions) {
    return (WebClientConfig) super.setSslOptions(sslOptions);
  }

  @Override
  public WebClientConfig setConnectTimeout(Duration connectTimeout) {
    return (WebClientConfig) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public WebClientConfig setMetricsName(String metricsName) {
    return (WebClientConfig) super.setMetricsName(metricsName);
  }

  @Override
  public WebClientConfig setIdleTimeout(Duration idleTimeout) {
    return (WebClientConfig) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientConfig setReadIdleTimeout(Duration idleTimeout) {
    return (WebClientConfig) super.setReadIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientConfig setWriteIdleTimeout(Duration idleTimeout) {
    return (WebClientConfig) super.setWriteIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientConfig setSsl(boolean ssl) {
    return (WebClientConfig) super.setSsl(ssl);
  }

  @Override
  public WebClientConfig setVersions(List<HttpVersion> versions) {
    return (WebClientConfig) super.setVersions(versions);
  }

  @Override
  public WebClientConfig setHttp1Config(Http1ClientConfig config) {
    return (WebClientConfig) super.setHttp1Config(config);
  }

  @Override
  public WebClientConfig setHttp2Config(Http2ClientConfig config) {
    return (WebClientConfig) super.setHttp2Config(config);
  }

  @Override
  public WebClientConfig setHttp3Config(Http3ClientConfig config) {
    return (WebClientConfig) super.setHttp3Config(config);
  }

  @Override
  public WebClientConfig setVerifyHost(boolean verifyHost) {
    return (WebClientConfig) super.setVerifyHost(verifyHost);
  }

  @Override
  public WebClientConfig setDecompressionEnabled(boolean decompressionEnabled) {
    return (WebClientConfig) super.setDecompressionEnabled(decompressionEnabled);
  }

  @Override
  public WebClientConfig setDefaultHost(String defaultHost) {
    return (WebClientConfig) super.setDefaultHost(defaultHost);
  }

  @Override
  public WebClientConfig setDefaultPort(int defaultPort) {
    return (WebClientConfig) super.setDefaultPort(defaultPort);
  }

  @Override
  public WebClientConfig setMaxRedirects(int maxRedirects) {
    return (WebClientConfig) super.setMaxRedirects(maxRedirects);
  }

  @Override
  public WebClientConfig setForceSni(boolean forceSni) {
    return (WebClientConfig) super.setForceSni(forceSni);
  }

  @Override
  public WebClientConfig setTracingPolicy(TracingPolicy tracingPolicy) {
    return (WebClientConfig) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public WebClientConfig setShared(boolean shared) {
    return (WebClientConfig) super.setShared(shared);
  }

  @Override
  public WebClientConfig setName(String name) {
    return (WebClientConfig) super.setName(name);
  }

  @Override
  public WebClientConfig setFollowAlternativeServices(boolean followAlternativeServices) {
    return (WebClientConfig) super.setFollowAlternativeServices(followAlternativeServices);
  }

  public static String loadUserAgent() {
    String userAgent = "Vert.x-WebClient";
    String version = VertxInternal.version();
    if (version.length() > 0) {
      userAgent += "/" + version;
    }
    return userAgent;
  }
}
