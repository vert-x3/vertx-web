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

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FormLoginHandlerImpl implements FormLoginHandler {

  private static final Logger log = LoggerFactory.getLogger(FormLoginHandlerImpl.class);

  private final AuthProvider authProvider;

  private final String usernameParam;
  private final String passwordParam;
  private final String returnURLParam;

  public FormLoginHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam, String returnURLParam) {
    this.authProvider = authProvider;
    this.usernameParam = usernameParam;
    this.passwordParam = passwordParam;
    this.returnURLParam = returnURLParam;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest req = context.request();
    if (req.method() != HttpMethod.POST) {
      context.fail(405); // Must be a POST
    } else {
      if (!req.isExpectMultipart()) {
        throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
      }
      MultiMap params = req.formAttributes();
      String username = params.get(usernameParam);
      String password = params.get(passwordParam);
      if (username == null || password == null) {
        log.warn("No username or password provided in form - did you forget to include a BodyHandler?");
        context.fail(400);
      } else {
        Session session = context.session();
        if (session == null) {
          context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
        } else {
          JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
          authProvider.authenticate(authInfo, res -> {
            if (res.succeeded()) {
              User user = res.result();
              context.setUser(user);
              String returnURL = session.remove(returnURLParam);
              if (returnURL == null) {
                context.fail(new IllegalStateException("Logged in OK, but no return URL"));
              } else {
                // Now redirect back to the original url
                req.response().putHeader("location", returnURL).setStatusCode(302).end();
              }
            } else {
              context.fail(403);  // Failed login
            }
          });
        }

      }
    }
  }
}
