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

  final Map<String, User> users;
  final List<Link> links;

  TestData() {
    User peter = new User(UUID.randomUUID().toString(), "Peter");
    User paul = new User(UUID.randomUUID().toString(), "Paul");
    User jack = new User(UUID.randomUUID().toString(), "Jack");

    Map<String, User> map = new HashMap<>();
    map.put(peter.getId(), peter);
    map.put(paul.getId(), paul);
    map.put(jack.getId(), jack);
    users = Collections.unmodifiableMap(map);

    List<Link> list = new ArrayList<>();
    list.add(new Link("https://vertx.io", "Vert.x project", peter.getId()));
    list.add(new Link("https://www.eclipse.org", "Eclipse Foundation", paul.getId()));
    list.add(new Link("http://reactivex.io", "ReactiveX libraries", jack.getId()));
    list.add(new Link("https://www.graphql-java.com", "GraphQL Java implementation", peter.getId()));
    links = Collections.unmodifiableList(list);
  }

  List<String> urls() {
    return links.stream().map(Link::getUrl).collect(toList());
  }

  boolean checkLinkUrls(List<String> expected, JsonObject body) {
    if (body.containsKey("errors")) {
      return false;
    }
    JsonObject data = body.getJsonObject("data");
    List<String> urls = data.getJsonArray("allLinks").stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getString("url"))
      .collect(toList());
    return expected.equals(urls);
  }

  List<String> posters() {
    return links.stream().map(link -> users.get(link.getUserId())).map(User::getName).collect(toList());
  }

  boolean checkLinkPosters(List<String> expected, JsonObject body) {
    if (body.containsKey("errors")) {
      return false;
    }
    JsonObject data = body.getJsonObject("data");
    List<String> names = data.getJsonArray("allLinks").stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getJsonObject("postedBy").getString("name"))
      .collect(toList());
    return expected.equals(names);
  }
}
