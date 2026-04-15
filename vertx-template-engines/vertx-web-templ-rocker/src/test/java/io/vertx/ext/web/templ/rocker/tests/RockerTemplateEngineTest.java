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

package io.vertx.ext.web.templ.rocker.tests;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.jupiter.api.Test;

import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class RockerTemplateEngineTest {

  @Test
  public void testTemplateHandler() {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"));

    Buffer render = engine.render(context, "somedir/TestRockerTemplate2.rocker.html").await();
    assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerNoExtension() {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2"));

    Buffer render = engine.render(context, "somedir/TestRockerTemplate2").await();
    assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerChangeExtension() {
    TemplateEngine engine = RockerTemplateEngine.create("rocker.raw");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate3"));

    Buffer render = engine.render(context, "somedir/TestRockerTemplate3").await();
    assertEquals("\nCheerio badger and fox\nRequest path is /TestRockerTemplate3\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerIncludes() {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate3"));

    Buffer render = engine.render(context, "somedir/Base").await();
    assertEquals("Vert.x rules\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testNoSuchTemplate() {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject();

    assertThrows(Exception.class, () -> engine.render(context, "nosuchtemplate.rocker.html").await());
  }

  @Test
  public void testTemplateWithUndrescoreKeysHandler() {
    TemplateEngine engine = RockerTemplateEngine.create();

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/TestRockerTemplate2.rocker.html"))
      .put("__body-handled", true);

    Buffer render = engine.render(context, "somedir/TestRockerTemplate2.rocker.html").await();
    assertEquals("Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html\n", normalizeCRLF(render.toString()));
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
