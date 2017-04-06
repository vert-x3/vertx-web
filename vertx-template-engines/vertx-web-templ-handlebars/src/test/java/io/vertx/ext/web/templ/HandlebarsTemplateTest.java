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

import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.ValueResolver;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    System.setProperty("vertx.disableFileCaching", "true");
    super.setUp();
  }

  @Test
  public void testTemplateOnClasspath() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-handlebars-template2.hbs", "Hello badger and fox");
  }

  @Test
  public void testTemplateJsonObjectResolver() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    JsonObject json = new JsonObject();
    json.put("bar", new JsonObject().put("one", "badger").put("two", "fox"));

    testTemplateHandlerWithContext(engine, "src/test/filesystemtemplates", "test-handlebars-template4.hbs", "Goodbye badger and fox", context -> {
      context.put("foo", json);
      context.next();
    });
  }

  @Test
  public void testTemplateJsonArrayResolver() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));
    String expected = "Iterator: badger,fox,{&quot;name&quot;:&quot;joe&quot;}, Element by index:fox - joe - Out of bounds:  - Size:3";
    testTemplateHandlerWithContext(engine, "src/test/filesystemtemplates", "test-handlebars-template5.hbs", expected, context -> {
      context.put("foo", jsonArray);
      context.next();
    });
  }

  @Test
  public void testCustomResolver() throws Exception {
    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();
    engine.setResolvers(new ValueResolver() {
      @Override
      public Object resolve(Object context, String name) {
        return "custom";
      }

      @Override
      public Object resolve(Object context) {
        return "custom";
      }

      @Override
      public Set<Entry<String, Object>> propertySet(Object context) {
        return Collections.emptySet();
      }
    });

    testTemplateHandlerWithContext(engine, "src/test/filesystemtemplates", "test-handlebars-template3.hbs", "Goodbye custom and custom", context -> {
      context.put("foo", "Badger");
      context.put("bar", "Fox");
      context.next();
    });
  }

  @Test
  public void testTemplateJsonArrayResolverError() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    JsonArray jsonArray = new JsonArray();
    jsonArray.add("badger").add("fox").add(new JsonObject().put("name", "joe"));

    AtomicReference<RoutingContext> contextRef = new AtomicReference<>();
    router.route().handler(context -> {
      contextRef.set(context);
      context.put("foo", jsonArray);
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, "src/test/filesystemtemplates", "text/plain"));
    testRequest(HttpMethod.GET, "/" + "test-handlebars-template6.hbs", 500, "Internal Server Error");
    if(contextRef.get().failure() instanceof HandlebarsException) {
      HandlebarsException exception = ((HandlebarsException)contextRef.get().failure());
      assertTrue(exception.getMessage().contains("test-handlebars-template6.hbs:1:19"));
    } else {
      fail("We would expect an handlebars exception with detailed location information.");
    }
  }

  private void testTemplateHandlerWithContext(TemplateEngine engine, String directoryName, String templateName, String expected,
    Handler<RoutingContext> contextHandler) throws Exception {
    router.route().handler(contextHandler);
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }

  @Test
  public void testTemplateOnFileSystem() throws Exception {
    TemplateEngine engine = HandlebarsTemplateEngine.create();
    testTemplateHandler(engine, "src/test/filesystemtemplates", "test-handlebars-template3.hbs", "Goodbye badger and fox");
  }

  @Test
  public void testTemplateOnClasspathDisableCaching() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "true");
    testTemplateOnClasspath();
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

  @Test
  public void testCachingEnabled() throws Exception {
    System.setProperty(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME, "false");
    TemplateEngine engine = HandlebarsTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".hbs", new File("target/classes"));
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
    TemplateEngine engine = HandlebarsTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".hbs", new File("target/classes"));
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
}
