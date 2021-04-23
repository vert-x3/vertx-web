/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.client.impl.cache;

import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public interface CacheControlDirectives {

  String PUBLIC = "public";
  String PRIVATE = "private";
  String NO_STORE = "no-store";
  String NO_CACHE = "no-cache";
  String SHARED_MAX_AGE = "s-maxage";
  String MAX_AGE = "max-age";
  String STALE_IF_ERROR = "stale-if-error";
  String STALE_WHILE_REVALIDATE = "stale-while-revalidate";
}
