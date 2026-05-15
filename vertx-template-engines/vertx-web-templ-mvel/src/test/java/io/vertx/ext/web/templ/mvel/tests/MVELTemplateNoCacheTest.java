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

package io.vertx.ext.web.templ.mvel.tests;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.common.template.TemplateEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MVELTemplateNoCacheTest {

  private static Vertx vertx;

  @BeforeAll
  public static void before() {
    vertx = Vertx.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(false)));
  }

  @Test
  public void testCachingDisabled() throws Throwable {
    System.setProperty("vertxweb.environment", "development");
    TemplateEngine engine = MVELTemplateEngine.create(vertx);

    PrintWriter out;
    File temp = File.createTempFile("template", ".templ", new File("target/classes"));
    temp.deleteOnExit();

    out = new PrintWriter(temp);
    out.print("before");
    out.flush();
    out.close();

    Buffer render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("before", render.toString());
    // cache is enabled so if we change the content that should not affect the result

    PrintWriter out2 = new PrintWriter(temp);
    out2.print("after");
    out2.flush();
    out2.close();

    render = engine.render(new JsonObject(), temp.getParent() + "/" + temp.getName()).await();
    assertEquals("after", render.toString());
  }
}
