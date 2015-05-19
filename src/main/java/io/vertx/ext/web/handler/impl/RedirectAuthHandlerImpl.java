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

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RedirectAuthHandlerImpl extends AuthHandlerImpl {

  private static final Logger log = LoggerFactory.getLogger(RedirectAuthHandlerImpl.class);

  private final String loginRedirectURL;
  private final String returnURLParam;

  public RedirectAuthHandlerImpl(AuthProvider authProvider, String loginRedirectURL, String returnURLParam) {
    super (authProvider);
    this.loginRedirectURL = loginRedirectURL;
    this.returnURLParam = returnURLParam;
  }

  @Override
  public void handle(RoutingContext context) {
    Session session = context.session();
    if (session != null) {
      User user = UserHolder.getUser(authProvider, context);
      if (user != null) {
        // Already logged in, just authorise
        authorise(user, context);
      } else {
        // Now redirect to the login url - we'll get redirected back here after successful login
        session.put(returnURLParam, context.request().path());
        context.response().putHeader("location", loginRedirectURL).setStatusCode(302).end();
      }
    } else {
      context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
    }

  }
}
