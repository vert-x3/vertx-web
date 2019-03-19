/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import io.vertx.core.json.JsonObject;

/**
 * Extends {@link PropertyDataFetcher} so that properties can be read from a {@link JsonObject}.
 *
 * @author Thomas Segismont
 */
public class VertxPropertyDataFetcher extends PropertyDataFetcher {

  public VertxPropertyDataFetcher(String propertyName) {
    super(propertyName);
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    Object source = environment.getSource();
    if (source instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) source;
      return jsonObject.getValue(getPropertyName());
    }
    return super.get(environment);
  }
}
