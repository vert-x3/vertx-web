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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.test.core.TestUtils;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Tests for the BodyHandler file streaming feature.
 */
public class BodyHandlerStreamToFileTest extends WebTestBase {

  @Rule
  public TemporaryFolder tempUploads = new TemporaryFolder();

  @AfterClass
  public static void oneTimeTearDown() throws Exception {
    cleanupFileUploadDir();
  }

  @Test
  public void testSmallBodyBelowThreshold() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(1024)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(500); // below 1024 threshold
    router.route().handler(rc -> {
      assertFalse(rc.isBodyStreamed());
      assertNull(rc.getBodyFilePath());
      assertNull(rc.getBodyFile());
      assertEquals(buff, rc.body().buffer());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testLargeBodyAboveThreshold() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(1024)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(5000); // above 1024 threshold
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      assertNotNull(rc.getBodyFilePath());
      // read the file and compare
      Buffer fileContent = vertx.fileSystem().readFileBlocking(rc.getBodyFilePath());
      assertEquals(buff, fileContent);
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testContentTypeMatchExact() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamContentTypes(List.of("application/octet-stream")));

    Buffer buff = TestUtils.randomBuffer(500);
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      Buffer fileContent = vertx.fileSystem().readFileBlocking(rc.getBodyFilePath());
      assertEquals(buff, fileContent);
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testContentTypeMatchWildcard() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamContentTypes(List.of("image/*")));

    Buffer buff = TestUtils.randomBuffer(500);
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      Buffer fileContent = vertx.fileSystem().readFileBlocking(rc.getBodyFilePath());
      assertEquals(buff, fileContent);
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "image/jpeg");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testNonMatchingContentType() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamContentTypes(List.of("image/*")));

    Buffer buff = TestUtils.randomBuffer(500);
    router.route().handler(rc -> {
      assertFalse(rc.isBodyStreamed());
      assertEquals(buff, rc.body().buffer());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/json");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testChunkedTransferExceedingThreshold() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(1024)
      .setStreamAllBinary(true));

    // Send a large body without Content-Length (chunked)
    Buffer buff = TestUtils.randomBuffer(5000);
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      assertNotNull(rc.getBodyFilePath());
      Buffer fileContent = vertx.fileSystem().readFileBlocking(rc.getBodyFilePath());
      assertEquals(buff, fileContent);
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      // No Content-Length header, chunked transfer
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testContentLengthKnownAboveThreshold() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(1024)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(5000);
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      Buffer fileContent = vertx.fileSystem().readFileBlocking(rc.getBodyFilePath());
      assertEquals(buff, fileContent);
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      // Set explicit Content-Length (not chunked)
      req.putHeader("Content-Type", "application/octet-stream");
      req.putHeader("Content-Length", String.valueOf(buff.length()));
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testBodyLimitStillEnforced() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setBodyLimit(2000)
      .setStreamToFile(true)
      .setStreamThreshold(500)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(5000); // exceeds bodyLimit
    router.route().handler(rc -> {
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 413, "Request Entity Too Large", null);
  }

  @Test
  public void testFileCleanupOnEnd() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamAllBinary(true)
      .setDeleteUploadedFilesOnEnd(true));

    Buffer buff = TestUtils.randomBuffer(500);
    String[] filePaths = new String[1];
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      filePaths[0] = rc.getBodyFilePath();
      assertNotNull(filePaths[0]);
      assertTrue(vertx.fileSystem().existsBlocking(filePaths[0]));
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);

    // After the response, the file should be cleaned up
    // Give a short time for async cleanup
    CountDownLatch latch = new CountDownLatch(1);
    vertx.setTimer(500, id -> {
      assertFalse(vertx.fileSystem().existsBlocking(filePaths[0]));
      latch.countDown();
    });
    awaitLatch(latch);
  }

  @Test
  public void testGetBodyFile() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(500);
    router.route().handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      FileUpload bodyFile = rc.getBodyFile();
      assertNotNull(bodyFile);
      assertEquals("body", bodyFile.name());
      assertEquals(rc.getBodyFilePath(), bodyFile.uploadedFileName());
      assertEquals("application/octet-stream", bodyFile.contentType());
      assertEquals(500, bodyFile.size());
      assertNull(bodyFile.fileName());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testStreamingDisabledByDefault() throws Exception {
    // Use default BodyHandler - streaming should be disabled
    router.clear();
    router.route().handler(BodyHandler.create());

    Buffer buff = TestUtils.randomBuffer(5000);
    router.route().handler(rc -> {
      assertFalse(rc.isBodyStreamed());
      assertEquals(buff, rc.body().buffer());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testStreamAllBinaryMode() throws Exception {
    String uploadsDir = tempUploads.newFolder().getPath();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDir)
      .setStreamToFile(true)
      .setStreamThreshold(100)
      .setStreamAllBinary(true));

    Buffer buff = TestUtils.randomBuffer(500);

    // text/plain should NOT be streamed
    router.route("/text").handler(rc -> {
      assertFalse(rc.isBodyStreamed());
      assertEquals(buff, rc.body().buffer());
      rc.response().end();
    });

    // application/octet-stream should be streamed
    router.route("/binary").handler(rc -> {
      assertTrue(rc.isBodyStreamed());
      rc.response().end();
    });

    testRequest(HttpMethod.POST, "/text", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "text/plain");
      req.write(buff);
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/binary", req -> {
      req.setChunked(true);
      req.putHeader("Content-Type", "application/octet-stream");
      req.write(buff);
    }, 200, "OK", null);
  }
}
