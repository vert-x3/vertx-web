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
package io.vertx.ext.web.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.impl.MultipartFormUpload;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.test.core.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MultipartFormUploadTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  private Vertx vertx;
  private File largeFile;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    largeFile = testFolder.newFile("large.dat");
    Files.write(largeFile.toPath(), TestUtils.randomAlphaString(32 * 1024).getBytes());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testSimpleAttribute(TestContext ctx) throws Exception {
    Async async = ctx.async();
    Buffer result = Buffer.buffer();
    MultipartFormUpload upload = new MultipartFormUpload(vertx.getOrCreateContext(), MultipartForm.create().attribute("foo", "bar"), false);
    upload.run();
    upload.endHandler(v -> {
      assertEquals("foo=bar", result.toString());
      async.complete();
    });
    upload.handler(result::appendBuffer);
    upload.resume();
  }

  @Test
  public void testFileUpload(TestContext ctx) throws Exception {
    Async async = ctx.async();
    MultipartFormUpload upload = new MultipartFormUpload(vertx.getOrCreateContext(), MultipartForm.create().textFileUpload(
      "the-file",
      largeFile.getName(),
      largeFile.getAbsolutePath(),
      "text/plain"), true);
    List<Buffer> buffers = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger end = new AtomicInteger();
    upload.run();
    upload.endHandler(v -> {
      assertEquals(0, end.getAndIncrement());
      ctx.assertTrue(buffers.size() > 0);
      async.complete();
    });
    upload.handler(buffer -> {
      assertEquals(0, end.get());
      buffers.add(buffer);
    });
    upload.resume();
  }
}
