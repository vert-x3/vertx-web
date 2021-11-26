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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.crud.*;
import io.vertx.ext.web.handler.crud.impl.JsonCrudHandlerImpl;

/**
 * Represents a JSON REST CRUD handler. The handler will take care of the basic protocol validation and ensure that REST
 * best practices are in place when implementing simple CRUD endpoints.
 *
 * This interface favours composition over inheritance. Users who wish to only support a few semantics of REST, are only
 * required to provide the functions needed for the use case.
 *
 * The handler allows the following functions:
 *
 * <ul>
 *   <li>{@link #createHandler(CreateHandler)} - Will handle {@code POST} requests to the base endpoint</li>
 *   <li>{@link #readHandler(ReadHandler)} - Will handle {@code GET} requests to the specific entity endpoint</li>
 *   <li>{@link #updateHandler(UpdateHandler)} - Will handle {@code PUT} requests to the specific entity endpoint</li>
 *   <li>{@link #deleteHandler(DeleteHandler)} - Will handle {@code DELETE} requests to the specific entity endpoint</li>
 * </ul>
 *
 * There are a few extra functions that can assist with providing extra metadata to the consumer of the REST API
 *
 * <ul>
 *   <li>{@link #queryHandler(QueryHandler)} - Will handle {@code GET} requests to the base endpoint performing simple queries</li>
 *   <li>{@link #countHandler(CountHandler)} - Will assist the query function above to include support for pagination</li>
 *   <li>{@link #updateHandler(PatchHandler)} - Will handle {@code PATCH} requests to the specific entity endpoint</li>
 * </ul>
 *
 * The handler will perform the following validations:
 *
 * <ul>
 *   <li>400: Client sent an invalid Json Object</li>
 *   <li>404: Client requested an entity that doesn't exist on the user database</li>
 *   <li>405: Client requested a verb that the handler has no function from the user</li>
 *   <li>406: Client sent a list of acceptable content types, that do not allow {@code application/json}</li>
 *   <li>412: Client sent a update request for a non existing entity requesting it to be an overwrite</li>
 *   <li>415: Client sent a body with a content type other than {@code application/json}</li>
 * </ul>
 *
 * Each function has its own default success response status codes too:
 *
 * <ul>
 *   <li>{@code GET}: Defaults to: {@code 200} for success</li>
 *   <li>{@code POST}: Defaults to: {@code 201} for success, {@code Location} header with the newly created id and no body</li>
 *   <li>{@code PUT/PATCH/DELETE}: Defaults to: {@code 204} for success and no body.</li>
 * </ul>
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
public interface CrudHandler<T> extends Handler<RoutingContext> {


  /**
   * Create a Json CRUD handler. This will be default use {@link JsonObject} as the type of data to be passed to the
   * underlying handlers.
   *
   * For type safe APIs {@link #create(Class)}.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static CrudHandler<JsonObject> create() {
    return new JsonCrudHandlerImpl<>();
  }

  /**
   * Create a Json CRUD handler with a well-defined POJO as entity type. In this mode all underlying handlers will
   * receive the body of the request already casted to the declared type. This is useful for typesafe APIs.
   *
   * <b>NOTE:</b> using this mode, requires that `jackson-databind` is present in your application class-path.
   */
  static <P> CrudHandler<P> create(Class<P> clazz) {
    return new JsonCrudHandlerImpl<>(clazz);
  }

  /**
   * Enforces a max allowed json input limit, like {@link io.vertx.ext.web.RoutingContext#getBodyAsJson(int)}
   */
  @Fluent
  CrudHandler<T> maxAllowedLength(int length);

  /**
   * Creates a new Object in the database and asynchronously returns the id for this new object.
   */
  @Fluent
  CrudHandler<T> createHandler(CreateHandler<T> fn);

  /**
   * Reads a single object from the database given the id.
   */
  @Fluent
  CrudHandler<T> readHandler(ReadHandler<T> fn);

  /**
   * Updates a single object. Returns the total updated elements
   */
  @Fluent
  CrudHandler<T> updateHandler(UpdateHandler<T> fn);

  /**
   * Updates a single object. Returns the total updated elements
   */
  @Fluent
  CrudHandler<T> updateHandler(PatchHandler<T> fn);

  /**
   * Deletes a single object given an id. Returns the total number of removed elements.
   */
  @Fluent
  CrudHandler<T> deleteHandler(DeleteHandler fn);

  /**
   * Queries for a collection of objects given a query, limit and sorting.
   */
  @Fluent
  CrudHandler<T> queryHandler(QueryHandler<T> fn);

  /**
   * Counts the elements of a collection given a query.
   */
  @Fluent
  CrudHandler<T> countHandler(CountHandler fn);
}
