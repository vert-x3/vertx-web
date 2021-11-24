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
package io.vertx.ext.web.handler.crud;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Data object holding all configuration related to a web query.
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
@DataObject(generateConverter = true)
public class CrudQuery {

  private JsonObject query;
  private Integer start;
  private Integer end;
  private JsonObject sort;

  public CrudQuery() {}

  public CrudQuery(JsonObject json) {
    CrudQueryConverter.fromJson(json, this);
  }

  public CrudQuery(CrudQuery other) {
    this.query = other.query;
    this.start = other.start;
    this.end = other.end;
    this.sort = other.sort;
  }

  /**
   * Getter for the query field. The query object defines which properties should be filtered, the value is
   * implementation dependent.
   *
   * @return JsonObject query
   */
  public JsonObject getQuery() {
    return query;
  }

  /**
   * Setter for the query object.
   *
   * @param query JsonObject holding a query. The format is implementation specific.
   * @return fluent self.
   */
  public CrudQuery setQuery(JsonObject query) {
    this.query = query;
    return this;
  }

  /**
   * Return the start of the paging results.
   * @return the desired start of the paging result window.
   */
  public Integer getStart() {
    return start;
  }

  /**
   * Set the start of the paging results.
   * @param start the desired start entry
   * @return fluent self.
   */
  public CrudQuery setStart(Integer start) {
    this.start = start;
    return this;
  }

  /**
   * Return the end of the paging results.
   * @return the desired end of the paging result window.
   */
  public Integer getEnd() {
    return end;
  }

  /**
   * Set the end of the paging results.
   * @param end the desired end entry
   * @return fluent self.
   */
  public CrudQuery setEnd(Integer end) {
    this.end = end;
    return this;
  }

  /**
   * Getter for the sort field. The sort object defines which properties should be sorted, a property with value
   * {@code 1} is expected to be sorted in {@code ascending order}, a property with value {@code -1} is expected
   * to be sorted in {@code descending order}.
   *
   * @return JsonObject query
   */
  public JsonObject getSort() {
    return sort;
  }

  /**
   * Setter for the sort object.
   *
   * @param sort JsonObject holding a query. The format is a property holding either {@code 1} or {@code -1} values.
   * @return fluent self.
   */
  public CrudQuery setSort(JsonObject sort) {
    this.sort = sort;
    return this;
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    CrudQueryConverter.toJson(this, json);
    return json;
  }
}
