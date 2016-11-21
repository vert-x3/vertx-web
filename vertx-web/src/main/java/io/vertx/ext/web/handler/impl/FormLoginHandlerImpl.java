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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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

  private String usernameParam;
  private String passwordParam;
  private String returnURLParam;
  private String directLoggedInOKURL;

  @Override
  public FormLoginHandler setUsernameParam(String usernameParam) {
    this.usernameParam = usernameParam;
    return this;
  }

  @Override
  public FormLoginHandler setPasswordParam(String passwordParam) {
    this.passwordParam = passwordParam;
    return this;
  }

  @Override
  public FormLoginHandler setReturnURLParam(String returnURLParam) {
    this.returnURLParam = returnURLParam;
    return this;
  }

  @Override
  public FormLoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
    this.directLoggedInOKURL = directLoggedInOKURL;
    return this;
  }

  public FormLoginHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam,
                              String returnURLParam, String directLoggedInOKURL) {
    this.authProvider = authProvider;
    this.usernameParam = usernameParam;
    this.passwordParam = passwordParam;
    this.returnURLParam = returnURLParam;
    this.directLoggedInOKURL = directLoggedInOKURL;
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
        JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
        authProvider.authenticate(authInfo, res -> {
          if (res.succeeded()) {
            User user = res.result();
            context.setUser(user);
            if (session != null) {
              // the user has upgraded from unauthenticated to authenticated
              // session should be upgraded as recommended by owasp
              session.regenerateId();

              String returnURL = session.remove(returnURLParam);
              if (returnURL != null) {
                // Now redirect back to the original url
                doRedirect(req.response(), returnURL);
                return;
              }
            }
            // Either no session or no return url
            if (directLoggedInOKURL != null) {
              // Redirect to the default logged in OK page - this would occur
              // if the user logged in directly at this URL without being redirected here first from another
              // url
              doRedirect(req.response(), directLoggedInOKURL);
            } else {
              // Just show a basic page
              req.response().end(DEFAULT_DIRECT_LOGGED_IN_OK_PAGE);
            }
          } else {
            context.fail(403);  // Failed login
          }
        });
      }
    }
  }

  private void doRedirect(HttpServerResponse response, String url) {
    response.putHeader("location", url).setStatusCode(302).end();
  }

  private static final String DEFAULT_DIRECT_LOGGED_IN_OK_PAGE = "" +
    "<html><body><h1>Login successful</h1></body></html>";
}
