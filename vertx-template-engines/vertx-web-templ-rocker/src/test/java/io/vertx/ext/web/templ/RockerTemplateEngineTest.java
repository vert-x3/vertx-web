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

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;

import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class RockerTemplateEngineTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    System.setProperty("vertx.disableFileCaching", "true");
    super.setUp();
  }

  @Test
  public void testTemplateHandler() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "TestRockerTemplate2.rocker.html",
        "Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html");
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "TestRockerTemplate2",
        "Hello badger and fox\nRequest path is /TestRockerTemplate2");
  }

  @Test
  public void testTemplateHandlerChangeExtension() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create().setExtension("rocker.raw");
    testTemplateHandler(engine, "somedir", "TestRockerTemplate3",
        "\nCheerio badger and fox\nRequest path is /TestRockerTemplate3");
  }

  @Test
  public void testTemplateHandlerIncludes() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "Base", "Vert.x rules");
  }
  
  @Test
  public void testTemplateHandlerNotIncludedArg() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create();
    router.route().handler(context -> {
      context.put("notExistingArg", true);
      context.next();
    });
    testTemplateHandler(engine, "somedir", "TestRockerTemplate2.rocker.html",
        "Hello badger and fox\nRequest path is /TestRockerTemplate2.rocker.html");
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    TemplateEngine engine = RockerTemplateEngine.create();
    router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.rocker.html", "text/html"));
    testRequest(HttpMethod.GET, "/foo.rocker.html", 500, "Internal Server Error");
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName, String expected)
      throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }

}
