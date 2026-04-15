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

package io.vertx.ext.web.templ.handlebars.tests;

import com.github.jknack.handlebars.ValueResolver;
import io.netty.util.internal.PlatformDependent;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateTest {

  private static Vertx vertx;

  @BeforeAll
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateOnClasspath() {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-handlebars-template2.hbs").await();
    assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateJsonObjectResolver() {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonObject json = new JsonObject();
    json.put("bar", new JsonObject().put("one", "badger").put("two", "fox"));

    Buffer render = engine.render(new JsonObject().put("foo", json), "src/test/filesystemtemplates/test-handlebars-template4.hbs").await();
    assertEquals("Goodbye badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateJsonArrayResolver() {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));
    String expected = "Iterator: badger,fox,{&quot;name&quot;:&quot;joe&quot;}, Element by index:fox - joe - Out of bounds:  - Size:3";

    Buffer render = engine.render(new JsonObject().put("foo", jsonArray), "src/test/filesystemtemplates/test-handlebars-template5.hbs").await();
    assertEquals(expected, normalizeCRLF(render.toString()));
  }

  @Test
  public void testCustomResolver() {
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

    Buffer render = engine.render(new JsonObject().put("foo", "Badger").put("bar", "Fox"), "src/test/filesystemtemplates/test-handlebars-template3.hbs").await();
    assertEquals("Goodbye custom and custom", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateJsonArrayResolverError() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));

    final JsonObject context = new JsonObject().put("foo", jsonArray);

    Exception e = assertThrows(Exception.class, () -> engine.render(context, "src/test/filesystemtemplates/test-handlebars-template6.hbs").await());
    assertTrue(e.getMessage().contains("test-handlebars-template6.hbs:1:19"));
  }

  @Test
  public void testTemplateOnFileSystem() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");


    Buffer render = engine.render(context, "src/test/filesystemtemplates/test-handlebars-template3.hbs").await();
    assertEquals("Goodbye badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateOnWindowsFileSystem() {
    assumeTrue(PlatformDependent.isWindows());

    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");


    Buffer render = engine.render(context, "src/test/filesystemtemplates\\test-handlebars-template3.hbs").await();
    assertEquals("Goodbye badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateOnClasspathDisableCaching() {
    System.setProperty("vertxweb.environment", "development");
    testTemplateOnClasspath();
  }

  @Test
  public void testTemplateWithPartial() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "src/test/filesystemtemplates/test-handlebars-template7").await();
    assertEquals("\ntext from template8\n\ntext from template7\n\n\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateWithPartialFromSubdir() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "src/test/filesystemtemplates/sub/test-handlebars-template9").await();
    assertEquals("\ntext from template8\n\ntext from template9\n\n\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateNoExtension() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-handlebars-template2").await();
    assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateChangeExtension() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx, "zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-handlebars-template2").await();
    assertEquals("Cheerio badger and fox", normalizeCRLF(render.toString()));
  }

  @Test
  public void testNoSuchTemplate() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx, "zbs");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    assertThrows(Exception.class, () -> engine.render(context, "somedir/foo.hbs").await());
  }

  @Test
  public void testGetHandlebars() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);
    assertNotNull(engine.unwrap());
  }

  @Test
  public void testCachingEnabled() throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".hbs", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    Buffer render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
    // cache is enabled so if we change the content that should not affect the result

    try (PrintWriter out2 = new PrintWriter(temp)) {
      out2.print("after");
      out2.flush();
    }

    render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplatePerf() {
    TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    final long t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      Buffer render = engine.render(context, "somedir/test-handlebars-template2.hbs").await();
      assertEquals("Hello badger and fox", normalizeCRLF(render.toString()));
    }
    final long t1 = System.currentTimeMillis();
    System.out.println(t1 - t0);
  }

  @Test
  public void testBlock() {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject();

    engine.render(context, "templates/index.hbs").await();
  }


  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
