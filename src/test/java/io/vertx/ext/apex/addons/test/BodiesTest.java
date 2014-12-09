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

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.core.BodyHandler;
import io.vertx.ext.apex.addons.FileUpload;
import io.vertx.ext.apex.test.ApexTestBase;
import io.vertx.test.core.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodiesTest extends ApexTestBase {

  @Rule
  public TemporaryFolder tempUploads = new TemporaryFolder();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(BodyHandler.bodyHandler());
  }

  @Override
  public void tearDown() throws Exception {
    if (vertx.fileSystem().existsBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY)) {
      vertx.fileSystem().deleteRecursiveBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, true);
    }
    super.tearDown();
  }

  @Test
  public void testGETNoBody() throws Exception {
    router.route().handler(rc -> {
      assertNull(rc.getBody());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testHEADNoBody() throws Exception {
    router.route().handler(rc -> {
      assertNull(rc.getBody());
      rc.response().end();
    });
    testRequest(HttpMethod.HEAD, "/", 200, "OK");
  }

  @Test
  public void testBodyBuffer() throws Exception {
    Buffer buff = TestUtils.randomBuffer(1000);
    router.route().handler(rc -> {
      assertEquals(buff, rc.getBody());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(buff);
    }, 200, "OK", null);
  }

  @Test
  public void testBodyString() throws Exception {
    String str = "sausages";
    router.route().handler(rc -> {
      assertEquals(str, rc.getBodyAsString());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(str);
    }, 200, "OK", null);
  }

  @Test
  public void testBodyStringEncoding() throws Exception {
    String str = TestUtils.randomUnicodeString(100);
    String enc = "UTF-16";
    router.route().handler(rc -> {
      assertEquals(str, rc.getBodyAsString(enc));
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(str, enc);
    }, 200, "OK", null);
  }

  @Test
  public void testBodyJson() throws Exception {
    JsonObject json = new JsonObject().put("foo", "bar").put("blah", 123);
    router.route().handler(rc -> {
      assertEquals(json, rc.getBodyAsJson());
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(json.encode());
    }, 200, "OK", null);
  }

  @Test
  public void testBodyTooBig() throws Exception {
    router.clear();
    router.route().handler(BodyHandler.bodyHandler(5000));
    Buffer buff = TestUtils.randomBuffer(10000);
    router.route().handler(rc -> {
      fail("Should not be called");
    });
    testRequest(HttpMethod.POST, "/", req -> {
      req.setChunked(true);
      req.write(buff);
    }, 413, "Request Entity Too Large", null);
  }

  @Test
  public void testFileUploadDefaultUploadsDir() throws Exception {
    testFileUpload(BodyHandler.DEFAULT_UPLOADS_DIRECTORY);
  }

  @Test
  public void testFileUploadOtherUploadsDir() throws Exception {
    router.clear();
    File dir = tempUploads.newFolder();
    router.route().handler(BodyHandler.bodyHandler(dir.getPath()));
    testFileUpload(dir.getPath());
  }

  private void testFileUpload(String uploadsDir) throws Exception {
    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
    Buffer fileData = TestUtils.randomBuffer(50000);
    router.route().handler(rc -> {
      Set<FileUpload> fileUploads = rc.fileUploads();
      assertNotNull(fileUploads);
      assertEquals(1, fileUploads.size());
      FileUpload upload = fileUploads.iterator().next();
      assertEquals(name, upload.name());
      assertEquals(fileName, upload.fileName());
      assertEquals(contentType, upload.contentType());
      assertEquals("binary", upload.contentTransferEncoding());
      // FIXME - Netty seems to be reporting wrong upload size
      // FIXME Add test in core test suite to validated this!
      // assertEquals(fileData.length(), upload.size());
      String uploadedFileName = upload.uploadedFileName();
      assertTrue(uploadedFileName.startsWith(uploadsDir + "/"));
      Buffer uploaded = vertx.fileSystem().readFileBlocking(uploadedFileName);
      assertEquals(fileData, uploaded);
      // The body should be set too
      Buffer rawBody = rc.getBody();
      assertNotNull(rawBody);
      assertTrue(rawBody.length() > fileData.length());
      rc.response().end();
    });
    sendFileUploadRequest(fileData, 200, "OK");
  }

  @Test
  public void testFileUploadTooBig() throws Exception {
    router.clear();
    router.route().handler(BodyHandler.bodyHandler(20000));

    Buffer fileData = TestUtils.randomBuffer(50000);
    router.route().handler(rc -> {
      fail("Should not be called");
    });
    sendFileUploadRequest(fileData, 413, "Request Entity Too Large");
  }

  @Test
  public void testFileUploadTooBig2() throws Exception {
    router.clear();
    router.route().handler(BodyHandler.bodyHandler(20000, BodyHandler.DEFAULT_UPLOADS_DIRECTORY));

    Buffer fileData = TestUtils.randomBuffer(50000);
    router.route().handler(rc -> {
      fail("Should not be called");
    });
    sendFileUploadRequest(fileData, 413, "Request Entity Too Large");
  }

  private void sendFileUploadRequest(Buffer fileData,
                                     int statusCode, String statusMessage) throws Exception {
    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
    testRequest(HttpMethod.POST, "/", req -> {
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String header =
        "--" + boundary + "\r\n" +
          "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
          "Content-Type: " + contentType + "\r\n" +
          "Content-Transfer-Encoding: binary\r\n" +
          "\r\n";
      buffer.appendString(header);
      buffer.appendBuffer(fileData);
      String footer = "\r\n--" + boundary + "--\r\n";
      buffer.appendString(footer);
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.write(buffer);
    }, statusCode, statusMessage, null);
  }

  @Test
  public void testFormURLEncoded() throws Exception {
    router.route().handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(3, attrs.size());
      assertEquals("junit-testUserAlias", attrs.get("origin"));
      assertEquals("admin@foo.bar", attrs.get("login"));
      assertEquals("admin", attrs.get("pass word"));
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      Buffer buffer = Buffer.buffer();
      buffer.appendString("origin=junit-testUserAlias&login=admin%40foo.bar&pass+word=admin");
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "application/x-www-form-urlencoded");
      req.write(buffer);
    }, 200, "OK", null);
  }

  @Test
  public void testFormMultipartFormData() throws Exception {
    router.route().handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(2, attrs.size());
      assertEquals("Tim", attrs.get("attr1"));
      assertEquals("Julien", attrs.get("attr2"));
      rc.response().end();
    });
    testRequest(HttpMethod.POST, "/", req -> {
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr1\"\r\n\r\nTim\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr2\"\r\n\r\nJulien\r\n" +
        "--" + boundary + "--\r\n";
      buffer.appendString(str);
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.write(buffer);
    }, 200, "OK", null);
  }

}
