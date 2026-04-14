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

import io.netty.util.internal.PlatformDependent;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;

import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.spi.file.FileResolver;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.test.core.TestUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.function.BiFunction;

import static io.vertx.core.http.HttpHeaders.ACCEPT_ENCODING;

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
  @BeforeEach
  public void setUp(io.vertx.core.Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
    webRootTarget = Files.createTempDirectory(webRootSrc.getParent(), "webroot");
    copyWebRootFiles();
    stat = StaticHandler.create(webRootTarget.getFileName().toString());
    router.route().handler(stat);
  }

  private void copyWebRootFiles() throws IOException {
    Files.walkFileTree(webRootSrc, new SimpleFileVisitor<>() {
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
  @AfterEach
  public void tearDown(VertxTestContext testContext) throws Exception {
    super.tearDown(testContext);
    deleteWebRootFiles();
  }

  private void deleteWebRootFiles() throws IOException {
    Files.walkFileTree(webRootTarget, new SimpleFileVisitor<>() {
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
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir").followRedirects(false).send(), 301, "Moved Permanently");
    String location = resp.headers().get("location");
    assertEquals("/somedir/", location);
  }

  @Test
  public void testGetSubdirectorySlashDefaultIndex() throws Exception {

    VertxInternal vi = (VertxInternal) vertx;
    FileResolver resolver = vi.fileResolver();
    File f = resolver.resolve("webroot/");
    System.out.println("f = " + f.getAbsolutePath());

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
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK");
    String dateHeader = resp.headers().get("date");
    assertNotNull(dateHeader);
    long diff = System.currentTimeMillis() - toDateTime(dateHeader);
    assertTrue(diff > 0 && diff < 2000);
  }

  @Test
  public void testContentHeadersSet() throws Exception {
    stat.setDefaultContentEncoding("UTF-8");
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK");
    String contentType = resp.headers().get("content-type");
    String contentLength = resp.headers().get("content-length");
    assertEquals("text/html;charset=UTF-8", contentType);
    assertEquals(fileSize("src/test/resources/webroot/otherpage.html"), Integer.valueOf(contentLength).intValue());
    resp = testRequest(webClient.get("/foo.json").send(), 200, "OK");
    contentType = resp.headers().get("content-type");
    contentLength = resp.headers().get("content-length");
    assertEquals("application/json", contentType);
    assertEquals(fileSize("src/test/resources/webroot/foo.json"), Integer.valueOf(contentLength).intValue());
  }

  @Test
  public void testNoLinkPreload() throws Exception {
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "webroot/somedir3");
    router.route().handler(stat);

    HttpResponse<Buffer> resp = testRequest(webClient.get("/testLinkPreload.html").send(), 200, "OK");
    List<String> linkHeaders = resp.headers().getAll("Link");
    assertTrue(linkHeaders.isEmpty());
  }

  @Test
  public void testLinkPreload() throws Exception {
    List<Http2PushMapping> mappings = new ArrayList<>();
    mappings.add(new Http2PushMapping("style.css", "style", false));
    mappings.add(new Http2PushMapping("coin.png", "image", false));

    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "webroot/somedir3");
    router.route().handler(stat);

    stat.setHttp2PushMapping(mappings);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/testLinkPreload.html").send(), 200, "OK");
    List<String> linkHeaders = resp.headers().getAll("Link");
    assertTrue(linkHeaders.contains("<style.css>; rel=preload; as=style"));
    assertTrue(linkHeaders.contains("<coin.png>; rel=preload; as=image"));
  }

  @Test
  public void testNoHttp2Push() throws Exception {
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "webroot/somedir3");
    router.route().handler(stat);

    client.close();
    client = vertx.createHttpClient(new HttpClientOptions()
      .setSsl(true)
      .setUseAlpn(true)
      .setProtocolVersion(HttpVersion.HTTP_2)
      .setTrustOptions(new PemTrustOptions().addCertPath("tls/server-cert.pem")));

    server.close();
    server = vertx.createHttpServer(new HttpServerOptions()
        .setUseAlpn(true)
        .setSsl(true)
      .setKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")));
    (server.requestHandler(router).listen(8443)).await();
    HttpClientResponse resp = client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
      .compose(req -> {
        req.pushHandler(pushedReq -> pushedReq.response().onComplete(pushedResp -> {
          fail();
        }));
        return req.send();
      }).await();
    assertEquals(200, resp.statusCode());
    assertEquals(HttpVersion.HTTP_2, resp.version());
    assertNotNull(resp.body().await());
  }

  @Test
  public void testHttp2Push(VertxTestContext testContext) throws Exception {
    Checkpoint pushReceived = testContext.checkpoint(2);

    List<Http2PushMapping> mappings = new ArrayList<>();
    mappings.add(new Http2PushMapping("style.css", "style", false));
    mappings.add(new Http2PushMapping("coin.png", "image", false));

    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "webroot/somedir3");
    router.route().handler(stat);

    stat.setHttp2PushMapping(mappings);

    client.close();
    client = vertx.createHttpClient(new HttpClientOptions()
      .setSsl(true)
      .setUseAlpn(true)
      .setProtocolVersion(HttpVersion.HTTP_2)
      .setTrustOptions(new PemTrustOptions().addCertPath("tls/server-cert.pem")));

    server.close();
    server = vertx.createHttpServer(new HttpServerOptions()
        .setUseAlpn(true)
        .setSsl(true)
      .setKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")));
    server
      .requestHandler(router)
      .listen(8443)
      .await();

    client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
      .compose(req ->
        req.pushHandler(push -> {
            assertNotNull(push);
            push.response().onComplete(TestUtils.onSuccess(resp -> {
              resp.body().onComplete(TestUtils.onSuccess(body -> {
                assertTrue(body.length() > 0);
                pushReceived.flag();
              }));
            }));
        }).send()
          .expecting(HttpResponseExpectation.SC_OK)
          .expecting(resp -> resp.version() == HttpVersion.HTTP_2)
          .compose(HttpClientResponse::body)
      ).await();
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
    (server.requestHandler(router).listen()).await();
    List<String> actualEncodings = new ArrayList<>();
    for (String uri : uris) {
      io.vertx.ext.web.client.HttpResponse<io.vertx.core.buffer.Buffer> resp = webClient
        .get(server.actualPort(), getHttpClientOptions().getDefaultHost(), uri)
        .putHeader(ACCEPT_ENCODING.toString(), String.join(", ", "gzip", "jpg", "jpeg", "png"))
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .await();
      actualEncodings.add(resp.getHeader(HttpHeaders.CONTENT_ENCODING.toString()));
    }
    assertEquals(expectedContentEncodings, actualEncodings);
  }

  @Test
  public void testHead() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.head("/otherpage.html").send(), 200, "OK");
    Buffer body = resp.body();
    assertTrue(body == null || body.length() == 0);
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
    testCacheReturnFromCache((lastModified, req) -> req, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testCacheReturnFromCacheWhenInvalidHeader() throws Exception {
    testCacheReturnFromCache((lastModified, req) -> req.putHeader("if-modified-since", "whatever"), 200, "OK", "<html><body>Other page</body></html>");
  }

  private void testCacheReturnFromCache(BiFunction<String, HttpRequest<Buffer>, HttpRequest<Buffer>> handler, int expectedStatusCode, String expectedStatusMessage, String expectedStatusBody) throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String cacheControl = resp.headers().get("cache-control");
    String lastModified = resp.headers().get("last-modified");
    assertNotNull(cacheControl);
    assertNotNull(lastModified);
    assertEquals("public, immutable, max-age=" + StaticHandler.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    testRequest(handler.apply(lastModified, webClient.get("/otherpage.html")), expectedStatusCode, expectedStatusMessage, expectedStatusBody);
  }

  @Test
  public void testCacheIndexPageReturnFromCache() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/").send(), 200, "OK", "<html><body>Subdirectory index page</body></html>");
    String cacheControl = resp.headers().get("cache-control");
    String lastModified = resp.headers().get("last-modified");
    assertNotNull(cacheControl);
    assertNotNull(lastModified);
    assertEquals("public, immutable, max-age=" + StaticHandler.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    testRequest(webClient.get("/somedir/").putHeader("if-modified-since", lastModified).send(), 304, "Not Modified");
  }

  @Test
  public void testCachingDisabled() throws Exception {
    stat.setCachingEnabled(false);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String cacheControl = resp.headers().get("cache-control");
    String lastModified = resp.headers().get("last-modified");
    assertNull(cacheControl);
    assertNull(lastModified);
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

    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").putHeader("accept-encoding", "gzip").send(), 200, "OK", "<html><body>Other page</body></html>");
    String cacheControl = resp.headers().get("cache-control");
    String lastModified = resp.headers().get("last-modified");
    String vary = resp.headers().get("vary");
    assertEquals("test1", cacheControl);
    assertEquals("test2", lastModified);
    assertEquals("test3", vary);
  }

  @Test
  public void testSendVaryAcceptEncodingHeader() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").putHeader("accept-encoding", "gzip").send(), 200, "OK", "<html><body>Other page</body></html>");
    String vary = resp.headers().get("vary");
    assertNotNull(vary);
    assertEquals("accept-encoding", vary);
  }

  @Test
  public void testNoSendingOfVaryAcceptEncodingHeader() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String vary = resp.headers().get("vary");
    assertNull(vary);
  }

  @Test
  public void testSetMaxAge() throws Exception {
    long maxAge = 60 * 60;
    stat.setMaxAgeSeconds(maxAge);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String cacheControl = resp.headers().get("cache-control");
    assertEquals("public, immutable, max-age=" + maxAge, cacheControl);
  }

  @Test
  public void testGetOtherPageTwice() throws Exception {
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystem() throws Exception {
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "src/test/filesystemwebroot");
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/fspage.html", 200, "OK", "<html><body>File system page</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystemWithSpaces() throws Exception {
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "src/test/filesystemwebroot");
    router.route().handler(stat);

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
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "src/test/filesystemwebroot");
    router.route().handler(stat);

    stat.setFilesReadOnly(false);
    long modified = Utils.secondsFactor(new File("src/test/filesystemwebroot", "fspage.html").lastModified());
    HttpResponse<Buffer> resp = testRequest(webClient.get("/fspage.html").send(), 200, "OK", "<html><body>File system page</body></html>");
    String lastModified = resp.headers().get("last-modified");
    assertEquals(modified, toDateTime(lastModified));
    testRequest(webClient.get("/fspage.html").putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)).send(), 304, "Not Modified");
  }

  @Test
  public void testCacheFilesEntryCached() throws Exception {
    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, "src/test/filesystemwebroot");
    router.route().handler(stat);

    stat.setFilesReadOnly(false);
    File resource = new File("src/test/filesystemwebroot", "fspage.html");
    long modified = resource.lastModified();
    HttpResponse<Buffer> resp = testRequest(webClient.get("/fspage.html").send(), 200, "OK", "<html><body>File system page</body></html>");
    String lastModified = resp.headers().get("last-modified");
    assertEquals(modified, toDateTime(lastModified));
    // Now update the web resource
    resource.setLastModified(modified + 1000);
    // But it should still return not modified as the entry is cached
    testRequest(webClient.get("/fspage.html").putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)).send(), 304, "Not Modified");
  }

  @Test
  public void testCacheFilesEntryOld() throws Exception {
    String webroot = "src/test/filesystemwebroot", page = "/fspage.html";
    File resource = new File(webroot + page);
    String html = new String(Files.readAllBytes(resource.toPath()));
    int cacheEntryTimeout = 100;

    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, webroot);
    router.route().handler(stat);

    stat.setFilesReadOnly(false);
    stat.setCacheEntryTimeout(cacheEntryTimeout);

    long modified = Utils.secondsFactor(resource.lastModified());
    HttpResponse<Buffer> resp = testRequest(webClient.get(page).send(), 200, "OK", html);
    String lastModified = resp.headers().get("last-modified");
    assertEquals(modified, toDateTime(lastModified));
    // Now update the web resource
    resource.setLastModified(modified + 1000);
    // But it should return a new entry as the entry is now old
    Thread.sleep(cacheEntryTimeout + 1);
    resp = testRequest(webClient.get(page).putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)).send(), 200, "OK", html);
    lastModified = resp.headers().get("last-modified");
    assertEquals(modified + 1000, toDateTime(lastModified));

    // 304 must still work when cacheEntry.isOutOfDate() == true, https://github.com/vert-x3/vertx-web/issues/726
    Thread.sleep(cacheEntryTimeout + 1);

    testRequest(webClient.get(page).putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified + 1000)), 304, "Not Modified");
  }

  @Test
  public void testCacheFilesFileDeleted() throws Exception {
    File webroot = new File("target/.vertx/webroot"), pageFile = new File(webroot, "deleted.html");
    if (!pageFile.exists()) {
      webroot.mkdirs();
      pageFile.createNewFile();
    }
    String page = '/' + pageFile.getName();

    router.clear();
    stat = StaticHandler.create(FileSystemAccess.RELATIVE, webroot.getPath());
    router.route().handler(stat);

    stat.setFilesReadOnly(false);
    stat.setCacheEntryTimeout(3600 * 1000);

    long modified = Utils.secondsFactor(pageFile.lastModified());
    testRequest(webClient.get(page).putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)).send(), 304, "Not Modified");
    pageFile.delete();
    testRequest(HttpMethod.GET, page, 404, "Not Found");
    testRequest(webClient.get(page).putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)).send(), 404, "Not Found");

  }

  @Test
  public void testDirectoryListingText() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "a", "foo.json", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    String sBuff = resp.body().toString();
    String[] elems = sBuff.split("\n");
    assertEquals(expected.size(), elems.length);
    for (String elem : elems) {
      assertTrue(expected.contains(elem));
    }
  }

  @Test
  public void testDirectoryListingTextNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "a", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    assertEquals("text/plain", resp.headers().get("content-type"));
    String sBuff = resp.body().toString();
    String[] elems = sBuff.split("\n");
    assertEquals(expected.size(), elems.length);
    for (String elem : elems) {
      assertTrue(expected.contains(elem));
    }
  }

  @Test
  public void testDirectoryListingJson() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "foo.json", "index.html", "otherpage.html", "a", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("accept", "application/json").send(), 200, "OK");
    assertEquals("application/json", resp.headers().get("content-type"));
    String sBuff = resp.body().toString();
    JsonArray arr = new JsonArray(sBuff);
    assertEquals(expected.size(), arr.size());
    for (Object elem : arr) {
      assertTrue(expected.contains(elem));
    }
  }

  @Test
  public void testDirectoryListingJsonNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "a", "index.html", "otherpage.html", "somedir", "somedir2", "somedir3", "testCompressionSuffix.html", "file with spaces.html", "sockjs", "swaggerui"));
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("accept", "application/json").send(), 200, "OK");
    assertEquals("application/json", resp.headers().get("content-type"));
    String sBuff = resp.body().toString();
    JsonArray arr = new JsonArray(sBuff);
    assertEquals(expected.size(), arr.size());
    for (Object elem : arr) {
      assertTrue(expected.contains(elem));
    }
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
    Assumptions.assumeFalse(PlatformDependent.isWindows());

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

    HttpResponse<Buffer> resp = testRequest(webClient.get(path).putHeader("accept", "text/html").send(), 200, "OK");
    assertEquals("text/html", resp.headers().get("content-type"));
    String sBuff = resp.body().toString();
    assertEquals(expected, sBuff);
  }

  @Test
  public void testFSBlockingTuning() throws Exception {
    stat.setCachingEnabled(false);
    stat.setMaxAvgServeTimeNs(10000);
    for (int i = 0; i < 2000; i++) {
      HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
      String cacheControl = resp.headers().get("cache-control");
      String lastModified = resp.headers().get("last-modified");
      assertNull(cacheControl);
      assertNull(lastModified);
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

    HttpResponse<Buffer> resp = testRequest(webClient.head("/somedir/range.jpg").send(), 200, "OK");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("15783", resp.headers().get("Content-Length"));

    resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=0-999").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1000", resp.headers().get("Content-Length"));
    assertEquals("bytes 0-999/15783", resp.headers().get("Content-Range"));

    resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=1000-").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("14783", resp.headers().get("Content-Length"));
    assertEquals("bytes 1000-15782/15783", resp.headers().get("Content-Range"));

    resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=1000-5000000").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("14783", resp.headers().get("Content-Length"));
    assertEquals("bytes 1000-15782/15783", resp.headers().get("Content-Range"));
  }

  @Test
  public void testRangeAwareRequestBody() throws Exception {
    stat.setEnableRangeSupport(true);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=0-999").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1000", resp.headers().get("Content-Length"));
    assertEquals("bytes 0-999/15783", resp.headers().get("Content-Range"));
    assertEquals(1000, resp.body().length());
  }

  @Test
  public void testRangeAwareRequestSegment() throws Exception {
    stat.setEnableRangeSupport(true);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/range.bin").putHeader("Range", "bytes=0-1023").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1024", resp.headers().get("Content-Length"));
    assertEquals(1024, resp.body().length());

    resp = testRequest(webClient.get("/somedir/range.bin").putHeader("Range", "bytes=1024-2047").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1024", resp.headers().get("Content-Length"));
    assertEquals(1024, resp.body().length());

    resp = testRequest(webClient.get("/somedir/range.bin").putHeader("Range", "bytes=2048-3071").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1024", resp.headers().get("Content-Length"));
    assertEquals(1024, resp.body().length());

    resp = testRequest(webClient.get("/somedir/range.bin").putHeader("Range", "bytes=3072-4095").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1024", resp.headers().get("Content-Length"));
    assertEquals(1024, resp.body().length());

    resp = testRequest(webClient.get("/somedir/range.bin").putHeader("Range", "bytes=4096-5119").send(), 206, "Partial Content");
    assertEquals("bytes", resp.headers().get("Accept-Ranges"));
    assertEquals("1024", resp.headers().get("Content-Length"));
    assertEquals(1024, resp.body().length());
  }

  @Test
  public void testRangeAwareRequestBodyForDisabledRangeSupport() throws Exception {
    stat.setEnableRangeSupport(false);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=0-999").send(), 200, "OK");
    assertNull(resp.headers().get("Accept-Ranges"));
    assertNotSame("1000", resp.headers().get("Content-Length"));
    assertNotSame(1000, resp.body().length());
  }

  @Test
  public void testOutOfRangeRequestBody() throws Exception {
    stat.setEnableRangeSupport(true);
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/range.jpg").putHeader("Range", "bytes=15783-").send(), 416, "Requested Range Not Satisfiable");
    assertEquals("bytes */15783", resp.headers().get("Content-Range"));
  }

  @Test
  public void testContentTypeSupport() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir/range.jpg").send(), 200, "OK");
    assertNotNull(resp.getHeader("Content-Type"));
    assertEquals("image/jpeg", resp.getHeader("Content-Type"));
  }

  @Test
  public void testAsyncExceptionIssue231() throws Exception {
    stat.setAlwaysAsyncFS(true);
    testRequest(HttpMethod.GET, "/non_existing.html", 404, "Not Found");
  }

  @Disabled("handle me")
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

    stat = StaticHandler.create(FileSystemAccess.ROOT, file.getParent());
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/" + file.getName(), 200, "OK", "");
  }

  @Test
  public void testAccessToRootPath() throws Exception {
    router.clear();

    File file = File.createTempFile("vertx", "tmp");
    file.deleteOnExit();

    // remap stat to the temp dir
    assertThrows(IllegalArgumentException.class, () -> StaticHandler.create(FileSystemAccess.RELATIVE, file.getParent()));
  }

  @Test
  public void testLastModifiedInGMT() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String lastModified = resp.headers().get("last-modified");
    assertTrue(lastModified.endsWith("GMT"));
  }

  @Test
  public void testChangeDefaultContentEncoding() throws Exception {
    stat.setDefaultContentEncoding("ISO-8859-1");
    HttpResponse<Buffer> resp = testRequest(webClient.get("/otherpage.html").send(), 200, "OK", "<html><body>Other page</body></html>");
    String contentType = resp.headers().get("Content-Type");
    assertEquals("text/html;charset=ISO-8859-1", contentType);
  }

  @Test
  public void testHandlerAfter() throws Exception {
    router.get().handler(ctx -> ctx.response().end("Howdy!"));
    testRequest(HttpMethod.GET, "/not-existing-file.html", 200, "OK", "Howdy!");
  }

  @Test
  public void testWriteResponseWhenAlreadyClosed(VertxTestContext testContext) throws Exception {
    Checkpoint done = testContext.checkpoint();
    router.clear();
    router
      .route()
      .handler(rc -> {
        rc.next();
        rc.response().end("OtherResponse");
        Context ctx = Vertx.currentContext();
        ctx.exceptionHandler(expected -> {
          // Thrown by static handler when trying to send the file and the response has already
          // been sent
          done.flag();
        });
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
    (vertx.deployVerticle(new VerticleBase() {
      @Override
      public Future<?> start() throws Exception {
        return vertx.createHttpServer(getHttpServerOptions())
          .requestHandler(router)
          .listen();
      }
    }, new DeploymentOptions().setClassLoader(classLoader))).await();
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
