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

package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@RunWith(VertxUnitRunner.class)
public class FreeMarkerTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileResolverCachingEnabled(true));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2.ftl", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template2.ftl\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    final Async test = should.async();

    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".ftl", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    engine.render(new JsonObject(), temp.getName(), render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("before", render.result().toString());
      // cache is enabled so if we change the content that should not affect the result

      try {
        PrintWriter out2 = new PrintWriter(temp);
        out2.print("after");
        out2.flush();
        out2.close();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getName(), render2 -> {
        should.assertTrue(render2.succeeded());
        should.assertEquals("before", render2.result().toString());
        test.complete();
      });
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template3.ftl"));

    engine.render(context, "src/test/filesystemtemplates/test-freemarker-template3.ftl", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template3.ftl\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext context) {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateHandlerOnClasspath(context);
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) throws Exception {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template2.ftl\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx).setExtension("mvl");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Cheerio badger and fox\nRequest path is /test-freemarker-template2.ftl\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerIncludes(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/base", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Vert.x rules", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    engine.render(new JsonObject(), "not-found", render -> {
      should.assertTrue(render.failed());
      test.complete();
    });
    test.await();
  }

}
