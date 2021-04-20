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
package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.openapi.impl.SecurityHandlerProviderImpl;

import java.util.function.Function;

/**
 * An authentication handler provider. This class will hold factories for creating {@link AuthenticationHandler}
 * objects.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface SecurityHandlerProvider {

  /**
   * Create a simple authentication handler provider.
   * The role of this object is to hold factory functions for internal querying during the route builder phase.
   * @return a new instance of this class.
   */
  static SecurityHandlerProvider create() {
    return new SecurityHandlerProviderImpl();
  }

  /**
   * Adds a new factory to the provider.
   *
   * @param securitySchemaId the factory security schema id
   * @param factory the factory function
   * @return fluent self.
   */
  @Fluent
  SecurityHandlerProvider add(String securitySchemaId, Function<JsonObject, Future<AuthenticationHandler>> factory);

  boolean containsSecuritySchemaId(String securitySchemaId);

  /**
   * Build a {@link AuthenticationHandler} given the {@code securitySchemaId}.
   *
   * @param securitySchemaId the security schema id used in the openapi document under {@code /components/securitySchemes}.
   * @param config the openAPI {@code securitySchemes} object.
   * @return a future operation result of the build.
   */
  Future<AuthenticationHandler> build(String securitySchemaId, JsonObject config);

  /**
   * Same as {@link #build(String, JsonObject)}
   */
  @Fluent
  default SecurityHandlerProvider build(String securitySchemaId, JsonObject config, Handler<AsyncResult<AuthenticationHandler>> handler) {
    build(securitySchemaId, config).onComplete(handler);
    return this;
  }
}
