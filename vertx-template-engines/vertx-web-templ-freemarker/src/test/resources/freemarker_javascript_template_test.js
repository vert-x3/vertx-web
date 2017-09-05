/*
 * Copyright 2016 Red Hat, Inc.
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

var Assert = org.junit.Assert;

var CountDownLatch = java.util.concurrent.CountDownLatch;
var TimeUnit = java.util.concurrent.TimeUnit;

var Vertx = require("vertx-js/vertx");

var console = require("vertx-js/util/console");

var Router = require("vertx-web-js/router");
var FreeMarkerTemplateEngine = require("vertx-web-js/free_marker_template_engine");
var TemplateHandler = require("vertx-web-js/template_handler");

function testTemplate() {
  var vertx = Vertx.vertx();
  var server = vertx.createHttpServer();
  var router = Router.router(vertx);

  var engine = FreeMarkerTemplateEngine.create();
  var handler = TemplateHandler.create(engine, "somedir", "text/plain");

  router.route().handler(function (context) {
    context.put("foo", "badger");
    context.put("bar", ["pipo", "molo"]);
    context.put("baz", {"top": "pipo", "bottom": "molo"});
    context.put("team", {
      "grenoble": ["Clément"],
      "lyon": ["Julien"],
      "amsterdam": ["Paulo"],
      "marseille": ["Julien", "Thomas"]
    });
    context.next();
  });
  router.get().handler(handler.handle);

  var listenLatch = new CountDownLatch(1);
  server.requestHandler(router.accept).listen(8080, function (res, res_err) {
    if (res_err != null) {
      Assert.fail(res_err)
    }
    listenLatch.countDown();
  });
  Assert.assertTrue(listenLatch.await(5, TimeUnit.SECONDS));

  var responseLatch = new CountDownLatch(1);
  var client = vertx.createHttpClient();
  client.getNow(8080, "localhost", "/altlang", function (response) {
    Assert.assertTrue(200 == response.statusCode());
    response.bodyHandler(function (buffer) {
      var lines = [];
      lines.push("Hello badger");
      lines.push("There is a pipo");
      lines.push("There is a molo");
      lines.push("top");
      lines.push("bottom");
      lines.push("pipo");
      lines.push("molo");
      lines.push("Julien loves Olympique de Marseille");
      lines.push("Thomas loves Olympique de Marseille");
      lines.push("");
      var actual = buffer.toString().replace(/\r\n/g , "\n");
      Assert.assertEquals(lines.join("\n"), actual);
      responseLatch.countDown();
    });
  });
  Assert.assertTrue(responseLatch.await(5, TimeUnit.SECONDS));

  var closeLatch = new CountDownLatch(2);
  client.close();
  server.close(function (res, res_err) {
    if (res_err != null) {
      Assert.fail(res_err)
    }
    closeLatch.countDown();
  });
  vertx.close(function (res, res_err) {
    if (res_err != null) {
      Assert.fail(res_err)
    }
    closeLatch.countDown();
  });
  Assert.assertTrue(closeLatch.await(5, TimeUnit.SECONDS));
}

if (typeof this[testName] === "undefined") {
  throw "No such test: " + testName;
}

this[testName]();
