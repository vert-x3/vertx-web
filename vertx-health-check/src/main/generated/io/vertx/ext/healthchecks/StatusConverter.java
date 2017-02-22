/*
 * Copyright 2014 Red Hat, Inc.
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

package io.vertx.ext.healthchecks;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.ext.healthchecks.Status}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.healthchecks.Status} original class using Vert.x codegen.
 */
public class StatusConverter {

  public static void fromJson(JsonObject json, Status obj) {
    if (json.getValue("data") instanceof JsonObject) {
      obj.setData(((JsonObject)json.getValue("data")).copy());
    }
    if (json.getValue("ok") instanceof Boolean) {
      obj.setOk((Boolean)json.getValue("ok"));
    }
    if (json.getValue("procedureInError") instanceof Boolean) {
      obj.setProcedureInError((Boolean)json.getValue("procedureInError"));
    }
  }

  public static void toJson(Status obj, JsonObject json) {
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    json.put("ok", obj.isOk());
    json.put("procedureInError", obj.isProcedureInError());
  }
}