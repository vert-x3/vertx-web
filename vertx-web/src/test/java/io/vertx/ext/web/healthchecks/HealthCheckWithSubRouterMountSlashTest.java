/*
 *  Copyright (c) 2011-2021 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.healthchecks;

import io.vertx.ext.web.Router;

/**
 * Same as {@link HealthCheckTest} but using a sub router mount point as: '/'.
 */
public class HealthCheckWithSubRouterMountSlashTest extends HealthCheckTest {

  @Override
  protected void setupRouter(Router router, HealthCheckHandler healthCheckHandler) {
    Router sub = Router.router(vertx);
    sub.get("/ping*").handler(healthCheckHandler);
    router.route("/*").subRouter(sub);
  }

  @Override
  protected String prefix() {
    return "/ping";
  }
}
