/*
 * Copyright 2014 Red Hat, Inc.
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
package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.Cookie;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.HttpException;

import java.util.function.Function;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class APIKeyHandlerImpl extends AuthenticationHandlerImpl<AuthenticationProvider> implements APIKeyHandler {

  enum Type {
    HEADER,
    PARAMETER,
    COOKIE
  }

  private Type source = Type.HEADER;
  private String value = "X-API-KEY";
  private Function<String, Future<String>> tokenExtractor = null;

  public APIKeyHandlerImpl(AuthenticationProvider authProvider) {
    super(authProvider);
  }


  @Override
  public APIKeyHandler header(String headerName) {
    if (headerName == null) {
      throw new IllegalArgumentException("'headerName' cannot be null");
    }
    source = Type.HEADER;
    value = headerName;
    return this;
  }

  @Override
  public APIKeyHandler parameter(String paramName) {
    if (paramName == null) {
      throw new IllegalArgumentException("'paramName' cannot be null");
    }
    source = Type.PARAMETER;
    value = paramName;
    return this;
  }

  @Override
  public APIKeyHandler cookie(String cookieName) {
    if (cookieName == null) {
      throw new IllegalArgumentException("'cookieName' cannot be null");
    }
    source = Type.COOKIE;
    value = cookieName;
    return this;
  }

  @Override
  public void authenticate(RoutingContext context, Handler<AsyncResult<User>> handler) {
    //fallback if no api key was found or extract token failed
    Future<String> tokenFuture = Future.failedFuture(UNAUTHORIZED);
    switch (source) {
      case HEADER:
        MultiMap headers = context.request().headers();
        if ((headers != null && headers.contains(value))) {
          tokenFuture = tokenExtractor != null
            ? tokenExtractor.apply(headers.get(value))
            : Future.succeededFuture(headers.get(value));
        }
        break;
      case PARAMETER:
        MultiMap params = context.request().params();
        if (params != null && params.contains(value)) {
          tokenFuture = tokenExtractor != null
            ? tokenExtractor.apply(params.get(value))
            : Future.succeededFuture(params.get(value));
        }
        break;
      case COOKIE:
        Cookie cookie = context.request().getCookie(value);
        if (cookie != null) {
          tokenFuture = tokenExtractor != null
            ? tokenExtractor.apply(cookie.getValue())
            : Future.succeededFuture(cookie.getValue());
        }
    }
    tokenFuture
      .compose(token -> authProvider.authenticate(new TokenCredentials(token)))
      .onComplete(authn -> {
        if (authn.failed()) {
          handler.handle(Future.failedFuture(new HttpException(401, authn.cause())));
        } else {
          handler.handle(authn);
        }
      });
  }

  @Override
  public APIKeyHandler tokenExtractor(Function<String, Future<String>> tokenExtractor) {
    this.tokenExtractor = tokenExtractor;
    return this;
  }
}
