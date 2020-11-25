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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.Test;

import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
@RunWith(VertxUnitRunner.class)
public class RockerTemplateEngineTest {

  @Test
  public void testTemplateHandler(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"));

    engine.render(context, "somedir/TestRockerTemplate2.rocker.html", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerNoExtension(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2"));

    engine.render(context, "somedir/TestRockerTemplate2", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerChangeExtension(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create("rocker.raw");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate3"));

    engine.render(context, "somedir/TestRockerTemplate3", should.asyncAssertSuccess(render -> {
      should.assertEquals("\nCheerio badger and fox\nRequest path is /TestRockerTemplate3\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testTemplateHandlerIncludes(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate3"));

    engine.render(context, "somedir/Base", should.asyncAssertSuccess(render -> {
      should.assertEquals("Vert.x rules\n", normalizeCRLF(render.toString()));
    }));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject();

    engine.render(context, "nosuchtemplate.rocker.html", should.asyncAssertFailure());
  }

  @Test
  public void testTemplateWithUndrescoreKeysHandler(TestContext should) {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"))
      .put("__body-handled", true);

    engine.render(context, "somedir/TestRockerTemplate2.rocker.html", should.asyncAssertSuccess(render -> {
      should.assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html\n", normalizeCRLF(render.toString()));
    }));
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
