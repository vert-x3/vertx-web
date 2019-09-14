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

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.UserSessionHandler;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@Deprecated
public class UserSessionHandlerImpl implements UserSessionHandler {

  private static final Logger log = LoggerFactory.getLogger(UserSessionHandlerImpl.class);

  public UserSessionHandlerImpl(AuthProvider authProvider) {
    log.warn("This handler is not needed anymore, the SessionHandler takes care of the user session.");
  }

  @Override
  public void handle(RoutingContext routingContext) {
    routingContext.next();
  }

}
