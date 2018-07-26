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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;

import com.github.jknack.handlebars.ValueResolver;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class HandlebarsTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileResolverCachingEnabled(true));
  }

  @Test
  public void testTemplateOnClasspath(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2.hbs", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Hello badger and fox", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateJsonObjectResolver(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonObject json = new JsonObject();
    json.put("bar", new JsonObject().put("one", "badger").put("two", "fox"));

    engine.render(new JsonObject().put("foo", json), "src/test/filesystemtemplates/test-handlebars-template4.hbs", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Goodbye badger and fox", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateJsonArrayResolver(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));
    String expected = "Iterator: badger,fox,{&quot;name&quot;:&quot;joe&quot;}, Element by index:fox - joe - Out of bounds:  - Size:3";

    engine.render(new JsonObject().put("foo", jsonArray), "src/test/filesystemtemplates/test-handlebars-template5.hbs", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testCustomResolver(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    engine.setResolvers(new ValueResolver() {
      @Override
      public Object resolve(Object context, String name) {
        return "custom";
      }

      @Override
      public Object resolve(Object context) {
        return "custom";
      }

      @Override
      public Set<Entry<String, Object>> propertySet(Object context) {
        return Collections.emptySet();
      }
    });

    engine.render(new JsonObject().put("foo", "Badger").put("bar", "Fox"), "src/test/filesystemtemplates/test-handlebars-template3.hbs", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Goodbye custom and custom", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateJsonArrayResolverError(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));

    final JsonObject context = new JsonObject().put("foo", jsonArray);

    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template6.hbs", render -> {
      should.assertFalse(render.succeeded());
      should.assertTrue(render.cause().getMessage().contains("test-handlebars-template6.hbs:1:19"));
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateOnFileSystem(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");


    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template3.hbs", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Goodbye badger and fox", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateOnClasspathDisableCaching(TestContext should) {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateOnClasspath(should);
  }

  @Test
  public void testTemplateWithPartial(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template7", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("\ntext from template8\n\ntext from template7\n\n\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateWithPartialFromSubdir(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/sub/test-handlebars-template9", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("\ntext from template8\n\ntext from template9\n\n\n", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateNoExtension(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Hello badger and fox", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateChangeExtension(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx).setExtension("zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2", render -> {
      should.assertTrue(render.succeeded());
      should.assertEquals("Cheerio badger and fox", render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    final Async test = should.async();
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx).setExtension("zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/foo.hbs", render -> {
      should.assertFalse(render.succeeded());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testGetHandlebars() throws Exception {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);
    assertNotNull(engine.getHandlebars());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    final Async test = should.async();

    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".hbs", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), render -> {
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

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), render2 -> {
        should.assertTrue(render2.succeeded());
        should.assertEquals("before", render2.result().toString());
        test.complete();
      });
    });
    test.await();
  }
}
