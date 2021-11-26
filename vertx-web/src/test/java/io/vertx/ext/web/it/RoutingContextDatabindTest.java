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

package io.vertx.ext.web.it;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CrudHandler;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.*;


public class RoutingContextDatabindTest extends WebTestBase {

  @AfterClass
  public static void oneTimeTearDown() {
    Vertx vertx = Vertx.vertx();
    if (vertx.fileSystem().existsBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY)) {
      vertx.fileSystem().deleteRecursiveBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, true);
    }
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(BodyHandler.create());
  }

  static class Point {
    public int x, y;

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public void setY(int y) {
      this.y = y;
    }
  }

  @Test
  public void testJsonBean() throws Exception {
    router.route().handler(ctx -> {
      Point p = new Point();
      p.setX(10);
      p.setY(20);
      ctx.json(p);
    }).failureHandler(ctx -> {ctx.failure().printStackTrace(); });

    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), "{\"x\":10,\"y\":20}");
  }

  static class Person {

    private String name;
    private String id;

    public Person() {}

    public String getName() {
      return name;
    }

    public Person setName(String name) {
      this.name = name;
      return this;
    }

    public String getId() {
      return id;
    }

    public Person setId(String id) {
      this.id = id;
      return this;
    }
  }

  @Test
  public void testCrudPOJO() throws Exception {

    final Map<String, Person> store = new HashMap<>();

    router
      .route().handler(BodyHandler.create());

    router
      .route("/persons/*")
      .handler(
        CrudHandler.create(Person.class)
          .createHandler(person -> {
            String id = UUID.randomUUID().toString();
            store.put(id, person.setId(id));
            return Future.succeededFuture(id);
          })
          .queryHandler(query -> {
            List<Person> persons = new ArrayList<>();
            Collection<Person> collection = store.values();
            int start = query.getStart() == null ? 0 : query.getStart();
            int end = query.getEnd() == null ? collection.size() : query.getEnd();

            int i = 0;
            for (Person o : collection) {
              if (i >= start && i < end) {
                persons.add(o);
              }
              i++;
            }

            return Future.succeededFuture(persons);
          })
          .updateHandler((id, newPerson) -> {
            Person o = store.put(id, newPerson);
            return Future.succeededFuture(o == null ? 0 : 1);
          })
          .countHandler(query -> Future.succeededFuture(store.size())));

    testRequest(
      HttpMethod.POST,
      "/persons",
      req ->
        req
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("name", "Paulo").encode()),
      res -> {
        assertNotNull(res.getHeader("Location"));
      }, 201, "Created", "");

    testRequest(
      HttpMethod.POST,
      "/persons",
      req ->
        req
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("name", "Thomas").encode()),
      res -> {
        assertNotNull(res.getHeader("Location"));
      }, 201, "Created", "");

    testRequest(
      HttpMethod.POST,
      "/persons",
      req ->
        req
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("name", "Julien").encode()),
      res -> {
        assertNotNull(res.getHeader("Location"));
      }, 201, "Created", "");

    testRequest(HttpMethod.GET, "/persons", req -> {
      req.putHeader("Range", "items=0-2");
    }, res -> {
      assertNotNull(res.getHeader("Content-Range"));
      res.body()
        .onFailure(this::fail);
    }, 200, "OK", null);
  }
}
