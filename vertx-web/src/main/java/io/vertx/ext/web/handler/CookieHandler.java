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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.CookieHandlerImpl;

/**
 * A handler which decodes cookies from the request, makes them available in the {@link RoutingContext}
 * and writes them back in the response.
 *
 * Since 3.8.1 this handler simply calls the next request handler. This handler will be removed in Vert.x 4.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @deprecated cookies are enabled by default and this handler is not required anymore
 */
@VertxGen
@Deprecated
public interface CookieHandler extends Handler<RoutingContext> {

  /**
   * Create a cookie handler
   *
   * @return the cookie handler
   */
  static CookieHandler create() {
    return new CookieHandlerImpl();
  }

}
