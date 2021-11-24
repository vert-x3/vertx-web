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
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.crud.*;

/**
 * Represents a REST CRUD handler. The handler will take care of the basic protocol validation and ensure that REST best
 * practices are in place when implementing simple CRUD endpoints.
 *
 * This interface favours composition over inheritance. Users who wish to only support a few semantics of REST, are only
 * required to provide the functions needed for the use case.
 *
 * The handler allows the following functions:
 *
 * <ul>
 *   <li>{@link #create(CreateFunction)} - Will handle {@code POST} requests to the base endpoint</li>
 *   <li>{@link #read(ReadFunction)} - Will handle {@code GET} requests to the specific entity endpoint</li>
 *   <li>{@link #update(UpdateFunction)} - Will handle {@code PUT} requests to the specific entity endpoint</li>
 *   <li>{@link #delete(DeleteFunction)} - Will handle {@code DELETE} requests to the specific entity endpoint</li>
 * </ul>
 *
 * There are a few extra functions that can assist with providing extra metadata to the consumer of the REST API
 *
 * <ul>
 *   <li>{@link #query(QueryFunction)} - Will handle {@code GET} requests to the base endpoint performing simple queries</li>
 *   <li>{@link #count(CountFunction)} - Will assist the query function above to include support for pagination</li>
 *   <li>{@link #update(PatchFunction)} - Will handle {@code PATCH} requests to the specific entity endpoint</li>
 * </ul>
 *
 * The handler will perform the following validations:
 *
 * <ul>
 *   <li>404: Client requested an entity that doesn't exist on the user database</li>
 *   <li>405: Client requested a verb that the handler has no function from the user</li>
 *   <li>406: Client sent a list of acceptable content types, that do not allow {@code application/json}</li>
 *   <li>412: Client sent a update request for a non existing entity requesting it to be an overwrite</li>
 *   <li>415: Client sent a body with a content type other than {@code application/json}</li>
 * </ul>
 *
 * Each function has it's own default success response status codes too:
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
public interface JsonCrudHandler {

  /**
   * Creates a new Object in the database and asynchronously returns the id for this new object.
   */
  @Fluent
  JsonCrudHandler create(CreateFunction fn);

  /**
   * Reads a single object from the database given the id.
   */
  @Fluent
  JsonCrudHandler read(ReadFunction fn);

  /**
   * Updates a single object. Returns the total updated elements
   */
  @Fluent
  JsonCrudHandler update(UpdateFunction fn);

  /**
   * Updates a single object. Returns the total updated elements
   */
  @Fluent
  JsonCrudHandler update(PatchFunction fn);

  /**
   * Deletes a single object given an id. Returns the total number of removed elements.
   */
  @Fluent
  JsonCrudHandler delete(DeleteFunction fn);

  /**
   * Queries for a collection of objects given a query, limit and sorting.
   */
  @Fluent
  JsonCrudHandler query(QueryFunction fn);

  /**
   * Counts the elements of a collection given a query.
   */
  @Fluent
  JsonCrudHandler count(CountFunction fn);
}
