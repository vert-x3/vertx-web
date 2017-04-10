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
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class FreeMarkerTemplateTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    System.setProperty("vertx.disableFileCaching", "true");
    super.setUp();
  }

  @Test
  public void testTemplateHandlerOnClasspath() throws Exception {
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-freemarker-template2.ftl", "Hello badger and fox\nRequest path is /test-freemarker-template2.ftl");
  }

  @Test
  public void testCachingEnabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = FreeMarkerTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".ftl", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    testTemplateHandler(engine, "", temp.getName(), "before");

    // cache is enabled so if we change the content that should not affect the result

    out = new PrintWriter(temp);
    out.print("after");
    out.flush();
    out.close();

    testTemplateHandler(engine, "", temp.getName(), "before");
  }

  @Test
  public void testCachingDisabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    TemplateEngine engine = FreeMarkerTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".ftl", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    testTemplateHandler(engine, "", temp.getName(), "before");

    // cache is disabled so if we change the content that should affect the result

    out = new PrintWriter(temp);
    out.print("after");
    out.flush();
    out.close();

    testTemplateHandler(engine, "", temp.getName(), "after");
  }

  @Test
  public void testTemplateHandlerOnFileSystem() throws Exception {
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-freemarker-template3.ftl", "Hello badger and fox\nRequest path is /test-freemarker-template3.ftl");
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateHandlerOnClasspath();
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-freemarker-template2", "Hello badger and fox\nRequest path is /test-freemarker-template2");
  }

  @Test
  public void testTemplateHandlerChangeExtension() throws Exception {
    TemplateEngine engine = FreeMarkerTemplateEngine.create().setExtension("mvl");
    testTemplateHandler(engine, "somedir", "test-freemarker-template2", "Cheerio badger and fox\nRequest path is /test-freemarker-template2");
  }

  @Test
  public void testTemplateHandlerIncludes() throws Exception {
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "base", "Vert.x rules");
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName,
                                   String expected) throws Exception {
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
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.templ", "text/plain"));
    testRequest(HttpMethod.GET, "/foo.templ", 500, "Internal Server Error");
  }

}
