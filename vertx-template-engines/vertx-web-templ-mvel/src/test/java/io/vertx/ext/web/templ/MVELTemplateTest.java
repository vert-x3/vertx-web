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

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MVELTemplateTest extends WebTestBase {

  protected VertxOptions getOptions() {
    return new VertxOptions().setFileResolverCachingEnabled(true);
  }

  @Test
  public void testTemplateHandlerOnClasspath() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-mvel-template2.templ", "Hello badger and fox\nRequest path is /test-mvel-template2.templ");
  }

  @Test
  public void MVELTemplateTestMVELTemplateTestMVELTemplateTest() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-mvel-template3.templ", "Hello badger and fox\nRequest path is /test-mvel-template3.templ");
  }

  @Test
  public void testTemplateHandlerWithInclude() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-mvel-template4.templ", "Hello badger and fox\nRequest path is /test-mvel-template4.templ");
  }

  @Test
  public void testTemplateHandlerOnClasspathDisableCaching() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateHandlerOnClasspath();
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-mvel-template2", "Hello badger and fox\nRequest path is /test-mvel-template2");
  }

  @Test
  public void testTemplateHandlerChangeExtension() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create().setExtension("bempl");
    testTemplateHandler(engine, "somedir", "test-mvel-template2", "Cheerio badger and fox\nRequest path is /test-mvel-template2");
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
    TemplateEngine engine = MVELTemplateEngine.create();
    router.route().handler(TemplateHandler.create(engine, "nosuchtemplate.templ", "text/plain"));
    testRequest(HttpMethod.GET, "/foo.templ", 500, "Internal Server Error");
  }

  @Test
  public void testCachingEnabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = MVELTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".templ", new File("target/classes"));
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
}
