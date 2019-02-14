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

import io.vertx.core.json.JsonObject;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class TestData {

  final Map<String, String> links;

  TestData() {
    Map<String, String> map = new HashMap<>();
    map.put("https://vertx.io", "Vert.x project");
    map.put("https://www.eclipse.org", "Eclipse Foundation");
    map.put("http://reactivex.io", "ReactiveX libraries");
    map.put("https://www.graphql-java.com", "GraphQL Java implementation");
    links = Collections.unmodifiableMap(map);
  }

  boolean checkLinkUrls(Set<String> expected, JsonObject body) {
    if (body.containsKey("errors")) {
      return false;
    }
    JsonObject data = body.getJsonObject("data");
    List<String> urls = data.getJsonArray("allLinks").stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getString("url"))
      .collect(toList());
    return urls.containsAll(expected) && expected.containsAll(urls);
  }
}
