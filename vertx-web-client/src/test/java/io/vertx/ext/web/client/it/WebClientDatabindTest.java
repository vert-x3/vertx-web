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

package io.vertx.ext.web.client.it;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.tests.WebClientJUnit5TestBase;
import io.vertx.ext.web.client.tests.jackson.WineAndCheese;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientDatabindTest extends WebClientJUnit5TestBase {

  @Test
  public void testResponseBodyAsJsonMapped() {
    JsonObject expected = new JsonObject().put("cheese", "Goat Cheese").put("wine", "Condrieu");
    server.requestHandler(req -> req.response().end(expected.encode()));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    HttpResponse<WineAndCheese> resp = get
      .as(BodyCodec.json(WineAndCheese.class))
      .send().await();
    assertEquals(200, resp.statusCode());
    assertEquals(new WineAndCheese().setCheese("Goat Cheese").setWine("Condrieu"), resp.body());
  }

  @Test
  public void testResponseUnknownContentTypeBodyAsJsonMapped() {
    JsonObject expected = new JsonObject().put("cheese", "Goat Cheese").put("wine", "Condrieu");
    server.requestHandler(req -> req.response().end(expected.encode()));
    startServer();
    HttpRequest<Buffer> get = webClient.get(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    HttpResponse<Buffer> resp = get.send().await();
    assertEquals(200, resp.statusCode());
    assertEquals(new WineAndCheese().setCheese("Goat Cheese").setWine("Condrieu"), resp.bodyAsJson(WineAndCheese.class));
  }

  @Test
  public void testSendJsonPojoBody() {
    server.requestHandler(req -> req.bodyHandler(buff -> {
      assertEquals("application/json", req.getHeader("content-type"));
      assertEquals(new JsonObject().put("wine", "Chateauneuf Du Pape").put("cheese", "roquefort"), buff.toJsonObject());
      req.response().end();
    }));
    startServer();
    HttpRequest<Buffer> post = webClient.post(DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, "/somepath");
    post.sendJson(new WineAndCheese().setCheese("roquefort").setWine("Chateauneuf Du Pape")).await();
  }
}
