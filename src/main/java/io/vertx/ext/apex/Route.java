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

package io.vertx.ext.apex;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Route {

  Route method(HttpMethod method);

  Route path(String path);

  Route pathRegex(String path);

  Route produces(String contentType);

  Route consumes(String contentType);

  Route order(int order);

  Route last(boolean last);

  Route handler(Handler<RoutingContext> requestHandler);

  Route failureHandler(Handler<RoutingContext> failureHandler);

  Route remove();

  Route disable();

  Route enable();

  String getPath();

}


