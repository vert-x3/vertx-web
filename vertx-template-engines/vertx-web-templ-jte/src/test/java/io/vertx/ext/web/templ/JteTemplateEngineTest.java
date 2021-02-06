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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.jte.JteTemplateEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Paths;

/**
 * @author <a href="mailto:andy@mazebert.com">Andreas Hager</a>
 */
@RunWith(VertxUnitRunner.class)
public class JteTemplateEngineTest {

  private static TemplateEngine engine;

  @BeforeClass
  public static void before() {
    Vertx vertx = Vertx.vertx();
    engine = JteTemplateEngine.create(vertx, Paths.get("src", "test", "jte"));
  }

  @Test
  public void testTemplateHandler(TestContext should) {
    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/testTemplate2.jte"));

    engine.render(context, "testTemplate2.jte", should.asyncAssertSuccess(render ->
      should.assertEquals("\nHello badger and fox\nRequest path is /testTemplate2.jte\n", normalizeCRLF(render.toString()))
    ));
  }

  @Test
  public void testTemplateHandlerIncludes(TestContext should) {
    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox")
      .put("context", new JsonObject().put("path", "/base"));

    engine.render(context, "base.jte", should.asyncAssertSuccess(render ->
      should.assertEquals("Vert.x rules\n\n", normalizeCRLF(render.toString()))
    ));
  }

  @Test
  public void testNoSuchTemplate(TestContext should) {
    final JsonObject context = new JsonObject();

    engine.render(context, "nosuchtemplate.jte", should.asyncAssertFailure());
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
