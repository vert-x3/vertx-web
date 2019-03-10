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

package io.vertx.ext.web.handler.graphql.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author Thomas Segismont
 */
public class GraphQLInputDeserializer extends JsonDeserializer<GraphQLInput> {

  @Override
  public GraphQLInput deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    if (p.isExpectedStartArrayToken()) {
      return p.readValueAs(GraphQLBatch.class);
    }
    return p.readValueAs(GraphQLQuery.class);
  }
}
