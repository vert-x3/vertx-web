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

package io.vertx.apex.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.apex.Router;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HelloWorldVerticle extends AbstractVerticle {

  public void start() {
    Router router = Router.router(vertx);
    router.route().handler(rc -> rc.response().putHeader("content-type", "text/plain").end("Hello World!"));

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
