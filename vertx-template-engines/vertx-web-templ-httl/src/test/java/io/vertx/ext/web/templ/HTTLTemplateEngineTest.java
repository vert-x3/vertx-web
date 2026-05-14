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
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.httl.HTTLTemplateEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
public class HTTLTemplateEngineTest {
  private static Vertx vertx;

  @BeforeAll
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
  }

  @Test
  public void testTemplateHandlerOnClasspath() throws Exception {
    TemplateEngine engine = HTTLTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-httl-template1.httl").await();
    assertEquals("<!--  -->\nHello badger and fox\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testCachingEnabled() throws Exception {
    System.setProperty("vertxweb.environment", "production");
    TemplateEngine engine = HTTLTemplateEngine.create(vertx);

    File temp = File.createTempFile("template", ".httl", new File("target/classes"));
    temp.deleteOnExit();

    try (PrintWriter out = new PrintWriter(temp)) {
      out.print("before");
      out.flush();
    }

    Buffer render = engine.render(new JsonObject(), temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
    // cache is enabled so if we change the content that should not affect the result

    try (PrintWriter out2 = new PrintWriter(temp)) {
      out2.print("after");
      out2.flush();
    }

    render = engine.render(new JsonObject(), temp.getName()).await();
    assertEquals("before", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching() throws Exception {
    System.setProperty("vertxweb.environment", "development");
    testTemplateHandlerOnClasspath();
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = HTTLTemplateEngine.create(vertx);

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-httl-template1").await();
    assertEquals("<!--  -->\nHello badger and fox\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testTemplateHandlerChangeExtension() throws Exception {
    TemplateEngine engine = HTTLTemplateEngine.create(vertx, "mvl");

    final JsonObject context = new JsonObject()
      .put("foo", "badger")
      .put("bar", "fox");

    Buffer render = engine.render(context, "somedir/test-httl-template1").await();
    assertEquals("<!--  -->\nCheerio badger and fox\n", normalizeCRLF(render.toString()));
  }

  @Test
  public void testNoSuchTemplate() {
    TemplateEngine engine = HTTLTemplateEngine.create(vertx);

    assertThrows(Exception.class, () -> engine.render(new JsonObject(), "not-found").await());
  }

  // For windows testing
  static String normalizeCRLF(String s) {
    return s.replace("\r\n", "\n");
  }
}
