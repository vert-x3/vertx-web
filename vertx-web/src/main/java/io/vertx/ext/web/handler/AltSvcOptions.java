/*
 * Copyright 2026 Red Hat, Inc.
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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Options for configuring {@link AltSvcHandler}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class AltSvcOptions {

  private Map<String, String> origins;

  /**
   * Default constructor.
   */
  public AltSvcOptions() {
    origins = new HashMap<>();
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public AltSvcOptions(AltSvcOptions other) {
    origins = new HashMap<>(other.origins);
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public AltSvcOptions(JsonObject json) {
    this();
    AltSvcOptionsConverter.fromJson(json, this);
  }

  /**
   * @return a JSON representation of these options
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    AltSvcOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * @return configured origins and their alternative service definitions
   */
  public Map<String, String> getOrigins() {
    return origins;
  }

  /**
   * Set configured origins and their alternative service definitions.
   *
   * @param origins configured origins
   * @return a reference to this, so the API can be used fluently
   */
  public AltSvcOptions setOrigins(Map<String, String> origins) {
    this.origins = origins == null ? new HashMap<>() : new HashMap<>(origins);
    return this;
  }

  /**
   * Add an origin and its alternative service definition.
   *
   * @param origin the origin
   * @param alternativeService the alternative service definition
   * @return a reference to this, so the API can be used fluently
   */
  public AltSvcOptions addOrigin(String origin, String alternativeService) {
    if (origin == null) {
      throw new IllegalArgumentException("origin cannot be null");
    }
    if (alternativeService == null) {
      throw new IllegalArgumentException("alternativeService cannot be null");
    }
    origins.put(origin, alternativeService);
    return this;
  }
}
