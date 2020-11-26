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
import io.vertx.core.impl.Utils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class MVELTemplateTest {

  private static Vertx vertx;

  @BeforeClass
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath(TestContext should) {
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-mvel-template2.templ"));

    String tmplPath = "somedir/test-mvel-template2.templ".replace('/', File.separatorChar);
    engine.render(context, tmplPath, should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-mvel-template2.templ\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void MVELTemplateTestMVELTemplateTestMVELTemplateTest(TestContext should) {
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-mvel-template3.templ"));

    String tmplPath = "src/test/filesystemtemplates/test-mvel-template3.templ".replace('/', File.separatorChar);
    engine.render(context, tmplPath, should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-mvel-template3.templ\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerWithInclude(TestContext should) {
    // Cannot pass on windows due to
    // File file = new File(runtime.getRelPath().peek() + "/" + fileName);
    // in org.mvel2.templates.res.CompiledIncludeNode
    Assume.assumeFalse(Utils.isWindows());
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-mvel-template4.templ"));

    String tmplPath = "src/test/filesystemtemplates/test-mvel-template4.templ".replace('/', File.separatorChar);
    engine.render(context, tmplPath, should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\n\nRequest path is /test-mvel-template4.templ\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching(TestContext should) {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath(should);
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) {
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-mvel-template2.templ"));

    String tmplPath = "somedir/test-mvel-template2".replace('/', File.separatorChar);
    engine.render(context, tmplPath, should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /test-mvel-template2.templ\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    TemplateEngine engine = MVELTemplateEngine.create(vertx, "bempl");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    context.put("context", new JsonObject().put("path", "/test-mvel-template2"));

    String tmplPath = "somedir/test-mvel-template2".replace('/', File.separatorChar);
    engine.render(context, tmplPath, should.asyncAssertSuccess(render -> {
      should.assertEquals("Cheerio badger and fox\nRequest path is /test-mvel-template2\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = MVELTemplateEngine.create(vertx);
    engine.render(new JsonObject(), "nosuchtemplate.templ", should.asyncAssertFailure());
  }

  @Test
  public void testCachingEnabled(TestContext should) throws IOException {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".templ", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    engine.render(new JsonObject(), temp.getParent() + File.separatorChar + temp.getName(), should.asyncAssertSuccess(render -> {
      should.assertEquals("before", render.toString());
      // cache is enabled so if we change the content that should not affect the result

      try {
        PrintWriter out2 = new PrintWriter(temp);
        out2.print("after");
        out2.flush();
        out2.close();
      } catch (IOException e) {
        should.fail(e);
      }

      engine.render(new JsonObject(), temp.getParent() + File.separatorChar + temp.getName(), should.asyncAssertSuccess(render2 -> {
        should.assertEquals("before", render2.toString());
      }));

    }));
  }

  // For windows testing
  private static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
