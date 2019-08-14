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
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.auth.AuthProvider;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class RedirectAuthHandlerImpl extends AuthHandlerImpl {

  private final String loginRedirectURL;
  private final String returnURLParam;

  public RedirectAuthHandlerImpl(AuthProvider authProvider, String loginRedirectURL, String returnURLParam) {
    super (authProvider);
    this.loginRedirectURL = loginRedirectURL;
    this.returnURLParam = returnURLParam;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    Session session = context.session();
    if (session != null) {
      // Now redirect to the login url - we'll get redirected back here after successful login
      session.put(returnURLParam, context.request().uri());
      handler.handle(Future.failedFuture(new HttpStatusException(302, loginRedirectURL)));
    } else {
      handler.handle(Future.failedFuture("No session - did you forget to include a SessionHandler?"));
    }
  }
}
