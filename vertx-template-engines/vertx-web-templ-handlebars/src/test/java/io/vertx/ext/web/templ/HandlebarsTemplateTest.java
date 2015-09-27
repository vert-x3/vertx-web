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

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateTest extends WebTestBase {

  @Test
  public void testTemplateOnClasspath() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-handlebars-template2.hbs", "Hello badger and fox");
  }

  @Test
  public void testTemplateOnFileSystem() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-handlebars-template3.hbs", "Goodbye badger and fox");
  }

  @Test
  public void testTemplateNoExtension() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-handlebars-template2", "Hello badger and fox");
  }

  @Test
  public void testTemplateChangeExtension() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create().setExtension("zbs");
    testTemplateHandler(engine, "somedir", "test-handlebars-template2", "Cheerio badger and fox");
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName, String expected) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    router.route().handler(TemplateHandler.create(engine, "somedir", "text/html"));
    testRequest(HttpMethod.GET, "/foo.hbs", 500, "Internal Server Error");
  }

  @Test
  public void testGetHandlebars() throws Exception {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();
    assertNotNull(engine.getHandlebars());
  }

}
