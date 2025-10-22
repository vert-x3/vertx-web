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

package io.vertx.ext.web.handler;

import io.netty.util.internal.PlatformDependent;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.impl.Utils;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static io.vertx.core.http.HttpHeaders.ACCEPT_ENCODING;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandlerTest extends WebTestBase {

  private static final Path webRootSrc;

  static {
    URL webRootUrl = StaticHandlerTest.class.getClassLoader().getResource("webroot");
    if (webRootUrl == null) {
      throw new AssertionError("webRootUrl is null");
    }
    try {
      webRootSrc = Paths.get(webRootUrl.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private Path webRootTarget;

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    webRootTarget = Files.createTempDirectory(webRootSrc.getParent(), "webroot");
    copyWebRootFiles();
    stat = StaticHandler.create(webRootTarget.getFileName().toString());
    router.route().handler(stat);
  }

  private void copyWebRootFiles() throws IOException {
    Files.walkFileTree(webRootSrc, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path resolved = webRootTarget.resolve(webRootSrc.relativize(dir));
        if (Files.notExists(resolved)) {
          Files.createDirectories(resolved);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path resolved = webRootTarget.resolve(webRootSrc.relativize(file));
        Files.copy(file, resolved, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    deleteWebRootFiles();
  }

  private void deleteWebRootFiles() throws IOException {
    Files.walkFileTree(webRootTarget, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Index page</body></html>");
  }

  @Test
  public void testGetSubdirectoryWithoutSlashDefaultIndex() throws Exception {
    // in the case the file is a directory, it redirects to the root.
    testRequest(HttpMethod.GET, "/somedir", null, res -> {
      String location = res.headers().get("location");
      assertEquals("/somedir/", location);
    }, 301, "Moved Permanently", null);
  }

  @Test
  public void testGetSubdirectorySlashDefaultIndex() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory index page</body></html>");
  }

  @Test
  public void testGetOtherIndex() throws Exception {
    stat.setIndexPage("otherpage.html");
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testGetSubdirectoryOtherIndex() throws Exception {
    stat.setIndexPage("otherpage.html");
    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory other page</body></html>");
  }

  @Test
  public void testGetSubdirectorySlashOtherIndex() throws Exception {
    stat.setIndexPage("otherpage.html");
    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory other page</body></html>");
  }

  @Test
  public void testGetFileWithSpaces() throws Exception {
    testRequest(HttpMethod.GET, "/file%20with%20spaces.html", 200, "OK", "<html><body>File with spaces</body></html>");
  }

  @Test
  public void testGetOtherPage() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testGetPageFromSubdir() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/something.html", 200, "OK", "<html><body>Blah page</body></html>");
  }

  @Test
  public void testBadPathNoLeadingSlash() throws Exception {
    testRequest(HttpMethod.GET, "otherpage.html", 404, "Not Found");
  }

  @Test
  public void testGetHiddenPage() throws Exception {
    testRequest(HttpMethod.GET, "/.hidden.html", 200, "OK", "<html><body>Hidden page</body></html>");
  }

  @Test
  public void testCantGetHiddenPage() throws Exception {
    stat.setIncludeHidden(false);
    testRequest(HttpMethod.GET, "/.hidden.html", 404, "Not Found");
  }

  @Test
  public void testGetHiddenPageSubdir() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/.hidden.html", 200, "OK", "<html><body>Hidden page</body></html>");
    testRequest(HttpMethod.GET, "/somedir/.hidden/otherpage.html", 200, "OK", "<html><body>Subdirectory other page</body></html>");
  }

  @Test
  public void testCantGetHiddenPageSubdir() throws Exception {
    stat.setIncludeHidden(false);
    testRequest(HttpMethod.GET, "/somedir/.hidden.html", 404, "Not Found");
    testRequest(HttpMethod.GET, "/somedir/.hidden/otherpage.html", 404, "Not Found");
  }

  @Test
  public void testCantGetNoSuchPage() throws Exception {
    testRequest(HttpMethod.GET, "/notexists.html", 404, "Not Found");
  }

  @Test
  public void testCantGetNoSuchPageInSubDir() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/notexists.html", 404, "Not Found");
  }

  @Test
  public void testDateHeaderSet() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String dateHeader = res.headers().get("date");
      assertNotNull(dateHeader);
      long diff = System.currentTimeMillis() - toDateTime(dateHeader);
      assertTrue(diff > 0 && diff < 2000);
    }, 200, "OK", null);
  }

  @Test
  public void testContentHeadersSet() throws Exception {
    stat.setDefaultContentEncoding("UTF-8");
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String contentType = res.headers().get("content-type");
      String contentLength = res.headers().get("content-length");
      assertEquals("text/html;charset=UTF-8", contentType);
      assertEquals(fileSize("src/test/resources/webroot/otherpage.html"), Integer.valueOf(contentLength).intValue());
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/foo.json", null, res -> {
      String contentType = res.headers().get("content-type");
      String contentLength = res.headers().get("content-length");
      assertEquals("application/json", contentType);
      assertEquals(fileSize("src/test/resources/webroot/foo.json"), Integer.valueOf(contentLength).intValue());
    }, 200, "OK", null);
  }

  @Test
  public void testNoLinkPreload() throws Exception {
    stat.setWebRoot("webroot/somedir3");
    testRequest(HttpMethod.GET, "/testLinkPreload.html", null, res -> {
      List<String> linkHeaders = res.headers().getAll("Link");
      assertTrue(linkHeaders.isEmpty());
    }, 200, "OK", null);
  }

  @Test
  public void testLinkPreload() throws Exception {
    List<Http2PushMapping> mappings = new ArrayList<>();
    mappings.add(new Http2PushMapping("style.css", "style", false));
    mappings.add(new Http2PushMapping("coin.png", "image", false));
    stat.setHttp2PushMapping(mappings)
      .setWebRoot("webroot/somedir3");
    testRequest(HttpMethod.GET, "/testLinkPreload.html", null, res -> {
      List<String> linkHeaders = res.headers().getAll("Link");
      assertTrue(linkHeaders.contains("<style.css>; rel=preload; as=style"));
      assertTrue(linkHeaders.contains("<coin.png>; rel=preload; as=image"));
    }, 200, "OK", null);
  }

  @Test
  public void testNoHttp2Push() throws Exception {
    stat.setWebRoot("webroot/somedir3");
    router.route().handler(stat);

    vertx.createHttpServer(new HttpServerOptions()
        .setUseAlpn(true)
        .setSsl(true)
        .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")))
      .requestHandler(router).listen(8443)
      .onFailure(this::fail)
      .onSuccess(server -> {
        HttpClientOptions options = new HttpClientOptions()
          .setSsl(true)
          .setUseAlpn(true)
          .setProtocolVersion(HttpVersion.HTTP_2)
          .setPemTrustOptions(new PemTrustOptions().addCertPath("tls/server-cert.pem"));
        HttpClient client = vertx.createHttpClient(options);
        client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
          .onComplete(onSuccess(req -> {
            req.pushHandler(pushedReq -> pushedReq.response(pushedResp -> {
              fail();
            }));
            req.send(onSuccess(resp -> {
              assertEquals(200, resp.statusCode());
              assertEquals(HttpVersion.HTTP_2, resp.version());
              resp.bodyHandler(this::assertNotNull);
              testComplete();
            }));
          }));
      });

    await();
  }

  @Test
  public void testHttp2Push() throws Exception {
    List<Http2PushMapping> mappings = new ArrayList<>();
    mappings.add(new Http2PushMapping("style.css", "style", false));
    mappings.add(new Http2PushMapping("coin.png", "image", false));
    stat.setHttp2PushMapping(mappings)
      .setWebRoot("webroot/somedir3");
    router.route().handler(stat);

    CountDownLatch latch = new CountDownLatch(2);

    vertx.createHttpServer(new HttpServerOptions()
        .setUseAlpn(true)
        .setSsl(true)
        .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")))
      .requestHandler(router).listen(8443)
      .onFailure(this::fail)
      .onSuccess(server -> {
        HttpClientOptions options = new HttpClientOptions()
          .setSsl(true)
          .setUseAlpn(true)
          .setProtocolVersion(HttpVersion.HTTP_2)
          .setPemTrustOptions(new PemTrustOptions().addCertPath("tls/server-cert.pem"));
        HttpClient client = vertx.createHttpClient(options);
        client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
          .onComplete(onSuccess(req -> {
            req.pushHandler(pushedReq -> pushedReq.response(onSuccess(pushedResp -> {
                assertNotNull(pushedResp);
                pushedResp.bodyHandler(this::assertNotNull);
                latch.countDown();
              })))
              .send(onSuccess(resp -> {
                assertEquals(200, resp.statusCode());
                assertEquals(HttpVersion.HTTP_2, resp.version());
                resp.bodyHandler(this::assertNotNull);
              }));
          }));
      });

    latch.await();
  }

  @Test
  public void testSkipCompressionForMediaTypes() throws Exception {
    StaticHandler staticHandler = StaticHandler.create()
      .skipCompressionForMediaTypes(Collections.singleton("image/jpeg"));

    List<String> uris = Arrays.asList("/testCompressionSuffix.html", "/somedir/range.jpg", "/somedir/range.jpeg", "/somedir3/coin.png");
    List<String> expectedContentEncodings = Arrays.asList("gzip", null, null, "gzip");
    testSkipCompression(staticHandler, uris, expectedContentEncodings);
  }

  @Test
  public void testSkipCompressionForSuffixes() throws Exception {
    StaticHandler staticHandler = StaticHandler.create()
      .skipCompressionForSuffixes(Collections.singleton("jpg"));

    List<String> uris = Arrays.asList("/testCompressionSuffix.html", "/somedir/range.jpg", "/somedir/range.jpeg", "/somedir3/coin.png");
    List<String> expectedContentEncodings = Arrays.asList("gzip", null, "gzip", "gzip");
    testSkipCompression(staticHandler, uris, expectedContentEncodings);
  }

  private void testSkipCompression(StaticHandler staticHandler, List<String> uris, List<String> expectedContentEncodings) throws Exception {
    server.close();
    server = vertx.createHttpServer(getHttpServerOptions().setPort(0).setCompressionSupported(true));
    router = Router.router(vertx);
    router.route().handler(staticHandler);

    CountDownLatch serverReady = new CountDownLatch(1);
    server.requestHandler(router).listen(onSuccess(s -> serverReady.countDown()));
    awaitLatch(serverReady);

    CompositeFuture cf = uris.stream().map(uri -> client.request(HttpMethod.GET, server.actualPort(), getHttpClientOptions().getDefaultHost(), uri)
        .compose(req -> {
          return req
            .putHeader(ACCEPT_ENCODING, String.join(", ", "gzip", "jpg", "jpeg", "png"))
            .send()
            .compose(resp -> {
              if (resp.statusCode() != 200)
                return Future.failedFuture("Request failed with status: " + resp.statusCode());
              return resp.end().map(resp.getHeader(HttpHeaders.CONTENT_ENCODING));
            });
        }))
      .collect(collectingAndThen(toList(), Future::all));
    cf.onComplete(onSuccess(v -> testComplete()));
    await();
    assertEquals(expectedContentEncodings, cf.list());
  }

  @Test
  public void testHead() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    testRequest(HttpMethod.HEAD, "/otherpage.html", null, res -> {
      res.bodyHandler(buff -> assertEquals(0, buff.length()));
      res.endHandler(v -> latch.countDown());
    }, 200, "OK", null);
    awaitLatch(latch);
  }

  @Test
  public void testCacheReturnFromCache() throws Exception {
    testCacheReturnFromCache((lastModified, req) -> req.putHeader("if-modified-since", lastModified), 304, "Not Modified", null);
  }

  @Test
  public void testCacheGetNew() throws Exception {
    testCacheReturnFromCache((lastModified, req) -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(toDateTime(lastModified) - 1)), 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testCacheReturnFromCacheWhenNoHeader() throws Exception {
    testCacheReturnFromCache((lastModified, req) -> { /* Do nothing */ }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testCacheReturnFromCacheWhenInvalidHeader() throws Exception {
    testCacheReturnFromCache((lastModified, req) -> req.putHeader("if-modified-since", "whatever"), 200, "OK", "<html><body>Other page</body></html>");
  }

  private void testCacheReturnFromCache(BiConsumer<String, HttpClientRequest> handler, int expectedStatusCode, String expectedStatusMessage, String expectedStatusBody) throws Exception {
    AtomicReference<String> lastModifiedRef = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      lastModifiedRef.set(lastModified);
      assertNotNull(cacheControl);
      assertNotNull(lastModified);
      assertEquals("public, immutable, max-age=" + StaticHandler.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    }, 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", req -> handler.accept(lastModifiedRef.get(), req), null, expectedStatusCode, expectedStatusMessage, expectedStatusBody);
  }

  @Test
  public void testCacheIndexPageReturnFromCache() throws Exception {
    AtomicReference<String> lastModifiedRef = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/somedir/", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      lastModifiedRef.set(lastModified);
      assertNotNull(cacheControl);
      assertNotNull(lastModified);
      assertEquals("public, immutable, max-age=" + StaticHandler.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    }, 200, "OK", "<html><body>Subdirectory index page</body></html>");
    testRequest(HttpMethod.GET, "/somedir/", req -> req.putHeader("if-modified-since", lastModifiedRef.get()), null, 304, "Not Modified", null);
  }

  @Test
  public void testCachingDisabled() throws Exception {
    stat.setCachingEnabled(false);
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      assertNull(cacheControl);
      assertNull(lastModified);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testCacheNoCacheAsNoIfModifiedSinceHeader() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testCacheNotOverwritingCacheControlHeaderValues() throws Exception {
    router.clear();
    router.route().order(0).handler(context -> {
      context.response().putHeader("cache-control", "test1");
      context.response().putHeader("last-modified", "test2");
      context.response().putHeader("vary", "test3");

      context.next();
    });
    router.route().order(2).handler(stat);

    testRequest(HttpMethod.GET, "/otherpage.html", req -> req.putHeader("accept-encoding", "gzip"), res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      String vary = res.headers().get("vary");
      assertEquals("test1", cacheControl);
      assertEquals("test2", lastModified);
      assertEquals("test3", vary);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testSendVaryAcceptEncodingHeader() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", req -> req.putHeader("accept-encoding", "gzip"), res -> {
      String vary = res.headers().get("vary");
      assertNotNull(vary);
      assertEquals("accept-encoding", vary);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testNoSendingOfVaryAcceptEncodingHeader() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String vary = res.headers().get("vary");
      assertNull(vary);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testSetMaxAge() throws Exception {
    long maxAge = 60 * 60;
    stat.setMaxAgeSeconds(maxAge);
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      assertEquals("public, immutable, max-age=" + maxAge, cacheControl);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testGetOtherPageTwice() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystem() throws Exception {
    stat.setWebRoot("src/test/filesystemwebroot");
    testRequest(HttpMethod.GET, "/fspage.html", 200, "OK", "<html><body>File system page</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystemWithSpaces() throws Exception {
    stat.setWebRoot("src/test/filesystemwebroot");
    testRequest(HttpMethod.GET, "/file%20with%20spaces2.html", 200, "OK", "<html><body>File with spaces</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystemWithSpacesMountOnWildcard() throws Exception {
    stat = StaticHandler.create("src/test/filesystemwebroot");
    router.clear();
    router.route("/*").handler(stat);
    testRequest(HttpMethod.GET, "/file%20with%20spaces2.html", 200, "OK", "<html><body>File with spaces</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystemWithSpacesMountOnWildcardPlus() throws Exception {
    stat = StaticHandler.create("src/test/filesystemwebroot");
    router.clear();
    router.route("/*").handler(stat);
    testRequest(HttpMethod.GET, "/file+with+spaces2.html", 404, "Not Found");
  }

  @Test
  public void testServeFilesFromFilesystemWebRootConstructor() throws Exception {
    stat = StaticHandler.create("src/test/filesystemwebroot");
    router.clear();
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/fspage.html", 200, "OK", "<html><body>File system page</body></html>");
  }

  @Test
  public void testCacheFilesNotReadOnly() throws Exception {
    stat.setFilesReadOnly(false);
    stat.setWebRoot("src/test/filesystemwebroot");
    long modified = Utils.secondsFactor(new File("src/test/filesystemwebroot", "fspage.html").lastModified());
    testRequest(HttpMethod.GET, "/fspage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
    }, 200, "OK", "<html><body>File system page</body></html>");
    testRequest(HttpMethod.GET, "/fspage.html", req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 304, "Not Modified", null);
  }

  @Test
  public void testCacheFilesEntryCached() throws Exception {
    stat.setFilesReadOnly(false);
    stat.setWebRoot("src/test/filesystemwebroot");
    File resource = new File("src/test/filesystemwebroot", "fspage.html");
    long modified = resource.lastModified();
    testRequest(HttpMethod.GET, "/fspage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
      // Now update the web resource
      resource.setLastModified(modified + 1000);
    }, 200, "OK", "<html><body>File system page</body></html>");
    // But it should still return not modified as the entry is cached
    testRequest(HttpMethod.GET, "/fspage.html", req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 304, "Not Modified", null);
  }

  @Test
  public void testCacheFilesEntryOld() throws Exception {
    String webroot = "src/test/filesystemwebroot", page = "/fspage.html";
    File resource = new File(webroot + page);
    String html = new String(Files.readAllBytes(resource.toPath()));
    int cacheEntryTimeout = 100;

    stat.setFilesReadOnly(false);
    stat.setWebRoot(webroot);
    stat.setCacheEntryTimeout(cacheEntryTimeout);

    long modified = Utils.secondsFactor(resource.lastModified());
    testRequest(HttpMethod.GET, page, null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
      // Now update the web resource
      resource.setLastModified(modified + 1000);
    }, 200, "OK", html);
    // But it should return a new entry as the entry is now old
    Thread.sleep(cacheEntryTimeout + 1);
    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified + 1000, toDateTime(lastModified));
    }, 200, "OK", html);

    // 304 must still work when cacheEntry.isOutOfDate() == true, https://github.com/vert-x3/vertx-web/issues/726
    Thread.sleep(cacheEntryTimeout + 1);

    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified + 1000)), 304, "Not Modified", null);
  }

  @Test
  public void testCacheFilesFileDeleted() throws Exception {
    File webroot = new File("target/.vertx/webroot"), pageFile = new File(webroot, "deleted.html");
    if (!pageFile.exists()) {
      webroot.mkdirs();
      pageFile.createNewFile();
    }
    String page = '/' + pageFile.getName();

    stat.setFilesReadOnly(false);
    stat.setWebRoot(webroot.getPath());
    stat.setCacheEntryTimeout(3600 * 1000);

    long modified = Utils.secondsFactor(pageFile.lastModified());
    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 304, "Not Modified", null);
    pageFile.delete();
    testRequest(HttpMethod.GET, page, 404, "Not Found");
    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 404, "Not Found", null);

  }

  @Test
  public void testDirectoryListingText() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "a", "foo.json", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    testRequest(HttpMethod.GET, "/", null, resp -> {
      resp.bodyHandler(buff -> {
        String sBuff = buff.toString();
        String[] elems = sBuff.split("\n");
        assertEquals(expected.size(), elems.length);
        for (String elem : elems) {
          assertTrue(expected.contains(elem));
        }
      });
    }, 200, "OK", null);
  }

  @Test
  public void testDirectoryListingTextNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "a", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    testRequest(HttpMethod.GET, "/", null, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("text/plain", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        String[] elems = sBuff.split("\n");
        assertEquals(expected.size(), elems.length);
        for (String elem : elems) {
          assertTrue(expected.contains(elem));
        }
      });
    }, 200, "OK", null);
  }

  @Test
  public void testDirectoryListingJson() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "foo.json", "index.html", "otherpage.html", "a", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("accept", "application/json");
    }, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("application/json", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        JsonArray arr = new JsonArray(sBuff);
        assertEquals(expected.size(), arr.size());
        for (Object elem : arr) {
          assertTrue(expected.contains(elem));
        }
        testComplete();
      });
    }, 200, "OK", null);
    await();
  }

  @Test
  public void testDirectoryListingJsonNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "a", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("accept", "application/json");
    }, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("application/json", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        JsonArray arr = new JsonArray(sBuff);
        assertEquals(expected.size(), arr.size());
        for (Object elem : arr) {
          assertTrue(expected.contains(elem));
        }
        testComplete();
      });
    }, 200, "OK", null);
    await();
  }

  @Test
  public void testDirectoryListingHtml() throws Exception {
    stat.setDirectoryListing(true);

    testDirectoryListingHtmlCustomTemplate("META-INF/vertx/web/vertx-web-directory.html", "/somedir2/", "<a href=\"/\">..</a>", "<ul id=\"files\"><li><a href=\"/somedir2/foo2.json\" title=\"foo2.json\">foo2.json</a></li>" +
      "<li><a href=\"/somedir2/somepage.html\" title=\"somepage.html\">somepage.html</a></li>" +
      "<li><a href=\"/somedir2/somepage2.html\" title=\"somepage2.html\">somepage2.html</a></li></ul>");
  }

  @Test
  public void testCustomDirectoryListingHtml() throws Exception {
    stat.setDirectoryListing(true);
    String dirTemplate = "custom_dir_template.html";
    stat.setDirectoryTemplate(dirTemplate);

    testDirectoryListingHtmlCustomTemplate(dirTemplate, "/somedir2/", "<a href=\"/\">..</a>", "<ul id=\"files\"><li><a href=\"/somedir2/foo2.json\" title=\"foo2.json\">foo2.json</a></li>" +
      "<li><a href=\"/somedir2/somepage.html\" title=\"somepage.html\">somepage.html</a></li>" +
      "<li><a href=\"/somedir2/somepage2.html\" title=\"somepage2.html\">somepage2.html</a></li></ul>");
  }

  @Test
  public void testCustomDirectoryListingHtmlEscaping() throws Exception {
    Assume.assumeFalse(PlatformDependent.isWindows());

    Path testDir = webRootTarget.resolve("dirxss");
    Files.createDirectories(testDir);
    Path dangerousFile = testDir.resolve("<img src=x onerror=alert('XSS-FILE')>.txt");
    Files.deleteIfExists(dangerousFile);
    Files.createFile(dangerousFile);

    stat.setDirectoryListing(true);

    testDirectoryListingHtmlCustomTemplate(
      "META-INF/vertx/web/vertx-web-directory.html",
      "/dirxss/",
      "<a href=\"/\">..</a>",
      "<ul id=\"files\"><li><a href=\"/dirxss/%3Cimg%20src=x%20onerror=alert('XSS-FILE')%3E.txt\" title=\"&#60;img src=x onerror=alert(&#39;XSS-FILE&#39;)&#62;.txt\">&#60;img src=x onerror=alert(&#39;XSS-FILE&#39;)&#62;.txt</a></li></ul>");
  }

  private void testDirectoryListingHtmlCustomTemplate(String dirTemplateFile, String path, String parentLink, String files) throws Exception {
    stat.setDirectoryListing(true);


    String directoryTemplate = vertx.fileSystem().readFileBlocking(dirTemplateFile).toString();

    String expected = directoryTemplate.replace("{directory}", path).replace("{parent}", parentLink).replace("{files}", files);

    testRequest(HttpMethod.GET, path, req -> req.putHeader("accept", "text/html"), resp -> resp.bodyHandler(buff -> {
      assertEquals("text/html", resp.headers().get("content-type"));
      String sBuff = buff.toString();
      assertEquals(expected, sBuff);
      testComplete();
    }), 200, "OK", null);
    await();
  }

  @Test
  public void testFSBlockingTuning() throws Exception {
    stat.setCachingEnabled(false);
    stat.setMaxAvgServeTimeNs(10000);
    for (int i = 0; i < 2000; i++) {
      testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
        String cacheControl = res.headers().get("cache-control");
        String lastModified = res.headers().get("last-modified");
        assertNull(cacheControl);
        assertNull(lastModified);
      }, 200, "OK", "<html><body>Other page</body></html>");
    }
  }

  @Test
  public void testServerRelativeToPath() throws Exception {
    router.clear();
    router.route("/somedir/*").handler(stat);
    testRequest(HttpMethod.GET, "/somedir/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testServerRelativeToPathAndMountPoint() throws Exception {
    router.clear();
    Router subRouter = Router.router(vertx);
    subRouter.route("/somedir/*").handler(stat);
    router.route("/mymount/*").subRouter(subRouter);
    testRequest(HttpMethod.GET, "/mymount/somedir/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testRangeAwareRequestHeaders() throws Exception {
    stat.setEnableRangeSupport(true);
    // this is a 3 step test
    // 1. request a head to a static image, this should tell us the server supports ranges
    // 2. make a request of the 1st 1000 bytes
    // 3. request all bytes after 1000
    // 4. request bytes from 1000 up to 5000000 if available (which isn't)

    testRequest(HttpMethod.HEAD, "/somedir/range.jpg", null, res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("15783", res.headers().get("Content-Length"));
    }, 200, "OK", null);

    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=0-999"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1000", res.headers().get("Content-Length"));
      assertEquals("bytes 0-999/15783", res.headers().get("Content-Range"));
    }, 206, "Partial Content", null);

    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=1000-"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("14783", res.headers().get("Content-Length"));
      assertEquals("bytes 1000-15782/15783", res.headers().get("Content-Range"));
    }, 206, "Partial Content", null);
    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=1000-5000000"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("14783", res.headers().get("Content-Length"));
      assertEquals("bytes 1000-15782/15783", res.headers().get("Content-Range"));
    }, 206, "Partial Content", null);
  }

  @Test
  public void testRangeAwareRequestBody() throws Exception {
    stat.setEnableRangeSupport(true);
    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=0-999"), res -> res.bodyHandler(buff -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1000", res.headers().get("Content-Length"));
      assertEquals("bytes 0-999/15783", res.headers().get("Content-Range"));

      assertEquals(1000, buff.length());
      testComplete();
    }), 206, "Partial Content", null);
    await();
  }

  @Test
  public void testRangeAwareRequestSegment() throws Exception {
    stat.setEnableRangeSupport(true);
    testRequest(HttpMethod.GET, "/somedir/range.bin", req -> req.headers().set("Range", "bytes=0-1023"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1024", res.headers().get("Content-Length"));

      res.bodyHandler(body -> {
        assertEquals(1024, body.length());
      });
    }, 206, "Partial Content", null);

    testRequest(HttpMethod.GET, "/somedir/range.bin", req -> req.headers().set("Range", "bytes=1024-2047"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1024", res.headers().get("Content-Length"));

      res.bodyHandler(body -> {
        assertEquals(1024, body.length());
      });
    }, 206, "Partial Content", null);

    testRequest(HttpMethod.GET, "/somedir/range.bin", req -> req.headers().set("Range", "bytes=2048-3071"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1024", res.headers().get("Content-Length"));

      res.bodyHandler(body -> {
        assertEquals(1024, body.length());
      });
    }, 206, "Partial Content", null);

    testRequest(HttpMethod.GET, "/somedir/range.bin", req -> req.headers().set("Range", "bytes=3072-4095"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1024", res.headers().get("Content-Length"));

      res.bodyHandler(body -> {
        assertEquals(1024, body.length());
      });
    }, 206, "Partial Content", null);

    testRequest(HttpMethod.GET, "/somedir/range.bin", req -> req.headers().set("Range", "bytes=4096-5119"), res -> {
      assertEquals("bytes", res.headers().get("Accept-Ranges"));
      assertEquals("1024", res.headers().get("Content-Length"));

      res.bodyHandler(body -> {
        assertEquals(1024, body.length());
      });
    }, 206, "Partial Content", null);
  }

  @Test
  public void testRangeAwareRequestBodyForDisabledRangeSupport() throws Exception {
    stat.setEnableRangeSupport(false);
    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=0-999"), res -> res.bodyHandler(buff -> {
      assertNull(res.headers().get("Accept-Ranges"));
      assertNotSame("1000", res.headers().get("Content-Length"));

      assertNotSame(1000, buff.length());
      testComplete();
    }), 200, "OK", null);
    await();
  }

  @Test
  public void testOutOfRangeRequestBody() throws Exception {
    stat.setEnableRangeSupport(true);
    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> req.headers().set("Range", "bytes=15783-"), res -> res.bodyHandler(buff -> {
      assertEquals("bytes */15783", res.headers().get("Content-Range"));
      testComplete();
    }), 416, "Requested Range Not Satisfiable", null);
    await();
  }

  @Test
  public void testContentTypeSupport() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/range.jpg", req -> {
    }, res -> {
      assertNotNull(res.getHeader("Content-Type"));
      assertEquals("image/jpeg", res.getHeader("Content-Type"));
      testComplete();
    }, 200, "OK", null);
    await();
  }

  @Test
  public void testAsyncExceptionIssue231() throws Exception {
    stat.setAlwaysAsyncFS(true);
    testRequest(HttpMethod.GET, "/non_existing.html", 404, "Not Found");
  }

  @Test
  public void testServerFileSystemPath() throws Exception {
    router.clear();

    File file = File.createTempFile("vertx", "tmp");
    file.deleteOnExit();

    // remap stat to the temp dir
    try {
      stat = StaticHandler.create(file.getParent());
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    stat = StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(file.getParent());
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/" + file.getName(), 200, "OK", "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccessToRootPath() throws Exception {
    router.clear();

    File file = File.createTempFile("vertx", "tmp");
    file.deleteOnExit();

    // remap stat to the temp dir
    stat = StaticHandler.create().setWebRoot(file.getParent());
  }

  @Test
  public void testLastModifiedInGMT() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertTrue(lastModified.endsWith("GMT"));
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testChangeDefaultContentEncoding() throws Exception {
    stat.setDefaultContentEncoding("ISO-8859-1");
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String contentType = res.headers().get("Content-Type");
      assertEquals("text/html;charset=ISO-8859-1", contentType);
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testHandlerAfter() throws Exception {
    router.get().handler(ctx -> ctx.response().end("Howdy!"));
    testRequest(HttpMethod.GET, "/not-existing-file.html", 200, "OK", "Howdy!");
  }

  @Test
  public void testWriteResponseWhenAlreadyClosed() throws Exception {
    router.clear();
    router
      .route()
      .handler(rc -> {
        rc.next();
        rc.response().end("OtherResponse");
      })
      .handler(stat);
    testRequest(HttpMethod.GET, "/index.html", 200, "OK", "OtherResponse");
  }

  @Test
  public void testEscapeWindows() throws Exception {
    router.clear();
    router
      .route()
      .handler(stat);
    // /\..\index.html -> /index.html
    testRequest(HttpMethod.GET, "/%5c..%5cindex.html", 200, "OK");
  }

  @Test
  public void testWithClassLoader() throws Exception {
    File tmp = File.createTempFile("vertx_", ".txt");
    tmp.deleteOnExit();
    URL url = tmp.toURI().toURL();
    Files.write(tmp.toPath(), "hello".getBytes(StandardCharsets.UTF_8));
    AtomicBoolean used = new AtomicBoolean();
    String expectedResourceName = webRootTarget.getFileName().toString() + "/index.html";
    ClassLoader classLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
      @Override
      public URL getResource(String name) {
        if (expectedResourceName.equals(name)) {
          used.set(true);
          return url;
        }
        return super.getResource(name);
      }
    };
    server.close();
    CountDownLatch latch = new CountDownLatch(1);
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start(Promise<Void> startPromise) throws Exception {
        server = vertx.createHttpServer(getHttpServerOptions());
        server.requestHandler(router)
          .listen()
          .<Void>mapEmpty()
          .onComplete(startPromise);
      }
    }, new DeploymentOptions().setClassLoader(classLoader), onSuccess(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
    testRequest(HttpMethod.GET, "/index.html", 200, "OK", "hello");
    assertTrue(used.get());
  }

  // TODO
  // 1.Test all the params including invalid values
  // 2. Make sure exists isn't being called too many times

  private long toDateTime(String header) {
    try {
      return Utils.parseRFC1123DateTime(header);
    } catch (Exception e) {
      fail(e.getMessage());
      return -1;
    }
  }

  private long fileSize(String filename) {
    return new File(filename).length();
  }

  @Test
  public void testSubRouterBeforeStaticHandler() throws Exception {
    Router subRouter = Router.router(vertx);

    router.clear();
    router
      .route("/test*").subRouter(subRouter);

    router
      .route()
      .handler(stat);

    testRequest(HttpMethod.GET, "/test", 404, "Not Found");
  }
}
