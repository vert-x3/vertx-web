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
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.HttpException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends HTTPAuthorizationHandler<AuthenticationProvider> implements BasicAuthHandler {

  public BasicAuthHandlerImpl(AuthenticationProvider authProvider, String realm) {
    super(authProvider, Type.BASIC, realm);
  }

  @Override
  public void authenticate(RoutingContext context, Handler<AsyncResult<User>> handler) {

    parseAuthorization(context, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      final String suser;
      final String spass;

      try {
        // decode the payload
        String decoded = new String(Base64.getDecoder().decode(parseAuthorization.result()), StandardCharsets.UTF_8);

        int colonIdx = decoded.indexOf(":");
        if (colonIdx != -1) {
          suser = decoded.substring(0, colonIdx);
          spass = decoded.substring(colonIdx + 1);
        } else {
          suser = decoded;
          spass = null;
        }
      } catch (RuntimeException e) {
        handler.handle(Future.failedFuture(new HttpException(400, e)));
        return;
      }

      authProvider.authenticate(new UsernamePasswordCredentials(suser, spass), authn -> {
        if (authn.failed()) {
          handler.handle(Future.failedFuture(new HttpException(401, authn.cause())));
        } else {
          handler.handle(authn);
        }
      });
    });
  }
}
