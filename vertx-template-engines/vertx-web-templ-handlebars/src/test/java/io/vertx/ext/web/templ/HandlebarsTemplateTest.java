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
import io.vertx.core.file.FileSystemOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class HandlebarsTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateOnClasspath(TestContext should) {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2.hbs", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateJsonObjectResolver(TestContext should) {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonObject json = new JsonObject();
    json.put("bar", new JsonObject().put("one", "badger").put("two", "fox"));

    engine.render(new JsonObject().put("foo", json), "src/test/filesystemtemplates/test-handlebars-template4.hbs", should.asyncAssertSuccess(render -> {
      should.assertEquals("Goodbye badger and fox", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateJsonArrayResolver(TestContext should) {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));
    String expected = "Iterator: badger,fox,{&quot;name&quot;:&quot;joe&quot;}, Element by index:fox - joe - Out of bounds:  - Size:3";

    engine.render(new JsonObject().put("foo", jsonArray), "src/test/filesystemtemplates/test-handlebars-template5.hbs", should.asyncAssertSuccess(render -> {
      should.assertEquals(expected, normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testCustomResolver(TestContext should) {
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

    engine.render(new JsonObject().put("foo", "Badger").put("bar", "Fox"), "src/test/filesystemtemplates/test-handlebars-template3.hbs", should.asyncAssertSuccess(render -> {
      should.assertEquals("Goodbye custom and custom", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateJsonArrayResolverError(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));

    final JsonObject context = new JsonObject().put("foo", jsonArray);

    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template6.hbs", should.asyncAssertFailure(cause -> {
      should.assertTrue(cause.getMessage().contains("test-handlebars-template6.hbs:1:19"));
    }));
  }

  @Test
  public void testTemplateOnFileSystem(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");


    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template3.hbs", should.asyncAssertSuccess(render -> {
      should.assertEquals("Goodbye badger and fox", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateOnClasspath(should);
  }

  @Test
  public void testTemplateWithPartial(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/test-handlebars-template7", should.asyncAssertSuccess(render -> {
      should.assertEquals("\ntext from template8\n\ntext from template7\n\n\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateWithPartialFromSubdir(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "src/test/filesystemtemplates/sub/test-handlebars-template9", should.asyncAssertSuccess(render -> {
      should.assertEquals("\ntext from template8\n\ntext from template9\n\n\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateNoExtension(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateChangeExtension(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx, "zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/test-handlebars-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("Cheerio badger and fox", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx, "zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    engine.render(context, "somedir/foo.hbs", should.asyncAssertFailure());
  }

  @Test
  public void testGetHandlebars() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);
    assertNotNull(engine.getHandlebars());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".hbs", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("before", normalizeCRLF(render.toString()));
      // cache is enabled so if we change the content that should not affect the result

      try (PrintWriter out2 = new PrintWriter(temp)) {
        out2.print("after");
        out2.flush();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("before", normalizeCRLF(render2.toString()));
      }));
    }));
  }

  @Test
  public void testTemplatePerf(TestContext should) {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    final AtomicInteger cnt = new AtomicInteger(0);
    final long t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      engine.render(context, "somedir/test-handlebars-template2.hbs", should.asyncAssertSuccess(render -> {
        should.assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
        if (cnt.incrementAndGet() == 1000000) {
          final long t1 = System.currentTimeMillis();
          System.out.println(t1 - t0);
        }
      }));
    }
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
