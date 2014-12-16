/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex.addons.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.AbstractTemplateEngine;
import io.vertx.ext.apex.addons.TemplateEngine;
import io.vertx.ext.apex.addons.TemplateHandler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.impl.Utils;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TemplateTest extends ApexTestBase {

  @Test
  public void testTemplateHandler() throws Exception {
    TemplateEngine engine = new TestEngine(false);
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.templateHandler(engine, "test-template.html", "text/html"));
    String expected =
      "<html>\n" +
      "<body>\n" +
      "<h1>Test template</h1>\n" +
      "foo is badger bar is fox<br>\n" +
      "</body>\n" +
      "</html>";
    testRequest(HttpMethod.GET, "/", 200, "OK", expected);
  }

  @Test
  public void testTemplateEngineFail() throws Exception {
    TemplateEngine engine = new TestEngine(true);
    router.route().handler(TemplateHandler.templateHandler(engine, "test-template.html", "text/html"));
    router.exceptionHandler(t -> {
      assertEquals("eek", t.getMessage());
      testComplete();
    });
    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
    await();
  }

  @Test
  public void testRenderDirectly() throws Exception {
    TemplateEngine engine = new TestEngine(false);
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      engine.renderResponse(context, "test-template.html", "text/html");
    });
    String expected =
      "<html>\n" +
        "<body>\n" +
        "<h1>Test template</h1>\n" +
        "foo is badger bar is fox<br>\n" +
        "</body>\n" +
        "</html>";
    testRequest(HttpMethod.GET, "/", 200, "OK", expected);
  }

  @Test
  public void testRenderToBuffer() throws Exception {
    TemplateEngine engine = new TestEngine(false);
    String expected =
      "<html>\n" +
        "<body>\n" +
        "<h1>Test template</h1>\n" +
        "foo is badger bar is fox<br>\n" +
        "</body>\n" +
        "</html>";
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      engine.render(context, "test-template.html", onSuccess(res -> {
        String rendered = res.toString();
        assertEquals(expected, rendered);
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
        context.response().end(rendered);
        testComplete();
      }));
    });

    testRequest(HttpMethod.GET, "/", 200, "OK", expected);
    await();
  }

  // Just for testing - not for actual use
  class TestEngine extends AbstractTemplateEngine {

    boolean fail;

    TestEngine(boolean fail) {
      this.fail = fail;
    }

    @Override
    public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
      if (fail) {
        handler.handle(Future.failedFuture(new Exception("eek")));
      } else {
        String templ = Utils.readResourceToString(templateFileName);
        String rendered = templ.replace("{foo}", context.get("foo"));
        rendered = rendered.replace("{bar}", context.get("bar"));
        handler.handle(Future.succeededFuture(Buffer.buffer(rendered)));
      }
    }

  }
}
