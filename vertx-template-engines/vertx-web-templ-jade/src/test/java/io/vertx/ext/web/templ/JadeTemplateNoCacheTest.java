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
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JadeTemplateNoCacheTest extends WebTestBase {

  protected VertxOptions getOptions() {
    return new VertxOptions().setFileResolverCachingEnabled(false);
  }

  private void testTemplateHandler(TemplateEngine engine, String directoryName, String templateName,
                                   String expected) throws Exception {
    router.route().handler(context -> {
      context.put("foo", "badger");
      context.next();
    });
    router.route().handler(TemplateHandler.create(engine, directoryName, "text/plain"));
    testRequest(HttpMethod.GET, "/" + templateName, 200, "OK", expected);
  }

  @Test
  public void testCachingDisabled() throws Exception {
    System.setProperty("vertx.mode", "dev");
    TemplateEngine engine = JadeTemplateEngine.create();

    PrintWriter out;
    File temp = File.createTempFile("template", ".jade", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "<before></before>");

    // cache is disabled so if we change the content that should affect the result

    out = new PrintWriter(temp);
    out.print("after");
    out.flush();
    out.close();

    testTemplateHandler(engine, ".", temp.getName(), "<after></after>");
  }
}
