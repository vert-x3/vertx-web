/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.client.impl.WebClientOauth2Aware;

@VertxGen
public interface WebClientOAuth2 extends WebClient {

  /**
   * Create a session aware web client using the provided {@code webClient} instance.
   *
   * @param webClient the web client instance
   * @param oAuth2Auth Configured oAuth2Auth provider to be used when {@link #withCredentials(Credentials)} used
   * @return the created client
   */
  static WebClientOAuth2 create(WebClient webClient, OAuth2Auth oAuth2Auth) {
    return new WebClientOauth2Aware(webClient, oAuth2Auth);
  }

  @Fluent
  WebClientOAuth2 leeway(int seconds);

  /**
   * Mark that request should be dispatched with authentication obtained from passed {@code OAuth2Auth} provider
   *
   * @return a reference to this, so the API can be used fluently
   */

  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  WebClientOAuth2 withCredentials(Credentials credentials);
}
