package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
public interface Crud {

  /**
   * Creates a new Object in the database and asynchronously returns the id for this new object.
   */
  @Fluent
  Crud create(CrudFunction<JsonObject, String> fn);

  /**
   * Reads a object from the database given the id.
   */
  @Fluent
  Crud read(CrudFunction<String, JsonObject> fn);

  /**
   * Updates a object. Returns the total updated elements
   */
  @Fluent
  Crud update(CrudBiFunction<JsonObject, Long> fn);

  /**
   * Deletes a object given an id. Returns the total number of removed elements.
   */
  @Fluent
  Crud delete(CrudFunction<String, Long> fn);

  /**
   * Queries for a collection of objects given a query, limit and sorting.
   */
  @Fluent
  Crud query(CrudFunction<CrudQuery, JsonArray> fn);

  /**
   * Counts the elements of a collection given a query.
   */
  @Fluent
  Crud count(CrudFunction<CrudQuery, Long> fn);
}
