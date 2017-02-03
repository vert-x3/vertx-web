package io.vertx.kotlin.ext.web.client

import io.vertx.ext.web.client.WebClientOptions
import io.vertx.core.http.HttpClientOptions

/**
 * A function providing a DSL for building [io.vertx.ext.web.client.WebClientOptions] objects.
 *
 *
 * @param httpClientOptions  Sets the underlying [io.vertx.core.http.HttpClient] options. Not used when the Web Client is created with [io.vertx.ext.web.client.WebClient].
 * @param userAgent  Sets the Web Client user agent header. Defaults to <code>Vert.x-WebClient/&lt;version&gt;</code>.
 * @param userAgentEnabled  Sets whether the Web Client should send a user agent header. Defaults to <code>true</code>.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.ext.web.client.WebClientOptions original] using Vert.x codegen.
 */
fun WebClientOptions(
  httpClientOptions: io.vertx.core.http.HttpClientOptions? = null,
  userAgent: String? = null,
  userAgentEnabled: Boolean? = null): WebClientOptions = io.vertx.ext.web.client.WebClientOptions().apply {

  if (httpClientOptions != null) {
    this.setHttpClientOptions(httpClientOptions)
  }
  if (userAgent != null) {
    this.setUserAgent(userAgent)
  }
  if (userAgentEnabled != null) {
    this.setUserAgentEnabled(userAgentEnabled)
  }
}

