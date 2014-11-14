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

package io.vertx.ext.yoke3;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.rest.Router;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface Yoke extends Router {

  static Yoke yoke() {
    return null;
  }

  @Override
  YokeRoute route();

  // Convenience methods where the method/path is already specified
  @Override
  YokeRoute route(HttpMethod method, String path);

  // Convenience methods where the method/regex is already specified
  @Override
  YokeRoute routeWithRegex(HttpMethod method, String regex);
}
