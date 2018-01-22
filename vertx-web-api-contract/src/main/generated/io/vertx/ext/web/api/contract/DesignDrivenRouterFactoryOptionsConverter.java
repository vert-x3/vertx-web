/*
 * Copyright (c) 2014 Red Hat, Inc. and others
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

package io.vertx.ext.web.api.contract;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.api.contract.DesignDrivenRouterFactoryOptions} original class using Vert.x codegen.
 */
 class DesignDrivenRouterFactoryOptionsConverter {

   static void fromJson(JsonObject json, DesignDrivenRouterFactoryOptions obj) {
    if (json.getValue("mountNotImplementedHandler") instanceof Boolean) {
      obj.setMountNotImplementedHandler((Boolean)json.getValue("mountNotImplementedHandler"));
    }
    if (json.getValue("mountResponseContentTypeHandler") instanceof Boolean) {
      obj.setMountResponseContentTypeHandler((Boolean)json.getValue("mountResponseContentTypeHandler"));
    }
    if (json.getValue("mountValidationFailureHandler") instanceof Boolean) {
      obj.setMountValidationFailureHandler((Boolean)json.getValue("mountValidationFailureHandler"));
    }
    if (json.getValue("requireSecurityHandlers") instanceof Boolean) {
      obj.setRequireSecurityHandlers((Boolean)json.getValue("requireSecurityHandlers"));
    }
  }

   static void toJson(DesignDrivenRouterFactoryOptions obj, JsonObject json) {
    json.put("mountNotImplementedHandler", obj.isMountNotImplementedHandler());
    json.put("mountResponseContentTypeHandler", obj.isMountResponseContentTypeHandler());
    json.put("mountValidationFailureHandler", obj.isMountValidationFailureHandler());
    json.put("requireSecurityHandlers", obj.isRequireSecurityHandlers());
  }
}