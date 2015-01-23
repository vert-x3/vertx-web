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
import io.vertx.ext.apex.addons.MVELTemplateEngine;
import io.vertx.ext.apex.addons.PathTemplateHandler;
import io.vertx.ext.apex.addons.TemplateEngine;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MVELTemplateTest extends ApexTestBase {

  @Test
  public void testTemplateHandler() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-mvel-template2.templ");
  }

  @Test
  public void testTemplateHandlerNoExtension() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    testTemplateHandler(engine, "somedir", "test-mvel-template2");
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.put("bar", "fox");
      context.next();
    });
    router.route().handler(PathTemplateHandler.templateHandler(engine, directoryName, "text/plain"));
    String expected = "Hello badger and fox";
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }

  @Test
  public void testNoSuchTemplate() throws Exception {
    TemplateEngine engine = MVELTemplateEngine.create();
    router.route().handler(PathTemplateHandler.templateHandler(engine, "nosuchtemplate.templ", "text/plain"));
    testRequest(HttpMethod.GET, "/foo.templ", 500, "Internal Server Error");
  }

}