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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class ThymeleafTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileResolverCachingEnabled(true));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/test-thymeleaf-template2.html"));

    engine.render(context, "somedir/test-thymeleaf-template2.html", render -> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!doctype html>\n" +
          "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
          "<head>\n" +
          "  <meta charset=\"utf-8\">\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "<p>/test-thymeleaf-template2.html</p>\n" +
          "</body>\n" +
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/test-thymeleaf-template2.html"));

    engine.render(context, "src/test/filesystemtemplates/test-thymeleaf-template3.html", render -> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!doctype html>\n" +
          "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
          "<head>\n" +
          "  <meta charset=\"utf-8\">\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "<p>/test-thymeleaf-template2.html</p>\n" +
          "</body>\n" +
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject();

    engine.render(context, "nosuchtemplate.html", render -> {
      should.assertFalse(render.succeeded());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testWithLocale(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("lang", "en-gb")
      .put("context", new JsonObject().put("path", "/test-thymeleaf-template2.html"));

    engine.render(context, "somedir/test-thymeleaf-template2.html", render -> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!doctype html>\n" +
          "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
          "<head>\n" +
          "  <meta charset=\"utf-8\">\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "<p>/test-thymeleaf-template2.html</p>\n" +
          "</body>\n" +
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testGetThymeLeafTemplateEngine() {
    ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create(vertx);
    assertNotNull(engine.getThymeleafTemplateEngine());
  }

  @Test
  public void testFragmentedTemplates(TestContext should) {
    final Async test = should.async();
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/test-thymeleaf-template2.html"));

    engine.render(context, "somedir/test-thymeleaf-fragmented.html", render -> {
      should.assertTrue(render.succeeded());

      final String expected =
        "<!doctype html>\n" +
          "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
          "<head>\n" +
          "  <meta charset=\"utf-8\">\n" +
          "</head>\n" +
          "<body>\n" +
          "<p>badger</p>\n" +
          "<p>fox</p>\n" +
          "<p>/test-thymeleaf-template2.html</p>\n" +
          "</body>\n" +
          "</html>\n";

      should.assertEquals(expected, render.result().toString());
      test.complete();
    });
    test.await();
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    final Async test = should.async();

    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".html", new File("target/classes"));
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
