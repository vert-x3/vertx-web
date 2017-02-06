package io.vertx.kotlin.ext.web.client

import io.vertx.ext.web.client.WebClientOptions
import io.vertx.core.http.Http2Settings
import io.vertx.core.http.HttpVersion
import io.vertx.core.net.JdkSSLEngineOptions
import io.vertx.core.net.JksOptions
import io.vertx.core.net.OpenSSLEngineOptions
import io.vertx.core.net.PemKeyCertOptions
import io.vertx.core.net.PemTrustOptions
import io.vertx.core.net.PfxOptions
import io.vertx.core.net.ProxyOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.client.WebClientOptions] objects.
 *
 *
 * @param alpnVersions 
 * @param connectTimeout 
 * @param crlPaths 
 * @param crlValues 
 * @param defaultHost 
 * @param defaultPort 
 * @param enabledCipherSuites 
 * @param enabledSecureTransportProtocols 
 * @param followRedirects 
 * @param http2ClearTextUpgrade 
 * @param http2ConnectionWindowSize 
 * @param http2MaxPoolSize 
 * @param http2MultiplexingLimit 
 * @param idleTimeout 
 * @param initialSettings 
 * @param jdkSslEngineOptions 
 * @param keepAlive 
 * @param keyStoreOptions 
 * @param localAddress 
 * @param logActivity 
 * @param maxChunkSize 
 * @param maxHeaderSize 
 * @param maxInitialLineLength 
 * @param maxPoolSize 
 * @param maxRedirects 
 * @param maxWaitQueueSize 
 * @param maxWebsocketFrameSize 
 * @param metricsName 
 * @param openSslEngineOptions 
 * @param pemKeyCertOptions 
 * @param pemTrustOptions 
 * @param pfxKeyCertOptions 
 * @param pfxTrustOptions 
 * @param pipelining 
 * @param pipeliningLimit 
 * @param protocolVersion 
 * @param proxyOptions 
 * @param receiveBufferSize 
 * @param reuseAddress 
 * @param sendBufferSize 
 * @param sendUnmaskedFrames 
 * @param soLinger 
 * @param ssl 
 * @param tcpKeepAlive 
 * @param tcpNoDelay 
 * @param trafficClass 
 * @param trustAll 
 * @param trustStoreOptions 
 * @param tryUseCompression 
 * @param useAlpn 
 * @param usePooledBuffers 
 * @param userAgent  Sets the Web Client user agent header. Defaults to Vert.x-WebClient/&lt;version&gt;.
 * @param userAgentEnabled  Sets whether the Web Client should send a user agent header. Defaults to true.
 * @param verifyHost 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.client.WebClientOptions original] using Vert.x codegen.
 */
fun WebClientOptions(
  alpnVersions: Iterable<HttpVersion>? = null,
  connectTimeout: Int? = null,
  crlPaths: Iterable<String>? = null,
  crlValues: Iterable<io.vertx.core.buffer.Buffer>? = null,
  defaultHost: String? = null,
  defaultPort: Int? = null,
  enabledCipherSuites: Iterable<String>? = null,
  enabledSecureTransportProtocols: Iterable<String>? = null,
  followRedirects: Boolean? = null,
  http2ClearTextUpgrade: Boolean? = null,
  http2ConnectionWindowSize: Int? = null,
  http2MaxPoolSize: Int? = null,
  http2MultiplexingLimit: Int? = null,
  idleTimeout: Int? = null,
  initialSettings: io.vertx.core.http.Http2Settings? = null,
  jdkSslEngineOptions: io.vertx.core.net.JdkSSLEngineOptions? = null,
  keepAlive: Boolean? = null,
  keyStoreOptions: io.vertx.core.net.JksOptions? = null,
  localAddress: String? = null,
  logActivity: Boolean? = null,
  maxChunkSize: Int? = null,
  maxHeaderSize: Int? = null,
  maxInitialLineLength: Int? = null,
  maxPoolSize: Int? = null,
  maxRedirects: Int? = null,
  maxWaitQueueSize: Int? = null,
  maxWebsocketFrameSize: Int? = null,
  metricsName: String? = null,
  openSslEngineOptions: io.vertx.core.net.OpenSSLEngineOptions? = null,
  pemKeyCertOptions: io.vertx.core.net.PemKeyCertOptions? = null,
  pemTrustOptions: io.vertx.core.net.PemTrustOptions? = null,
  pfxKeyCertOptions: io.vertx.core.net.PfxOptions? = null,
  pfxTrustOptions: io.vertx.core.net.PfxOptions? = null,
  pipelining: Boolean? = null,
  pipeliningLimit: Int? = null,
  protocolVersion: HttpVersion? = null,
  proxyOptions: io.vertx.core.net.ProxyOptions? = null,
  receiveBufferSize: Int? = null,
  reuseAddress: Boolean? = null,
  sendBufferSize: Int? = null,
  sendUnmaskedFrames: Boolean? = null,
  soLinger: Int? = null,
  ssl: Boolean? = null,
  tcpKeepAlive: Boolean? = null,
  tcpNoDelay: Boolean? = null,
  trafficClass: Int? = null,
  trustAll: Boolean? = null,
  trustStoreOptions: io.vertx.core.net.JksOptions? = null,
  tryUseCompression: Boolean? = null,
  useAlpn: Boolean? = null,
  usePooledBuffers: Boolean? = null,
  userAgent: String? = null,
  userAgentEnabled: Boolean? = null,
  verifyHost: Boolean? = null): WebClientOptions = io.vertx.ext.web.client.WebClientOptions().apply {

  if (alpnVersions != null) {
    this.setAlpnVersions(alpnVersions.toList())
  }
  if (connectTimeout != null) {
    this.setConnectTimeout(connectTimeout)
  }
  if (crlPaths != null) {
    for (item in crlPaths) {
      this.addCrlPath(item)
    }
  }
  if (crlValues != null) {
    for (item in crlValues) {
      this.addCrlValue(item)
    }
  }
  if (defaultHost != null) {
    this.setDefaultHost(defaultHost)
  }
  if (defaultPort != null) {
    this.setDefaultPort(defaultPort)
  }
  if (enabledCipherSuites != null) {
    for (item in enabledCipherSuites) {
      this.addEnabledCipherSuite(item)
    }
  }
  if (enabledSecureTransportProtocols != null) {
    for (item in enabledSecureTransportProtocols) {
      this.addEnabledSecureTransportProtocol(item)
    }
  }
  if (followRedirects != null) {
    this.setFollowRedirects(followRedirects)
  }
  if (http2ClearTextUpgrade != null) {
    this.setHttp2ClearTextUpgrade(http2ClearTextUpgrade)
  }
  if (http2ConnectionWindowSize != null) {
    this.setHttp2ConnectionWindowSize(http2ConnectionWindowSize)
  }
  if (http2MaxPoolSize != null) {
    this.setHttp2MaxPoolSize(http2MaxPoolSize)
  }
  if (http2MultiplexingLimit != null) {
    this.setHttp2MultiplexingLimit(http2MultiplexingLimit)
  }
  if (idleTimeout != null) {
    this.setIdleTimeout(idleTimeout)
  }
  if (initialSettings != null) {
    this.setInitialSettings(initialSettings)
  }
  if (jdkSslEngineOptions != null) {
    this.setJdkSslEngineOptions(jdkSslEngineOptions)
  }
  if (keepAlive != null) {
    this.setKeepAlive(keepAlive)
  }
  if (keyStoreOptions != null) {
    this.setKeyStoreOptions(keyStoreOptions)
  }
  if (localAddress != null) {
    this.setLocalAddress(localAddress)
  }
  if (logActivity != null) {
    this.setLogActivity(logActivity)
  }
  if (maxChunkSize != null) {
    this.setMaxChunkSize(maxChunkSize)
  }
  if (maxHeaderSize != null) {
    this.setMaxHeaderSize(maxHeaderSize)
  }
  if (maxInitialLineLength != null) {
    this.setMaxInitialLineLength(maxInitialLineLength)
  }
  if (maxPoolSize != null) {
    this.setMaxPoolSize(maxPoolSize)
  }
  if (maxRedirects != null) {
    this.setMaxRedirects(maxRedirects)
  }
  if (maxWaitQueueSize != null) {
    this.setMaxWaitQueueSize(maxWaitQueueSize)
  }
  if (maxWebsocketFrameSize != null) {
    this.setMaxWebsocketFrameSize(maxWebsocketFrameSize)
  }
  if (metricsName != null) {
    this.setMetricsName(metricsName)
  }
  if (openSslEngineOptions != null) {
    this.setOpenSslEngineOptions(openSslEngineOptions)
  }
  if (pemKeyCertOptions != null) {
    this.setPemKeyCertOptions(pemKeyCertOptions)
  }
  if (pemTrustOptions != null) {
    this.setPemTrustOptions(pemTrustOptions)
  }
  if (pfxKeyCertOptions != null) {
    this.setPfxKeyCertOptions(pfxKeyCertOptions)
  }
  if (pfxTrustOptions != null) {
    this.setPfxTrustOptions(pfxTrustOptions)
  }
  if (pipelining != null) {
    this.setPipelining(pipelining)
  }
  if (pipeliningLimit != null) {
    this.setPipeliningLimit(pipeliningLimit)
  }
  if (protocolVersion != null) {
    this.setProtocolVersion(protocolVersion)
  }
  if (proxyOptions != null) {
    this.setProxyOptions(proxyOptions)
  }
  if (receiveBufferSize != null) {
    this.setReceiveBufferSize(receiveBufferSize)
  }
  if (reuseAddress != null) {
    this.setReuseAddress(reuseAddress)
  }
  if (sendBufferSize != null) {
    this.setSendBufferSize(sendBufferSize)
  }
  if (sendUnmaskedFrames != null) {
    this.setSendUnmaskedFrames(sendUnmaskedFrames)
  }
  if (soLinger != null) {
    this.setSoLinger(soLinger)
  }
  if (ssl != null) {
    this.setSsl(ssl)
  }
  if (tcpKeepAlive != null) {
    this.setTcpKeepAlive(tcpKeepAlive)
  }
  if (tcpNoDelay != null) {
    this.setTcpNoDelay(tcpNoDelay)
  }
  if (trafficClass != null) {
    this.setTrafficClass(trafficClass)
  }
  if (trustAll != null) {
    this.setTrustAll(trustAll)
  }
  if (trustStoreOptions != null) {
    this.setTrustStoreOptions(trustStoreOptions)
  }
  if (tryUseCompression != null) {
    this.setTryUseCompression(tryUseCompression)
  }
  if (useAlpn != null) {
    this.setUseAlpn(useAlpn)
  }
  if (usePooledBuffers != null) {
    this.setUsePooledBuffers(usePooledBuffers)
  }
  if (userAgent != null) {
    this.setUserAgent(userAgent)
  }
  if (userAgentEnabled != null) {
    this.setUserAgentEnabled(userAgentEnabled)
  }
  if (verifyHost != null) {
    this.setVerifyHost(verifyHost)
  }
}

