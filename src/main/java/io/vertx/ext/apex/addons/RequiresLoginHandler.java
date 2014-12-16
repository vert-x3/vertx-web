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

package io.vertx.ext.apex.addons;

import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.RequiresLoginHandlerImpl;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface RequiresLoginHandler extends Handler<RoutingContext> {

  static final String DEFAULT_LOGIN_REDIRECT_URL = "/loginpage";
  static final String DEFAULT_RETURN_URL_PARAM = "return_url";

  static RequiresLoginHandler requiresLoginHandler() {
    return new RequiresLoginHandlerImpl(DEFAULT_LOGIN_REDIRECT_URL, DEFAULT_RETURN_URL_PARAM);
  }

  static RequiresLoginHandler requiresLoginHandler(String loginRedirectURL) {
    return new RequiresLoginHandlerImpl(loginRedirectURL, DEFAULT_RETURN_URL_PARAM);
  }

  static RequiresLoginHandler requiresLoginHandler(String loginRedirectURL, String returnURLParam) {
    return new RequiresLoginHandlerImpl(loginRedirectURL, returnURLParam);
  }

  @Override
  void handle(RoutingContext context);

}
