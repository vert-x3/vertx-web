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
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.ext.web.templ.jade.JadeTemplateEngine;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class JadeTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    TemplateEngine engine = JadeTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-jade-template2.jade"));

    engine.render(context, "somedir/test-jade-template2.jade", should.asyncAssertSuccess(render -> {
      should.assertEquals("<!DOCTYPE html><html><head><title>badger/test-jade-template2.jade</title></head><body></body></html>", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnFileSystem(TestContext should) {
    TemplateEngine engine = JadeTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-jade-template3.jade"));

    engine.render(context, "src/test/filesystemtemplates/test-jade-template3.jade", should.asyncAssertSuccess(render -> {
      should.assertEquals("<!DOCTYPE html><html><head><title>badger/test-jade-template3.jade</title></head><body></body></html>", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) {
    TemplateEngine engine = JadeTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-jade-template2.jade"));

    engine.render(context, "somedir/test-jade-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("<!DOCTYPE html><html><head><title>badger/test-jade-template2.jade</title></head><body></body></html>", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    TemplateEngine engine = JadeTemplateEngine.create(vertx, "made");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-jade-template2.jade"));

    engine.render(context, "somedir/test-jade-template2", should.asyncAssertSuccess(render -> {
      should.assertEquals("<!DOCTYPE html><html><head><title>aardvark/test-jade-template2.jade</title></head><body></body></html>", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = JadeTemplateEngine.create(vertx, "made");

    final JsonObject context = new JsonObject();

    engine.render(context, "somedir/foo", should.asyncAssertFailure());
  }

  @Test
  public void testGetJadeConfiguration() {
    JadeTemplateEngine engine = JadeTemplateEngine.create(vertx);
    assertNotNull(engine.getJadeConfiguration());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = JadeTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".jade", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("<before></before>", normalizeCRLF(render.toString()));
      // cache is enabled so if we change the content that should not affect the result

      try (PrintWriter out2 = new PrintWriter(temp)) {
        out2.print("after");
        out2.flush();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("<before></before>", normalizeCRLF(render2.toString()));
      }));
    }));
  }


  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
