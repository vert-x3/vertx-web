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
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.DigestAuthHandler;

import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends HTTPAuthorizationHandler<AuthenticationProvider> implements BasicAuthHandler {

  public BasicAuthHandlerImpl(AuthenticationProvider authProvider, String realm) {
    super(authProvider, realm, Type.BASIC);
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler) {

    parseAuthorization(context, parseAuthorization -> {
      if (parseAuthorization.failed()) {
        handler.handle(Future.failedFuture(parseAuthorization.cause()));
        return;
      }

      final String suser;
      final String spass;

      try {
        // decode the payload
        String decoded = new String(Base64.getDecoder().decode(parseAuthorization.result()));

        int colonIdx = decoded.indexOf(":");
        if (colonIdx != -1) {
          suser = decoded.substring(0, colonIdx);
          spass = decoded.substring(colonIdx + 1);
        } else {
          suser = decoded;
          spass = null;
        }
      } catch (RuntimeException e) {
        context.fail(400,e);
        return;
      }

      handler.handle(Future.succeededFuture(new UsernamePasswordCredentials(suser, spass)));
    });
  }

  @Override
  public BasicAuthHandler postAuthenticationHandler(Handler<RoutingContext> handler) {
    super.postAuthenticationHandler(handler);
    return this;
  }
}
