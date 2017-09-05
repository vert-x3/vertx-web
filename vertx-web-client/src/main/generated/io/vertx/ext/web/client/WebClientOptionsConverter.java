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

package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.ext.web.client.WebClientOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.WebClientOptions} original class using Vert.x codegen.
 */
public class WebClientOptionsConverter {

  public static void fromJson(JsonObject json, WebClientOptions obj) {
    if (json.getValue("followRedirects") instanceof Boolean) {
      obj.setFollowRedirects((Boolean)json.getValue("followRedirects"));
    }
    if (json.getValue("userAgent") instanceof String) {
      obj.setUserAgent((String)json.getValue("userAgent"));
    }
    if (json.getValue("userAgentEnabled") instanceof Boolean) {
      obj.setUserAgentEnabled((Boolean)json.getValue("userAgentEnabled"));
    }
  }

  public static void toJson(WebClientOptions obj, JsonObject json) {
    json.put("followRedirects", obj.isFollowRedirects());
    if (obj.getUserAgent() != null) {
      json.put("userAgent", obj.getUserAgent());
    }
    json.put("userAgentEnabled", obj.isUserAgentEnabled());
  }
}