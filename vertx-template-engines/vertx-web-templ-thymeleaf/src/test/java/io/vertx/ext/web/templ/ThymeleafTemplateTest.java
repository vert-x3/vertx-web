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
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ThymeleafTemplateTest extends WebTestBase {

  static {
    System.setProperty("vertx.disableFileCaching", "true");
  }

  @Test
  public void testTemplateHandlerOnClasspath() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-thymeleaf-template2.html");
  }

  @Test
  public void testTemplateHandlerOnFileSystem() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-thymeleaf-template3.html");
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateHandlerOnClasspath();
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/html"));
    String expected =
      "<!doctype html>\n" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
        "<head>\n" +
        "  <meta charset=\"utf-8\">\n" +
        "</head>\n" +
        "<body>\n" +
        "<p>badger</p>\n" +
        "<p>fox</p>\n" +
        "<p>/" + templateName + "</p>\n" +
        "<p>blah</p>\n" +
        "<p>wibble</p>\n" +
        "</body>\n" +
        "</html>";

    testRequest(HttpMethod.GET, "/" + templateName + "?param1=blah&param2=wibble", 200, "OK", expected);
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();
    router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.html", "text/html"));
    testRequest(HttpMethod.GET, "/foo.html", 500, "Internal Server Error");
  }

  @Test
  public void testWithLocale() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();

    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, "somedir", "text/html"));

    testRequest(HttpMethod.GET, "/test-thymeleaf-template2.html?param1=blah&param2=wibble", req -> req.putHeader("Accept-Language", "en-gb"), 200, "OK", null);
  }


  @Test
  public void testGetThymeLeafTemplateEngine() throws Exception {
    ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
    assertNotNull(engine.getThymeleafTemplateEngine());
  }

  @Test
  public void testFragmentedTemplates() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-thymeleaf-fragmented.html");
  }

  @Test
  public void testWithLocale2() throws Exception {
    TemplateEngine engine = ThymeleafTemplateEngine.create();

    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, "somedir", "text/html"));

    testRequest(HttpMethod.GET, "/test-thymeleaf-template2.html?param1=blah&param2=wibble", req -> req.putHeader("Accept-Language", "ru"), 200, "OK", null);
  }

  @Test
  public void testCachingEnabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = ThymeleafTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".html", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "before");

    // cache is enabled so if we change the content that should not affect the result

    out = new PrintWriter(temp);
    out.print("after");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "before");
  }

  @Test
  public void testCachingDisabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    TemplateEngine engine = ThymeleafTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".html", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "before");

    // cache is disabled so if we change the content that should affect the result

    out = new PrintWriter(temp);
    out.print("after");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "after");
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
}
