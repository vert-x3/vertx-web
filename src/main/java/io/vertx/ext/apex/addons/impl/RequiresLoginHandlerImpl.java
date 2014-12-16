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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.addons.RequiresLoginHandler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.Session;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RequiresLoginHandlerImpl implements RequiresLoginHandler {

  private static final Logger log = LoggerFactory.getLogger(RequiresLoginHandlerImpl.class);

  private final String loginRedirectURL;
  private final String returnURLParam;

  public RequiresLoginHandlerImpl(String loginRedirectURL, String returnURLParam) {
    this.loginRedirectURL = loginRedirectURL;
    this.returnURLParam = returnURLParam;
  }

  @Override
  public void handle(RoutingContext context) {
    Session session = context.session();
    if (session != null) {
      if (session.isLoggedIn()) {
        // Already logged in
        context.next();
      } else {
        // Now redirect to the login url
        session.data().put(returnURLParam, context.request().path());
        context.response().putHeader("location", loginRedirectURL).setStatusCode(302).end();
      }
    } else {
      context.fail(new NullPointerException("No session - did you forget to include a SessionHandler?"));
    }

  }
}
