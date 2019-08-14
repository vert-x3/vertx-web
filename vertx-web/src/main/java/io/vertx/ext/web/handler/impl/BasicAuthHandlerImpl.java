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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.AuthProviderInternal;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.AuthProvider;

import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends AuthorizationAuthHandler {

  /**
   * This is a verification step, it can abort the instantiation by
   * throwing a RuntimeException
   */
  private static AuthProvider verifyProvider(AuthProvider provider) {
    if (provider instanceof AuthProviderInternal) {
      ((AuthProviderInternal)provider).verifyIsUsingPassword();
    }
    return provider;
  }

  public BasicAuthHandlerImpl(AuthProvider authProvider, String realm) {
    super(verifyProvider(authProvider), realm, Type.BASIC);
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {

    parseAuthorization(context, false, parseAuthorization -> {
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
        // IllegalArgumentException includes PatternSyntaxException
        context.fail(e);
        return;
      }

      handler.handle(Future.succeededFuture(new JsonObject().put("username", suser).put("password", spass)));
    });
  }

  @Override
  protected String authenticateHeader(RoutingContext context) {
    return "Basic realm=\"" + realm + "\"";
  }
}
