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

package io.vertx.serviceproxy.testmodel;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
@VertxGen
public interface TestConnection {

  @Fluent
  TestConnection startTransaction(Handler<AsyncResult<String>> resultHandler);

  @Fluent
  TestConnection insert(String name, JsonObject data, Handler<AsyncResult<String>> resultHandler);

  @Fluent
  TestConnection commit(Handler<AsyncResult<String>> resultHandler);

  @Fluent
  TestConnection rollback(Handler<AsyncResult<String>> resultHandler);

  @ProxyClose
  void close();

}
