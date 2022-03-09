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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.impl.launcher.commands.VersionCommand;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.uritemplate.ExpandOptions;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Segismont
 */
@DataObject(generateConverter = true)
public class WebClientOptions extends HttpClientOptions {

  /**
   * The default value of whether the Web Client should send a user agent header = true.
   */
  public static final boolean DEFAULT_USER_AGENT_ENABLED = true;

  /**
   * The default user agent string = Vert.x-WebClient/&lt;version&gt;.
   */
  public static final String DEFAULT_USER_AGENT = loadUserAgent();

  /**
   * The default value of whether the Web Client should follow redirects = true.
   */
  public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

  public static final ExpandOptions DEFAULT_EXPAND_OPTIONS = null;

  private boolean userAgentEnabled = DEFAULT_USER_AGENT_ENABLED;
  private String userAgent = DEFAULT_USER_AGENT;
  private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;
  private ExpandOptions templateExpandOptions = DEFAULT_EXPAND_OPTIONS;

  public WebClientOptions() {
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public WebClientOptions(WebClientOptions other) {
    super(other);
    init(other);
  }

  /**
   * Copy constructor using {@link HttpClientOptions}.
   *
   * @param other the options to copy
   */
  public WebClientOptions(HttpClientOptions other) {
    super(other);
  }

  /**
   * Creates a new instance from JSON.
   *
   * @param json the JSON object
   */
  public WebClientOptions(JsonObject json) {
    super(json);
    WebClientOptionsConverter.fromJson(json, this);
  }

  void init(WebClientOptions other) {
    this.userAgentEnabled = other.userAgentEnabled;
    this.userAgent = other.userAgent;
    this.followRedirects = other.followRedirects;
    this.templateExpandOptions = other.templateExpandOptions != null ? new ExpandOptions(other.templateExpandOptions) : null;
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    WebClientOptionsConverter.toJson(this, json);
    return json;
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
  public WebClientOptions setUserAgentEnabled(boolean userAgentEnabled) {
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
  public WebClientOptions setUserAgent(String userAgent) {
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
  public WebClientOptions setFollowRedirects(boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  public ExpandOptions getTemplateExpandOptions() {
    return templateExpandOptions;
  }

  public WebClientOptions setTemplateExpandOptions(ExpandOptions templateExpandOptions) {
    this.templateExpandOptions = templateExpandOptions;
    return this;
  }

  @Override
  public WebClientOptions setMaxRedirects(int maxRedirects) {
    return (WebClientOptions) super.setMaxRedirects(maxRedirects);
  }

  @Override
  public WebClientOptions setSendBufferSize(int sendBufferSize) {
    return (WebClientOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public WebClientOptions setReceiveBufferSize(int receiveBufferSize) {
    return (WebClientOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public WebClientOptions setReuseAddress(boolean reuseAddress) {
    return (WebClientOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public WebClientOptions setTrafficClass(int trafficClass) {
    return (WebClientOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public WebClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (WebClientOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public WebClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (WebClientOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public WebClientOptions setSoLinger(int soLinger) {
    return (WebClientOptions) super.setSoLinger(soLinger);
  }

  @Override
  public WebClientOptions setIdleTimeout(int idleTimeout) {
    return (WebClientOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientOptions setIdleTimeoutUnit(TimeUnit idleTimeoutUnit) {
    return (WebClientOptions) super.setIdleTimeoutUnit(idleTimeoutUnit);
  }

  @Override
  public WebClientOptions setSsl(boolean ssl) {
    return (WebClientOptions) super.setSsl(ssl);
  }

  @Override
  public WebClientOptions setKeyCertOptions(KeyCertOptions options) {
    return (WebClientOptions) super.setKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setKeyStoreOptions(JksOptions options) {
    return (WebClientOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public WebClientOptions setPfxKeyCertOptions(PfxOptions options) {
    return (WebClientOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setTrustOptions(TrustOptions options) {
    return (WebClientOptions) super.setTrustOptions(options);
  }

  @Override
  public WebClientOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (WebClientOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public WebClientOptions setTrustStoreOptions(JksOptions options) {
    return (WebClientOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public WebClientOptions setPfxTrustOptions(PfxOptions options) {
    return (WebClientOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public WebClientOptions setPemTrustOptions(PemTrustOptions options) {
    return (WebClientOptions) super.setPemTrustOptions(options);
  }

  @Override
  public WebClientOptions addEnabledCipherSuite(String suite) {
    return (WebClientOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public WebClientOptions addCrlPath(String crlPath) throws NullPointerException {
    return (WebClientOptions) super.addCrlPath(crlPath);
  }

  @Override
  public WebClientOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (WebClientOptions) super.addCrlValue(crlValue);
  }

  @Override
  public WebClientOptions setConnectTimeout(int connectTimeout) {
    return (WebClientOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public WebClientOptions setTrustAll(boolean trustAll) {
    return (WebClientOptions) super.setTrustAll(trustAll);
  }

  @Override
  public WebClientOptions setMaxPoolSize(int maxPoolSize) {
    return (WebClientOptions) super.setMaxPoolSize(maxPoolSize);
  }

  @Override
  public WebClientOptions setHttp2MultiplexingLimit(int limit) {
    return (WebClientOptions) super.setHttp2MultiplexingLimit(limit);
  }

  @Override
  public WebClientOptions setHttp2MaxPoolSize(int max) {
    return (WebClientOptions) super.setHttp2MaxPoolSize(max);
  }

  @Override
  public WebClientOptions setHttp2ConnectionWindowSize(int http2ConnectionWindowSize) {
    return (WebClientOptions) super.setHttp2ConnectionWindowSize(http2ConnectionWindowSize);
  }

  @Override
  public WebClientOptions setKeepAlive(boolean keepAlive) {
    return (WebClientOptions) super.setKeepAlive(keepAlive);
  }

  @Override
  public WebClientOptions setPipelining(boolean pipelining) {
    return (WebClientOptions) super.setPipelining(pipelining);
  }

  @Override
  public WebClientOptions setPipeliningLimit(int limit) {
    return (WebClientOptions) super.setPipeliningLimit(limit);
  }

  @Override
  public WebClientOptions setVerifyHost(boolean verifyHost) {
    return (WebClientOptions) super.setVerifyHost(verifyHost);
  }

  @Override
  public WebClientOptions setTryUseCompression(boolean tryUseCompression) {
    return (WebClientOptions) super.setTryUseCompression(tryUseCompression);
  }

  @Override
  public WebClientOptions setSendUnmaskedFrames(boolean sendUnmaskedFrames) {
    return (WebClientOptions) super.setSendUnmaskedFrames(sendUnmaskedFrames);
  }

  @Override
  public WebClientOptions setMaxWebSocketFrameSize(int maxWebsocketFrameSize) {
    return (WebClientOptions) super.setMaxWebSocketFrameSize(maxWebsocketFrameSize);
  }

  @Override
  public WebClientOptions setDefaultHost(String defaultHost) {
    return (WebClientOptions) super.setDefaultHost(defaultHost);
  }

  @Override
  public WebClientOptions setDefaultPort(int defaultPort) {
    return (WebClientOptions) super.setDefaultPort(defaultPort);
  }

  @Override
  public WebClientOptions setMaxChunkSize(int maxChunkSize) {
    return (WebClientOptions) super.setMaxChunkSize(maxChunkSize);
  }

  @Override
  public WebClientOptions setProtocolVersion(HttpVersion protocolVersion) {
    return (WebClientOptions) super.setProtocolVersion(protocolVersion);
  }

  @Override
  public WebClientOptions setMaxHeaderSize(int maxHeaderSize) {
    return (WebClientOptions) super.setMaxHeaderSize(maxHeaderSize);
  }

  @Override
  public WebClientOptions setMaxWaitQueueSize(int maxWaitQueueSize) {
    return (WebClientOptions) super.setMaxWaitQueueSize(maxWaitQueueSize);
  }

  @Override
  public WebClientOptions setUseAlpn(boolean useAlpn) {
    return (WebClientOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public WebClientOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (WebClientOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public WebClientOptions setHttp2ClearTextUpgrade(boolean value) {
    return (WebClientOptions) super.setHttp2ClearTextUpgrade(value);
  }

  @Override
  public WebClientOptions setAlpnVersions(List<HttpVersion> alpnVersions) {
    return (WebClientOptions) super.setAlpnVersions(alpnVersions);
  }

  @Override
  public WebClientOptions setMetricsName(String metricsName) {
    return (WebClientOptions) super.setMetricsName(metricsName);
  }

  @Override
  public WebClientOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (WebClientOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public WebClientOptions setLocalAddress(String localAddress) {
    return (WebClientOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public WebClientOptions setLogActivity(boolean logEnabled) {
    return (WebClientOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public WebClientOptions addEnabledSecureTransportProtocol(String protocol) {
    return (WebClientOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public WebClientOptions removeEnabledSecureTransportProtocol(String protocol) {
    return (WebClientOptions) super.removeEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public WebClientOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (WebClientOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
  }

  @Override
  public WebClientOptions setReusePort(boolean reusePort) {
    return (WebClientOptions) super.setReusePort(reusePort);
  }

  @Override
  public WebClientOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (WebClientOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public WebClientOptions setTcpCork(boolean tcpCork) {
    return (WebClientOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public WebClientOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (WebClientOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public WebClientOptions setHttp2KeepAliveTimeout(int keepAliveTimeout) {
    return (WebClientOptions) super.setHttp2KeepAliveTimeout(keepAliveTimeout);
  }

  @Override
  public WebClientOptions setForceSni(boolean forceSni) {
    return (WebClientOptions) super.setForceSni(forceSni);
  }

  @Override
  public WebClientOptions setDecoderInitialBufferSize(int decoderInitialBufferSize) {
    return (WebClientOptions) super.setDecoderInitialBufferSize(decoderInitialBufferSize);
  }

  @Override
  public WebClientOptions setPoolCleanerPeriod(int poolCleanerPeriod) {
    return (WebClientOptions) super.setPoolCleanerPeriod(poolCleanerPeriod);
  }

  @Override
  public WebClientOptions setKeepAliveTimeout(int keepAliveTimeout) {
    return (WebClientOptions) super.setKeepAliveTimeout(keepAliveTimeout);
  }

  @Override
  public WebClientOptions setMaxWebSocketMessageSize(int maxWebsocketMessageSize) {
    return (WebClientOptions) super.setMaxWebSocketMessageSize(maxWebsocketMessageSize);
  }

  @Override
  public WebClientOptions setMaxInitialLineLength(int maxInitialLineLength) {
    return (WebClientOptions) super.setMaxInitialLineLength(maxInitialLineLength);
  }

  @Override
  public WebClientOptions setInitialSettings(Http2Settings settings) {
    return (WebClientOptions) super.setInitialSettings(settings);
  }

  @Override
  public WebClientOptions setSslHandshakeTimeout(long sslHandshakeTimeout) {
    return (WebClientOptions) super.setSslHandshakeTimeout(sslHandshakeTimeout);
  }

  @Override
  public WebClientOptions setSslHandshakeTimeoutUnit(TimeUnit sslHandshakeTimeoutUnit) {
    return (WebClientOptions) super.setSslHandshakeTimeoutUnit(sslHandshakeTimeoutUnit);
  }

  @Override
  public WebClientOptions setTryUsePerFrameWebSocketCompression(boolean offer) {
    return (WebClientOptions) super.setTryUsePerFrameWebSocketCompression(offer);
  }

  @Override
  public WebClientOptions setTryUsePerMessageWebSocketCompression(boolean offer) {
    return (WebClientOptions) super.setTryUsePerMessageWebSocketCompression(offer);
  }

  @Override
  public WebClientOptions setWebSocketCompressionLevel(int compressionLevel) {
    return (WebClientOptions) super.setWebSocketCompressionLevel(compressionLevel);
  }

  @Override
  public WebClientOptions setWebSocketCompressionAllowClientNoContext(boolean offer) {
    return (WebClientOptions) super.setWebSocketCompressionAllowClientNoContext(offer);
  }

  @Override
  public WebClientOptions setWebSocketCompressionRequestServerNoContext(boolean offer) {
    return (WebClientOptions) super.setWebSocketCompressionRequestServerNoContext(offer);
  }

  @Override
  public WebClientOptions setReadIdleTimeout(int idleTimeout) {
    return (WebClientOptions) super.setReadIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientOptions setWriteIdleTimeout(int idleTimeout) {
    return (WebClientOptions) super.setWriteIdleTimeout(idleTimeout);
  }

  @Override
  public WebClientOptions setMaxWebSockets(int maxWebSockets) {
    return (WebClientOptions) super.setMaxWebSockets(maxWebSockets);
  }

  @Override
  public WebClientOptions setNonProxyHosts(List<String> nonProxyHosts) {
    return (WebClientOptions) super.setNonProxyHosts(nonProxyHosts);
  }

  @Override
  public WebClientOptions addNonProxyHost(String nonProxyHost) {
    return (WebClientOptions) super.addNonProxyHost(nonProxyHost);
  }

  @Override
  public WebClientOptions setWebSocketClosingTimeout(int webSocketClosingTimeout) {
    return (WebClientOptions) super.setWebSocketClosingTimeout(webSocketClosingTimeout);
  }

  @Override
  public WebClientOptions setTracingPolicy(TracingPolicy tracingPolicy) {
    return (WebClientOptions) super.setTracingPolicy(tracingPolicy);
  }

  @Override
  public WebClientOptions setPoolEventLoopSize(int poolEventLoopSize) {
    return (WebClientOptions) super.setPoolEventLoopSize(poolEventLoopSize);
  }

  @Override
  public WebClientOptions setShared(boolean shared) {
    return (WebClientOptions) super.setShared(shared);
  }

  @Override
  public WebClientOptions setName(String name) {
    return (WebClientOptions) super.setName(name);
  }

  public static String loadUserAgent() {
    String userAgent = "Vert.x-WebClient";
    String version = VersionCommand.getVersion();
    if (version.length() > 0) {
      userAgent += "/" + version;
    }
    return userAgent;
  }
}
