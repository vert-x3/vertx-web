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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodyHandlerTest extends WebTestBase2 {

  private static final String name = "somename";

  @TempDir
  File tempUploads;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    super.setUp(vertx, testContext);
    router.route().handler(BodyHandler.create());
  }

  @AfterAll
  public static void oneTimeTearDown() throws Exception {
    cleanupFileUploadDir();
  }

  @Test
  public void testGETWithoutBody() {
    router.route().handler(rc -> {
      assertNull(rc.body().buffer());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testHEADWithoutBody() {
    router.route().handler(rc -> {
      assertNull(rc.body().buffer());
      rc.response().end();
    });
    testRequest(HttpMethod.HEAD, "/", 200, "OK");
  }

  @Test
  public void testBodyBuffer() {
    Buffer buff = TestUtils.randomBuffer(1000);
    router.route().handler(rc -> {
      assertEquals(buff, rc.body().buffer());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendBuffer(buff), 200, "OK");
  }

  @Test
  public void testBodyString() {
    String str = "sausages";
    router.route().handler(rc -> {
      assertEquals(str, rc.body().asString());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendBuffer(Buffer.buffer(str)), 200, "OK");
  }

  @Test
  public void testBodyStringWithEncoding() {
    String str = "\u00FF";
    router.route().handler(rc -> {
      assertEquals(1, rc.body().length());
      String decoded = rc.body().asString();
      assertEquals(str, decoded);
      rc.response().end();
    });
    byte b = str.getBytes(StandardCharsets.ISO_8859_1)[0];
    testRequest(webClient.post("/").putHeader("content-type", "text/plain;charset=ISO-8859-1").sendBuffer(Buffer.buffer(new byte[]{b})), 200, "OK");
  }

  @Test
  public void testBodyStringEncoding() {
    String str = TestUtils.randomUnicodeString(100);
    String enc = "UTF-16";
    router.route().handler(rc -> {
      assertEquals(str, rc.body().asString(enc));
      rc.response().end();
    });
    testRequest(webClient.post("/").sendBuffer(Buffer.buffer(str, enc)), 200, "OK");
  }

  @Test
  public void testBodyJson() {
    JsonObject json = new JsonObject().put("foo", "bar").put("blah", 123);
    router.route().handler(rc -> {
      assertEquals(json, rc.body().asJsonObject());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendJsonObject(json), 200, "OK");
  }

  @Test
  public void testBodyJsonWithNegativeContentLength() {
    JsonObject json = new JsonObject().put("foo", "bar").put("blah", 123);
    router.route().handler(rc -> {
      assertEquals(json, rc.body().asJsonObject());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendJsonObject(json), 200, "OK");
  }

  @Test
  public void testBodyJsonWithEmptyContentLength() {
    JsonObject json = new JsonObject().put("foo", "bar").put("blah", 123);
    router.route().handler(rc -> {
      assertEquals(json, rc.body().asJsonObject());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendJsonObject(json), 200, "OK");
  }

  @Test
  public void testBodyJsonWithHugeContentLength() {
    JsonObject json = new JsonObject().put("foo", "bar").put("blah", 123);
    router.route().handler(rc -> {
      assertEquals(json, rc.body().asJsonObject());
      rc.response().end();
    });
    testRequest(webClient.post("/").sendJsonObject(json), 200, "OK");
  }

  @Test
  public void testBodyTooBig() {
    router.clear();
    router.route().handler(BodyHandler.create().setBodyLimit(5000));
    Buffer buff = TestUtils.randomBuffer(10000);
    router.route().handler(rc -> fail("Should not be called"));
    testRequest(webClient.post("/").sendBuffer(buff), 413, "Request Entity Too Large");
  }

  @Test
  public void testBodyTooBig2() {
    router.clear();
    router.route().handler(BodyHandler.create().setBodyLimit(500));
    Buffer buff = TestUtils.randomBuffer(1000);
    router.route().handler(rc -> fail("Should not be called"));
    testRequest(webClient.post("/").sendBuffer(buff), 413, "Request Entity Too Large");
  }

  @Test
  public void testFileUploadSmallUpload() {
    testFileUpload(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, 50);
  }

  @Test
  // This size (7990) has caused issues in the past so testing it
  public void testFileUpload7990Upload() {
    testFileUpload(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, 7990);
  }

  @Test
  public void testFileUploadLargeUpload() {
    testFileUpload(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, 20000);
  }

  @Test
  public void testFileUploadDefaultUploadsDir() {
    testFileUpload(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, 5000);
  }

  @Test
  public void testFileUploadOtherUploadsDir() {
    File dir = new File(tempUploads, "other");
    dir.mkdirs();
    router.clear();
    router.route().handler(BodyHandler.create().setUploadsDirectory(dir.getPath()));
    testFileUpload(dir.getPath(), 5000);
  }

  private void testFileUpload(String uploadsDir, int size) {
    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
    Buffer fileData = TestUtils.randomBuffer(size);
    router.route().handler(rc -> {
      List<FileUpload> fileUploads = rc.fileUploads();
      assertNotNull(fileUploads);
      assertEquals(1, fileUploads.size());
      FileUpload upload = fileUploads.iterator().next();
      assertEquals(name, upload.name());
      assertEquals(fileName, upload.fileName());
      assertEquals(contentType, upload.contentType());
      assertEquals("binary", upload.contentTransferEncoding());
      assertEquals(fileData.length(), upload.size());
      String uploadedFileName = upload.uploadedFileName();
      assertTrue(uploadedFileName.startsWith(uploadsDir + File.separator));
      Buffer uploaded = vertx.fileSystem().readFileBlocking(uploadedFileName);
      assertEquals(fileData, uploaded);
      // the data is upload as HTML form, so the body should be empty
      Buffer rawBody = rc.body().buffer();
      assertNull(rawBody);
      upload.delete().onComplete(TestUtils.onSuccess(v -> {
        assertFalse(vertx.fileSystem().existsBlocking(uploadedFileName));
        rc.response().end();
      }));
    });
    sendFileUploadRequest(fileData, 200, "OK");
  }

  @Test
  public void testFileUploadTooBig() {
    router.clear();
    router.route().handler(BodyHandler.create().setBodyLimit(20000));

    Buffer fileData = TestUtils.randomBuffer(50000);
    router.route().handler(rc -> fail("Should not be called"));
    sendFileUploadRequest(fileData, 413, "Request Entity Too Large");
  }

  @Test
  public void testFileUploadTooBig2() {
    router.clear();
    router.route().handler(BodyHandler.create().setBodyLimit(20000));

    Buffer fileData = TestUtils.randomBuffer(50000);
    router.route().handler(rc -> fail("Should not be called"));
    sendFileUploadRequest(fileData, 413, "Request Entity Too Large");
  }

  @Test
  public void testFileUploadNoFileRemovalOnEnd() {
    testFileUploadFileRemoval(rc -> rc.response().end(), false, 200, "OK");
  }

  @Test
  public void testFileUploadFileRemovalOnEnd() {
    testFileUploadFileRemoval(rc -> rc.response().end(), true, 200, "OK");
  }

  @Test
  public void testFileUploadFileRemovalOnError() {
    testFileUploadFileRemoval(rc -> {
      throw new IllegalStateException();
    }, true, 500, "Internal Server Error");
  }

  @Test
  public void testFileUploadFileRemovalIfAlreadyRemoved() {
    testFileUploadFileRemoval(rc -> {
      vertx.fileSystem().deleteBlocking(rc.fileUploads().iterator().next().uploadedFileName());
      rc.response().end();
    }, true, 200, "OK");
  }

  @Test
  public void testFileDeleteOnLargeUpload() {
    String uploadsDirectory = new File(tempUploads, "largeDel").getPath();
    new File(uploadsDirectory).mkdirs();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setDeleteUploadedFilesOnEnd(true)
      .setBodyLimit(10000)
      .setUploadsDirectory(uploadsDirectory));
    router.route().handler(ctx -> {
      fail();
      ctx.fail(500);
    });

    sendFileUploadRequest(TestUtils.randomBuffer(20000), 413, "Request Entity Too Large");

    assertWaitUntil(() -> vertx.fileSystem().readDirBlocking(uploadsDirectory).isEmpty());
  }

  @Test
  public void testFileUploadFileRemovalOnClientClosesConnection(VertxTestContext testContext) {

    String uploadsDirectory = new File(tempUploads, "clientClose").getPath();
    new File(uploadsDirectory).mkdirs();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDirectory));

    assertEquals(0, vertx.fileSystem().readDirBlocking(uploadsDirectory).size());
    client.request(HttpMethod.POST, "/").onComplete(TestUtils.onSuccess(req -> {
      String name = "somename";
      String fileName = "somefile.dat";
      String contentType = "application/octet-stream";
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String header =
        "--" + boundary + "\r\n" +
          "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
          "Content-Type: " + contentType + "\r\n" +
          "Content-Transfer-Encoding: binary\r\n" +
          "\r\n";
      buffer.appendString(header);
      buffer.appendBuffer(TestUtils.randomBuffer(50));
      req.headers().set("content-length", String.valueOf(buffer.length() + 50)); //partial upload
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.write(buffer);

      //wait for upload beginning
      repeatWhile(100, i -> i < 100 && vertx.fileSystem().readDirBlocking(uploadsDirectory).size() == 0, () -> {
        assertEquals(1, vertx.fileSystem().readDirBlocking(uploadsDirectory).size());
        req.connection().close();
        //wait for upload being deleted
        repeatWhile(100, i -> i < 100 && vertx.fileSystem().readDirBlocking(uploadsDirectory).size() != 0, () -> {
          assertEquals(0, vertx.fileSystem().readDirBlocking(uploadsDirectory).size());
          testContext.completeNow();
        });
      });
    }));
  }

  private <T> void repeatWhile(long time, Function<Integer, Boolean> f, Runnable done) {
    repeatWhile(time, 0, f, done);
  }

  private <T> void repeatWhile(long time, int current, Function<Integer, Boolean> f, Runnable done) {
    if (f.apply(current)) {
      vertx.setTimer(time, id -> {
        repeatWhile(time, current + 1, f, done);
      });
    } else {
      done.run();
    }
  }

  private void testFileUploadFileRemoval(Handler<RoutingContext> requestHandler, boolean deletedUploadedFilesOnEnd,
                                         int statusCode, String statusMessage) {
    String uploadsDirectory = new File(tempUploads, "removal" + System.nanoTime()).getPath();
    new File(uploadsDirectory).mkdirs();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setDeleteUploadedFilesOnEnd(deletedUploadedFilesOnEnd)
      .setUploadsDirectory(uploadsDirectory));
    router.route().handler(requestHandler);

    sendFileUploadRequest(TestUtils.randomBuffer(50), statusCode, statusMessage);

    int uploadedFilesAfterEnd = deletedUploadedFilesOnEnd ? 0 : 1;
    assertWaitUntil(() -> uploadedFilesAfterEnd == vertx.fileSystem().readDirBlocking(uploadsDirectory).size());
  }

  private void sendFileUploadRequest(Buffer fileData,
                                     int statusCode, String statusMessage) {
    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
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
    testRequest(webClient.post("/").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), statusCode, statusMessage);
  }

  @Test
  public void testRoutingContextFailedBeforeFileIsFullyUploaded(VertxTestContext testContext) {
    String uploadsDirectory = new File(tempUploads, "failUpload").getPath();
    new File(uploadsDirectory).mkdirs();
    router.clear();
    AtomicBoolean stop = new AtomicBoolean();

    router.post("/upload")
      .handler((PlatformHandler) rc -> {
        vertx.setPeriodic(5, id -> {
          if (!stop.get()) {
            List<String> files = vertx.fileSystem().readDirBlocking(uploadsDirectory);
            if (!files.isEmpty() && vertx.fileSystem().propsBlocking(files.get(0)).size() > 0) {
              vertx.cancelTimer(id);
              rc.fail(503);
              stop.set(true);
            }
          }
        });
        rc.next();
      })
      .handler(BodyHandler.create().setUploadsDirectory(uploadsDirectory).setDeleteUploadedFilesOnEnd(true))
      .handler(rc -> rc.end("foo"));

    RequestOptions requestOptions = new RequestOptions()
      .setMethod(HttpMethod.POST)
      .setHost("localhost")
      .setPort(8080)
      .setURI("/upload");
    Checkpoint responseLatch = testContext.checkpoint();
    client.request(requestOptions).onComplete(TestUtils.onSuccess(req -> {
      req.response().onComplete(TestUtils.onSuccess(resp -> {
        assertEquals(503, resp.statusCode());
        responseLatch.flag();
      }));
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = TestUtils.randomBuffer(2048);
      req.headers().set(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
      req.headers().set(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
      req.setChunked(true);
      req.write("--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"somename\"; filename=\"somefile.dat\"\r\n" +
        "Content-Type: application/octet-stream\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n");
      req.write(buffer.getBuffer(0, 1024));
      vertx.setPeriodic(50, id -> {
        if (stop.get()) {
          vertx.cancelTimer(id);
          req.write(buffer.getBuffer(0, 1024));
          String footer = "\r\n--" + boundary + "--\r\n";
          req.end(footer);
        }
      });
    }));

    responseLatch.await();

    assertWaitUntil(() -> vertx.fileSystem().readDirBlocking(uploadsDirectory).isEmpty());
  }

  @Test
  public void testFormURLEncoded() {
    router.route().handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(3, attrs.size());
      assertEquals("junit-testUserAlias", attrs.get("origin"));
      assertEquals("admin@foo.bar", attrs.get("login"));
      assertEquals("admin", attrs.get("pass word"));
      rc.response().end();
    });
    Buffer buffer = Buffer.buffer("origin=junit-testUserAlias&login=admin%40foo.bar&pass+word=admin");
    testRequest(webClient.post("/").putHeader("content-type", "application/x-www-form-urlencoded").sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testFormContentTypeIgnoreCase() {
    router.route().handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(1, attrs.size());
      assertEquals("junit-testUserAlias", attrs.get("origin"));
      rc.response().end();
    });
    Buffer buffer = Buffer.buffer("origin=junit-testUserAlias");
    testRequest(webClient.post("/").putHeader("content-type", "ApPlIcAtIoN/x-WwW-fOrM-uRlEnCoDeD").sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testFormMultipartFormDataMergeAttributesDefault() {
    testFormMultipartFormData(true);
  }

  @Test
  public void testFormMultipartFormDataMergeAttributes() {
    router.clear();
    router.route().handler(BodyHandler.create().setMergeFormAttributes(true));
    testFormMultipartFormData(true);
  }

  @Test
  public void testFormMultipartFormDataNoMergeAttributes() {
    router.clear();
    router.route().handler(BodyHandler.create().setMergeFormAttributes(false));
    testFormMultipartFormData(false);
  }

  @Test
  public void testMultiFileUpload() {
    router.clear();
    router.route().handler(BodyHandler.create().setBodyLimit(-1));

    int uploads = 1000;

    router.route().handler(rc -> {
      assertEquals(uploads, rc.fileUploads().size());
      rc.response().end();
    });

    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();

    for (int i = 0; i < uploads; i++) {
      String header =
        "--" + boundary + "\r\n" +
          "Content-Disposition: form-data; name=\"file" + i + "\"; filename=\"file" + i + "\"\r\n" +
          "Content-Type: application/octet-stream\r\n" +
          "Content-Transfer-Encoding: binary\r\n" +
          "\r\n";
      buffer.appendString(header);
      buffer.appendBuffer(TestUtils.randomBuffer(4096 * 16));
      buffer.appendString("\r\n");
    }
    buffer.appendString("--" + boundary + "\r\n");

    testRequest(webClient.post("/").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  private void testFormMultipartFormData(boolean mergeAttributes) {
    router.route().handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(2, attrs.size());
      assertEquals("Tim", attrs.get("attr1"));
      assertEquals("Julien", attrs.get("attr2"));
      MultiMap params = rc.request().params();
      if (mergeAttributes) {
        assertNotNull(params);
        assertEquals(3, params.size());
        assertEquals("Tim", params.get("attr1"));
        assertEquals("Julien", params.get("attr2"));
      } else {
        assertNotNull(params);
        assertEquals(1, params.size());
      }
      assertEquals("foo", params.get("p1"));
      rc.response().end();
    });
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String str =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr1\"\r\n\r\nTim\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr2\"\r\n\r\nJulien\r\n" +
        "--" + boundary + "--\r\n";
    buffer.appendString(str);
    testRequest(webClient.post("/?p1=foo").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testMixedUploadAndForm() {

    String uploadsDirectory = new File(tempUploads, "mixed").getPath();
    new File(uploadsDirectory).mkdirs();

    router.clear();
    router.route().handler(BodyHandler.create()
      .setUploadsDirectory(uploadsDirectory));
    router.route().handler(ctx -> {
      assertNull(ctx.body().buffer());
      assertEquals(1, ctx.fileUploads().size());
      ctx.response().end();
    });

    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(TestUtils.randomBuffer(50));
    String footer = "\r\n--" + boundary + "--\r\n";
    buffer.appendString(footer);
    testRequest(webClient.post("/").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testNoUploadDirMultiPartFormData() {
    String dirName = getNotCreatedTemporaryFolderName();
    router.clear();
    router.route().handler(BodyHandler.create(false).setUploadsDirectory(dirName));

    Buffer fileData = TestUtils.randomBuffer(50);
    router.route().handler(rc -> {
      rc.response().end();
      assertFalse(vertx.fileSystem().existsBlocking(dirName), "Upload directory must not be created.");
    });
    sendFileUploadRequest(fileData, 200, "OK");
  }

  @Test
  public void testFormMultipartFormDataWithAllowedFilesUploadFalse1() {
    testFormMultipartFormDataWithAllowedFilesUploadFalse(true);
  }

  @Test
  public void testFormMultipartFormDataWithAllowedFilesUploadFalse2() {
    testFormMultipartFormDataWithAllowedFilesUploadFalse(false);
  }

  private void testFormMultipartFormDataWithAllowedFilesUploadFalse(boolean mergeAttributes) {
    String fileName = "test.bin";
    router.clear();
    router.route().handler(BodyHandler.create(false).setMergeFormAttributes(mergeAttributes)).handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(2, attrs.size());
      assertEquals("Tim", attrs.get("attr1"));
      assertEquals("Tommaso", attrs.get("attr2"));
      MultiMap params = rc.request().params();
      assertEquals(0, rc.fileUploads().size());
      if (mergeAttributes) {
        assertNotNull(params);
        assertEquals(3, params.size());
        assertEquals("Tim", params.get("attr1"));
        assertEquals("Tommaso", params.get("attr2"));
        assertEquals("foo", params.get("p1"));
      } else {
        assertNotNull(params);
        assertEquals(1, params.size());
        assertEquals("foo", params.get("p1"));
        assertEquals("Tim", rc.request().getFormAttribute("attr1"));
        assertEquals("Tommaso", rc.request().getFormAttribute("attr2"));
      }
      rc.response().end();
    });
    Buffer buffer = Buffer.buffer();
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr1\"\r\n\r\nTim\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr2\"\r\n\r\nTommaso\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
        "Content-Type: application/octet-stream\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(TestUtils.randomBuffer(50));
    buffer.appendString("\r\n--" + boundary + "--\r\n");
    testRequest(webClient.post("/?p1=foo").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testNoUploadDirFormURLEncoded() {
    String dirName = getNotCreatedTemporaryFolderName();
    router.clear();
    router.route().handler(BodyHandler.create(false).setUploadsDirectory(dirName));

    testFormURLEncoded();

    assertFalse(vertx.fileSystem().existsBlocking(dirName), "Upload directory must not be created.");
  }

  @Test
  public void testBodyHandlerCreateTrueWorks() {
    router.clear();
    router.route().handler(BodyHandler.create(true));
    testFormURLEncoded();
  }

  @Test
  public void testSetHandleFileUploads() {
    String dirName = getNotCreatedTemporaryFolderName();
    router.clear();

    BodyHandler bodyHandler = BodyHandler.create().setUploadsDirectory(dirName).setHandleFileUploads(false);
    router.route().handler(bodyHandler);

    Buffer fileData = TestUtils.randomBuffer(50);
    Route route = router.route().handler(rc -> {
      rc.response().end();
      assertFalse(vertx.fileSystem().existsBlocking(dirName), "Upload directory must not be created.");
    });
    sendFileUploadRequest(fileData, 200, "OK");

    route.remove();
    bodyHandler.setHandleFileUploads(true);
    router.route().handler(rc -> {
      rc.response().end();
      assertTrue(vertx.fileSystem().existsBlocking(dirName), "Upload directory must be created.");
    });
    sendFileUploadRequest(fileData, 200, "OK");
  }

  @Test
  public void testRerouteWithHandleFileUploadsFalse() {
    String fileName = "test.bin";
    router.clear();
    router.route().handler(BodyHandler.create(false).setMergeFormAttributes(true));
    router.route("/toBeRerouted").handler(rc -> {
      rc.reroute("/rerouted");
    });
    router.route("/rerouted").handler(rc -> {
      MultiMap attrs = rc.request().formAttributes();
      assertNotNull(attrs);
      assertEquals(2, attrs.size());
      assertEquals("Tim", attrs.get("attr1"));
      assertEquals("Tommaso", attrs.get("attr2"));
      MultiMap params = rc.request().params();
      assertEquals(0, rc.fileUploads().size());
      assertNotNull(params);
      assertEquals(2, params.size());
      assertEquals("Tim", params.get("attr1"));
      assertEquals("Tommaso", params.get("attr2"));
      rc.response().end();
    });
    Buffer buffer = Buffer.buffer();
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr1\"\r\n\r\nTim\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr2\"\r\n\r\nTommaso\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
        "Content-Type: application/octet-stream\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(TestUtils.randomBuffer(50));
    buffer.appendString("\r\n--" + boundary + "--\r\n");
    testRequest(webClient.post("/toBeRerouted").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testBodyLimitWithHandleFileUploadsFalse() {
    router.clear();

    BodyHandler bodyHandler = BodyHandler.create(false).setBodyLimit(2048);
    router.route().handler(bodyHandler);

    Buffer fileData = TestUtils.randomBuffer(4096);
    router.route().handler(rc -> {
      rc.response().end();
    });
    sendFileUploadRequest(fileData, 413, "Request Entity Too Large");
  }

  private String getNotCreatedTemporaryFolderName() {
    File dir = new File(tempUploads, "notCreated" + System.nanoTime());
    return dir.getPath();
  }

  @Test
  public void testFomWithoutParamNameRequestForm() {
    router.clear();
    router.route().handler(BodyHandler.create());
    Buffer buffer = Buffer.buffer("a=b&=&c=d");
    router.route().handler(rc -> fail("Should not be called"));
    testRequest(webClient.post("/").putHeader("content-type", "application/x-www-form-urlencoded").sendBuffer(buffer), 400, "Bad Request");
  }

  @Test
  public void testFomWithoutParamRequestForm() {
    router.clear();
    router.route().handler(BodyHandler.create());
    Buffer buffer = Buffer.buffer("a=b&&c=d");
    router.route().handler(RoutingContext::end);
    testRequest(webClient.post("/").putHeader("content-type", "application/x-www-form-urlencoded").sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testJsonLimit() {
    router.clear();
    router.route().handler(BodyHandler.create());
    Buffer buffer = Buffer.buffer("000000000000000000000000000000000000000000000000");
    router.route().handler(rc -> {
      try {
        rc.body().asJsonObject(10);
        // should not reach here!
        rc.fail(500);
      } catch (IllegalStateException e) {
        rc.fail(413);
      }
    });
    testRequest(webClient.post("/").putHeader("content-type", "application/json").sendBuffer(buffer), 413, "Request Entity Too Large");
  }

  @Test
  public void testJsonLimitOK() {
    router.clear();
    router.route().handler(BodyHandler.create());
    Buffer buffer = Buffer.buffer("{\"k\":1111}");
    router.route().handler(rc -> {
      try {
        rc.body().asJsonObject(10);
        rc.end();
      } catch (IllegalStateException e) {
        // should not reach here!
        rc.fail(500);
      }
    });
    testRequest(webClient.post("/").putHeader("content-type", "application/json").sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testFileUploadUTF8() {
    String name = "somename";
    String fileName = "somefile.dat";
    String contentType = "application/octet-stream";
    Buffer fileData = TestUtils.randomBuffer(50);
    router.route().handler(rc -> {
      List<FileUpload> fileUploads = rc.fileUploads();
      assertNotNull(fileUploads);
      assertEquals(1, fileUploads.size());
      FileUpload upload = fileUploads.iterator().next();
      assertEquals(name, upload.name());
      // tests https://tools.ietf.org/html/rfc5987
      assertEquals("\u00A3 and \u20AC " + fileName, upload.fileName());
      assertEquals(contentType, upload.contentType());
      assertEquals("binary", upload.contentTransferEncoding());
      assertEquals(fileData.length(), upload.size());
      String uploadedFileName = upload.uploadedFileName();
      assertTrue(uploadedFileName.startsWith(BodyHandler.DEFAULT_UPLOADS_DIRECTORY + File.separator));
      Buffer uploaded = vertx.fileSystem().readFileBlocking(uploadedFileName);
      assertEquals(fileData, uploaded);
      // the data is upload as HTML form, so the body should be empty
      Buffer rawBody = rc.body().buffer();
      assertNull(rawBody);
      rc.response().end();
    });

    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename*=\"UTF-8''%c2%a3%20and%20%e2%82%ac%20" + fileName + "\"\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(fileData);
    String footer = "\r\n--" + boundary + "--\r\n";
    buffer.appendString(footer);
    testRequest(webClient.post("/").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");
  }

  @Test
  public void testFormMultipartFormDataLarge() {
    router.clear();
    router.route().handler(BodyHandler.create());
    router.route().handler(rc -> {
      fail("Should not get here");
    }).failureHandler(ctx -> {
      assertNotNull(ctx.failure());
      assertTrue(ctx.failure() instanceof IOException);
      assertEquals("Size exceed allowed maximum capacity", ctx.failure().getMessage());
      ctx.next();
    });

    Buffer buffer = Buffer.buffer();
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr1\"\r\n\r\n" + Base64.getUrlEncoder().encodeToString(TestUtils.randomBuffer(8192).getBytes()) + "\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"attr2\"\r\n\r\n" + Base64.getUrlEncoder().encodeToString(TestUtils.randomBuffer(8192).getBytes()) + "\r\n" +
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"file\"\r\n" +
        "Content-Type: application/octet-stream\r\n" +
        "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(TestUtils.randomBuffer(50));
    buffer.appendString("\r\n--" + boundary + "--\r\n");
    testRequest(webClient.post("/?p1=foo").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 400, "Bad Request");
  }

  @Test
  public void testLogExceptions() {
    router.clear();
    router.route().handler(BodyHandler.create());

    router.route().handler(ctx -> {
      throw new NullPointerException();
    });
    testRequest(webClient.get("/").send(), 500, "Internal Server Error");
  }

  @Test
  public void testFileUploadSize() {
    String uploadsDirectory = new File(tempUploads, "uploadSize").getPath();
    new File(uploadsDirectory).mkdirs();
    router.clear();
    router.route().handler(BodyHandler.create()
      .setDeleteUploadedFilesOnEnd(true)
      .setUploadsDirectory(uploadsDirectory));

    int realSize = 20000;

    router.route().handler(ctx -> {
      String specData = ctx.request().formAttributes().get("specData");
      System.out.println(specData);
      FileUpload file = ctx.fileUploads().iterator().next();
      long uploadSize = file.size();
      assertEquals(realSize, uploadSize);
      ctx.end();
    });

    String name = "file";
    String fileName = "/C:/Users/vishal.b05/Desktop/1p.png";
    String contentType = "application/octet-stream";
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String header =
      "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n" +
        "Content-Type: " + contentType + "\r\n" +
//          "Content-Transfer-Encoding: binary\r\n" +
        "\r\n";
    buffer.appendString(header);
    buffer.appendBuffer(TestUtils.randomBuffer(realSize));
    String footer = "\r\n--" + boundary + "\r\n";
    buffer.appendString(footer);

    String extra =
        "Content-Disposition: form-data; name=\"specData\"\r\n\r\n{\"id\":\"abc@xyz.com\"}\r\n" +
        "--" + boundary + "--\r\n\r\n";

    buffer.appendString(extra);

    testRequest(webClient.post("/").putHeader("content-type", "multipart/form-data; boundary=" + boundary).sendBuffer(buffer), 200, "OK");

    assertWaitUntil(() -> vertx.fileSystem().readDirBlocking(uploadsDirectory).isEmpty());
  }


  @Test
  public void testMaxFormFieldsLimit() throws InterruptedException {
    router.clear();
    router.route().handler(BodyHandler.create());
    router.route().handler(ctx -> {
      fail();
    });

    int len = 1025;

    Integer sc = client.request(HttpMethod.POST, 8080, "localhost", "/")
      .compose(request -> {
        request.setChunked(true);
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.write("a".repeat(len));
        vertx.setTimer(10, id -> {
          request.end("=b");
        });
        return request.response().map(response -> response.statusCode());
      }).await();

    assertEquals(400, sc);
  }
}
