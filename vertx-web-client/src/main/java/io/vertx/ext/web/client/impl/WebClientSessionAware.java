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
package io.vertx.ext.web.client.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import io.vertx.ext.web.client.spi.CookieStore;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class WebClientSessionAware extends WebClientBase implements WebClientSession {

  private final CookieStore cookieStore;
  private MultiMap headers;
  final OAuth2Auth oAuth2Auth;
  private String securityHeader;
  private User user;
  private JsonObject tokenConfig;
  protected boolean withAuthentication;

  public WebClientSessionAware(WebClient webClient, CookieStore cookieStore, OAuth2Auth oAuth2Auth) {
    super((WebClientBase) webClient);
    this.cookieStore = cookieStore;
    this.oAuth2Auth = oAuth2Auth;
    addInterceptor(new SessionAwareInterceptor());
  }

  public CookieStore cookieStore() {
    return cookieStore;
  }

  @Override
  public WebClientSession withAuthentication(JsonObject tokenConfig) {

    if (oAuth2Auth == null) {
      throw new NullPointerException("Can not obtain required authentication for request as oAuth2Auth provider is null");
    }

    if (tokenConfig == null) {
      throw new NullPointerException("Token Configuration passed to WebClientSessionAware can not be null");
    }

    if (this.tokenConfig != null && !this.tokenConfig.equals(tokenConfig)) {
      //We need to invalidate the current data as new configuration is passed
      user = null;
      securityHeader = null;
    }

    this.tokenConfig = tokenConfig;

    if (user != null) {
      if (user.expired()) {
        //Token has expired we need to invalidate the session
        oAuth2Auth.refresh(user)
          .onSuccess(userResult -> {
            user = userResult;
            securityHeader = userResult.principal().getString("access_token");
            this.withAuthentication = true;
          })
          .onFailure(error -> {
            // Refresh token failed, we can try standard authentication
            oAuth2Auth.authenticate(tokenConfig)
              .onSuccess(userResult -> {
                user = userResult;
                securityHeader = userResult.principal().getString("access_token");
                this.withAuthentication = true;
              })
              .onFailure(errorAuth -> {
                //Refresh token did not work and failed to obtain new authentication token, we need to fail
                user = null;
                securityHeader = null;
                this.withAuthentication = false;
                throw new RuntimeException(errorAuth);
              });
          });
      }
    } else {
      oAuth2Auth.authenticate(tokenConfig)
        .onSuccess(userResult -> {
          this.user = userResult;
          this.securityHeader = userResult.principal().getString("access_token");
          this.withAuthentication = true;
        })
        .onFailure(errorAuth -> {
          securityHeader = null;
          this.withAuthentication = false;
          throw new RuntimeException(errorAuth);
        });
    }
    return this;
  }

  protected MultiMap headers() {
    if (headers == null) {
      headers = HttpHeaders.headers();
    }
    return headers;
  }

  protected User getUser() {
    return user;
  }

  protected String getSecurityHeader() {
    return securityHeader;
  }

  @Override
  public WebClientSession addHeader(CharSequence name, CharSequence value) {
    headers().add(name, value);
    return this;
  }

  @Override
  public WebClientSession addHeader(String name, String value) {
    headers().add(name, value);
    return this;
  }

  @Override
  public WebClientSession addHeader(CharSequence name, Iterable<CharSequence> values) {
    headers().add(name, values);
    return this;
  }

  @Override
  public WebClientSession addHeader(String name, Iterable<String> values) {
    headers().add(name, values);
    return this;
  }

  @Override
  public WebClientSession removeHeader(CharSequence name) {
    headers().remove(name);
    return this;
  }

  @Override
  public WebClientSession removeHeader(String name) {
    headers().remove(name);
    return this;
  }

}
