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

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BasicAuthHandlerImpl extends AuthHandlerImpl {

  private final String realm;

  public BasicAuthHandlerImpl(AuthProvider authProvider, String realm) {
    super(authProvider);
    this.realm = realm;
  }

  @Override
  public void handle(RoutingContext context) {
    User user = context.user();
    if (user != null) {
      // Already authenticated in, just authorise
      authorise(user, context);
    } else {
      HttpServerRequest request = context.request();
      String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);

      if (authorization == null) {
        handle401(context);
      } else {
        String suser;
        String spass;
        String sscheme;

        try {
          String[] parts = authorization.split(" ");
          sscheme = parts[0];
          String decoded = new String(Base64.getDecoder().decode(parts[1]));          
          int colonIdx = decoded.indexOf(":");
          if(colonIdx!=-1) {
              suser = decoded.substring(0,colonIdx);
              spass = decoded.substring(colonIdx+1);
          } else {
              suser = decoded;
              spass = null;                      
          }          
        } catch (ArrayIndexOutOfBoundsException e) {
          handle401(context);
          return;
        } catch (IllegalArgumentException | NullPointerException e) {
          // IllegalArgumentException includes PatternSyntaxException
          context.fail(e);
          return;
        }

        if (!"Basic".equals(sscheme)) {
          context.fail(400);
        } else {
          JsonObject authInfo = new JsonObject().put("username", suser).put("password", spass);
          authProvider.authenticate(authInfo, res -> {
            if (res.succeeded()) {
              User authenticated = res.result();
              context.setUser(authenticated);
              authorise(authenticated, context);
            } else {
              handle401(context);
            }
          });
        }
      }
    }
  }

  private void handle401(RoutingContext context) {
    context.response().putHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    context.fail(401);
  }  
}