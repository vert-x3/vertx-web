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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.ext.web.templ.pug.PugTemplateEngine;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This implementation has been copied from
 * <a href="https://github.com/vert-x3/vertx-web/blob/4.0.0/vertx-template-engines/vertx-web-templ-jade/src/test/java/io/vertx/ext/web/templ/JadeTemplateTest.java">
 * JadeTemplateTest.java</a>.
 * Authors of JadeTemplateTest.java are <a href="http://tfox.org">Tim Fox</a>, Julien Viet (vietj),
 * Paulo Lopes (pmlopes), Kevin Macksamie (k-mack), Clement Escoffier (cescoffier).
 *
 * For authors of this file see git history.
 */
@RunWith(VertxUnitRunner.class)
public class PugTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-pug-template2.pug"));

    engine.render(context, "somedir/test-pug-template2.pug", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>badger/test-pug-template2.pug</title>");
    }));
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-pug-template3.pug"));

    engine.render(context, "src/test/filesystemtemplates/test-pug-template3.pug", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>badger/test-pug-template3.pug</title>");
    }));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-pug-template2.pug"));

    engine.render(context, "somedir/test-pug-template2", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>badger/test-pug-template2.pug</title>");
    }));
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx, "made");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-pug-template2.pug"));

    engine.render(context, "somedir/test-pug-template2", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>aardvark/test-pug-template2.pug</title>");
    }));
  }

  @Test
  public void testDefaultEncoding(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx);

    engine.render(new JsonObject(), "somedir/test-pug-template-umlaut", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>&auml;</title>");
    }));
  }

  @Test
  public void testIsoEncoding(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx, "pug", StandardCharsets.ISO_8859_1.name());

    engine.render(new JsonObject(), "somedir/test-pug-template-umlaut", should.asyncAssertSuccess(render -> {
      assertContains(render, "<title>&Atilde;&curren;</title>");
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = PugTemplateEngine.create(vertx, "made");

    final JsonObject context = new JsonObject();

    engine.render(context, "somedir/foo", should.asyncAssertFailure());
  }

  @Test
  public void testGetPugConfiguration() {
    PugTemplateEngine engine = PugTemplateEngine.create(vertx);
    assertNotNull(engine.unwrap());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = PugTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".pug", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("<before></before>", render.toString());
      // cache is enabled so if we change the content that should not affect the result

      try (PrintWriter out2 = new PrintWriter(temp)) {
        out2.print("after");
        out2.flush();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("<before></before>", render2.toString());
      }));
    }));
  }

  private void assertContains(Buffer render, String chunk) {
    assertTrue(chunk, render.toString().contains(chunk));
  }
}
