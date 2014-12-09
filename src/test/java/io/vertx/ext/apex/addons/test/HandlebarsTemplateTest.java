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

package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.HandlebarsTemplateEngine;
import io.vertx.ext.apex.addons.TemplateEngine;
import io.vertx.ext.apex.addons.TemplateHandler;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateTest extends ApexTestBase {

  @Test
  public void testTemplateHandler() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "test-handlebars-template.hbs");
  }

  @Test
  public void testTemplateHandlerwithPrefixAndExtension1() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir/", "hbs");
    testTemplateHandler(engine, "test-handlebars-template2.hbs");
  }

  @Test
  public void testTemplateHandlerwithPrefixAndExtension2() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir/", "hbs");
    testTemplateHandler(engine, "test-handlebars-template2");
  }

  @Test
  public void testTemplateHandlerwithPrefixAndExtension3() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir/", ".hbs");
    testTemplateHandler(engine, "test-handlebars-template2");
  }

  @Test
  public void testTemplateHandlerwithPrefixAndExtension4() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir", ".hbs");
    testTemplateHandler(engine, "test-handlebars-template2");
  }

  @Test
  public void testTemplateHandlerwithPrefixAndExtension5() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir", ".foo");
    testTemplateHandler(engine, "test-handlebars-template2");
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "test-handlebars-template");
  }

  @Test
  public void testTemplateHandlerwithMaxCacheSize() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create("somedir", ".foo", 12);
    testTemplateHandler(engine, "test-handlebars-template2");
  }

  private void testTemplateHandler(TemplateEngine engine, String templateName) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.templateHandler(engine, templateName, "text/plain"));
    String expected = "Hello badger and fox";
    testRequest(HttpMethod.GET, "/", 200, "OK", expected);
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    router.route().handler(TemplateHandler.templateHandler(engine, "nosuchtemplate.hbs", "text/html"));
    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
  }

}