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
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2.ftl", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template2.ftl\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".ftl", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    engine.render(new JsonObject(), temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("before", normalizeCRLF(render.toString()));
      // cache is enabled so if we change the content that should not affect the result

      try (PrintWriter out2 = new PrintWriter(temp)) {
        out2.print("after");
        out2.flush();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("before", normalizeCRLF(render2.toString()));
      }));
    }));
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template3.ftl"));

    engine.render(context, "src/test/filesystemtemplates/test-freemarker-template3.ftl", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template3.ftl\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext context) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(context);
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-freemarker-template2.ftl\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx, "mvl");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/test-freemarker-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("Cheerio badger and fox\nRequest path is /test-freemarker-template2.ftl\n",  normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerIncludes(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-freemarker-template2.ftl"));

    engine.render(context, "somedir/base", should.asyncAssertSuccess(render -> {
      should.assertEquals("Vert.x rules", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);

    engine.render(new JsonObject(), "not-found", should.asyncAssertFailure());
  }

  @Test
  public void testLang(TestContext should) {
    TemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);
    engine.render(new JsonObject(), "somedir/lang.ftl", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello world\n", normalizeCRLF(render.toString()));
    }));

    engine.render(new JsonObject().put("lang", "el"), "somedir/lang.ftl", should.asyncAssertSuccess(render -> {
      should.assertEquals("Γειά σου Κόσμε\n", normalizeCRLF(render.toString()));
    }));
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
