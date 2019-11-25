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
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.AfterClass;
import org.junit.Test;


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
    });

    testRequest(HttpMethod.GET, "/", null, res -> {
      assertEquals("application/json; charset=utf-8", res.getHeader("Content-Type"));
    }, HttpResponseStatus.OK.code(), HttpResponseStatus.OK.reasonPhrase(), "{\"x\":10,\"y\":20}");
  }
}
