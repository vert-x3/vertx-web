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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.rest.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface YokeContext extends RoutingContext {


  @CacheReturn
  Buffer bodyBuffer();

  @CacheReturn
  JsonObject bodyJson();

  // Any other Yoke specific stuff



}
