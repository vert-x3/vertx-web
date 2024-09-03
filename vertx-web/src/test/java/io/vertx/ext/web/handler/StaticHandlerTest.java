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

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.impl.Utils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static io.vertx.core.http.HttpHeaders.ACCEPT_ENCODING;
import static io.vertx.ext.web.handler.StaticHandler.DEFAULT_WEB_ROOT;
import static io.vertx.ext.web.handler.StaticHandlerOptions.DEFAULT_MAX_AGE_SECONDS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandlerTest extends WebTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create();
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setIndexPage("otherpage.html"));
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testGetSubdirectoryOtherIndex() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setIndexPage("otherpage.html"));
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory other page</body></html>");
  }

  @Test
  public void testGetSubdirectorySlashOtherIndex() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setIndexPage("otherpage.html"));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setIncludeHidden(false));
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/.hidden.html", 404, "Not Found");
  }

  @Test
  public void testGetHiddenPageSubdir() throws Exception {
    testRequest(HttpMethod.GET, "/somedir/.hidden.html", 200, "OK", "<html><body>Hidden page</body></html>");
  }

  @Test
  public void testCantGetHiddenPageSubdir() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setIncludeHidden(false));
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/somedir/.hidden.html", 404, "Not Found");
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDefaultContentEncoding("UTF-8"));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create("webroot/somedir3");
    router.route().handler(stat);

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

    router.clear();
    stat = StaticHandler.create("webroot/somedir3", new StaticHandlerOptions().setHttp2PushMapping(mappings));
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/testLinkPreload.html", null, res -> {
      List<String> linkHeaders = res.headers().getAll("Link");
      assertTrue(linkHeaders.contains("<style.css>; rel=preload; as=style"));
      assertTrue(linkHeaders.contains("<coin.png>; rel=preload; as=image"));
    }, 200, "OK", null);
  }

  @Test
  public void testNoHttp2Push() throws Exception {
    router.clear();
    stat = StaticHandler.create("webroot/somedir3");
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
    awaitFuture(server.requestHandler(router).listen(8443));
    client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
      .onComplete(onSuccess(req -> {
        req.pushHandler(pushedReq -> pushedReq.response().onComplete(pushedResp -> {
          fail();
        }));
        req.send().onComplete(onSuccess(resp -> {
          assertEquals(200, resp.statusCode());
          assertEquals(HttpVersion.HTTP_2, resp.version());
          resp.bodyHandler(this::assertNotNull);
          testComplete();
        }));
      }));

    await();
  }

  @Test
  public void testHttp2Push() throws Exception {
    waitFor(2);

    List<Http2PushMapping> mappings = new ArrayList<>();
    mappings.add(new Http2PushMapping("style.css", "style", false));
    mappings.add(new Http2PushMapping("coin.png", "image", false));

    router.clear();
    stat = StaticHandler.create("webroot/somedir3", new StaticHandlerOptions().setHttp2PushMapping(mappings));
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
    awaitFuture(server.requestHandler(router).listen(8443));

    client.request(HttpMethod.GET, 8443, "localhost", "/testLinkPreload.html")
      .onComplete(onSuccess(req -> {
        req.pushHandler(pushedReq -> pushedReq.response().onComplete(onSuccess(pushedResp -> {
            assertNotNull(pushedResp);
            pushedResp.bodyHandler(this::assertNotNull);
            complete();
          })))
          .send().onComplete(onSuccess(resp -> {
            assertEquals(200, resp.statusCode());
            assertEquals(HttpVersion.HTTP_2, resp.version());
            resp.bodyHandler(this::assertNotNull);
          }));
      }));

    await();
  }

  @Test
  public void testSkipCompressionForMediaTypes() throws Exception {
    StaticHandler staticHandler = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions()
      .setCompressedMediaTypes(Collections.singleton("image/jpeg")));

    List<String> uris = Arrays.asList("/testCompressionSuffix.html", "/somedir/range.jpg", "/somedir/range.jpeg", "/somedir3/coin.png");
    List<String> expectedContentEncodings = Arrays.asList("gzip", null, null, "gzip");
    testSkipCompression(staticHandler, uris, expectedContentEncodings);
  }

  @Test
  public void testSkipCompressionForSuffixes() throws Exception {
    StaticHandler staticHandler = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions()
      .setCompressedFileSuffixes(Collections.singleton("jpg")));

    List<String> uris = Arrays.asList("/testCompressionSuffix.html", "/somedir/range.jpg", "/somedir/range.jpeg", "/somedir3/coin.png");
    List<String> expectedContentEncodings = Arrays.asList("gzip", null, "gzip", "gzip");
    testSkipCompression(staticHandler, uris, expectedContentEncodings);
  }

  private void testSkipCompression(StaticHandler staticHandler, List<String> uris, List<String> expectedContentEncodings) throws Exception {
    server.close();
    server = vertx.createHttpServer(getHttpServerOptions().setPort(0).setCompressionSupported(true));
    router = Router.router(vertx);
    router.route().handler(staticHandler);
    awaitFuture(server.requestHandler(router).listen());
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
      assertEquals("public, immutable, max-age=" + DEFAULT_MAX_AGE_SECONDS, cacheControl);
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
      assertEquals("public, immutable, max-age=" + DEFAULT_MAX_AGE_SECONDS, cacheControl);
    }, 200, "OK", "<html><body>Subdirectory index page</body></html>");
    testRequest(HttpMethod.GET, "/somedir/", req -> req.putHeader("if-modified-since", lastModifiedRef.get()), null, 304, "Not Modified", null);
  }

  @Test
  public void testCachingDisabled() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setCachingEnabled(false));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setMaxAgeSeconds(maxAge));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create("src/test/filesystemwebroot");
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/fspage.html", 200, "OK", "<html><body>File system page</body></html>");
  }

  @Test
  public void testServeFilesFromFilesystemWithSpaces() throws Exception {
    router.clear();
    stat = StaticHandler.create("src/test/filesystemwebroot");
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
    stat = StaticHandler.create("src/test/filesystemwebroot", new StaticHandlerOptions().setFilesReadOnly(false));
    router.route().handler(stat);

    long modified = Utils.secondsFactor(new File("src/test/filesystemwebroot", "fspage.html").lastModified());
    testRequest(HttpMethod.GET, "/fspage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
    }, 200, "OK", "<html><body>File system page</body></html>");
    testRequest(HttpMethod.GET, "/fspage.html", req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 304, "Not Modified", null);
  }

  @Test
  public void testCacheFilesEntryCached() throws Exception {
    router.clear();
    stat = StaticHandler.create("src/test/filesystemwebroot", new StaticHandlerOptions().setFilesReadOnly(false));
    router.route().handler(stat);

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

    router.clear();
    stat = StaticHandler.create(webroot, new StaticHandlerOptions().setFilesReadOnly(false).setCacheEntryTimeout(cacheEntryTimeout));
    router.route().handler(stat);

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

    router.clear();
    stat = StaticHandler.create(webroot.getPath(), new StaticHandlerOptions().setFilesReadOnly(false).setCacheEntryTimeout(3600 * 1000));
    router.route().handler(stat);

    long modified = Utils.secondsFactor(pageFile.lastModified());
    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 304, "Not Modified", null);
    pageFile.delete();
    testRequest(HttpMethod.GET, page, 404, "Not Found");
    testRequest(HttpMethod.GET, page, req -> req.putHeader("if-modified-since", Utils.formatRFC1123DateTime(modified)), null, 404, "Not Found", null);

  }

  @Test
  public void testDirectoryListingText() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true).setIncludeHidden(false));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true).setIncludeHidden(false));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true));
    router.route().handler(stat);

    testDirectoryListingHtmlCustomTemplate("META-INF/vertx/web/vertx-web-directory.html");
  }

  @Test
  public void testCustomDirectoryListingHtml() throws Exception {
    router.clear();
    String dirTemplate = "custom_dir_template.html";
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDirectoryListing(true).setDirectoryTemplate(dirTemplate));
    router.route().handler(stat);
    testDirectoryListingHtmlCustomTemplate(dirTemplate);
  }

  private void testDirectoryListingHtmlCustomTemplate(String dirTemplateFile) throws Exception {
    String directoryTemplate = vertx.fileSystem().readFileBlocking(dirTemplateFile).toString();

    String parentLink = "<a href=\"/\">..</a>";
    String files = "<ul id=\"files\"><li><a href=\"/somedir2/foo2.json\" title=\"foo2.json\">foo2.json</a></li>" +
      "<li><a href=\"/somedir2/somepage.html\" title=\"somepage.html\">somepage.html</a></li>" +
      "<li><a href=\"/somedir2/somepage2.html\" title=\"somepage2.html\">somepage2.html</a></li></ul>";

    String expected = directoryTemplate.replace("{directory}", "/somedir2/").replace("{parent}", parentLink).replace("{files}", files);

    testRequest(HttpMethod.GET, "/somedir2/", req -> req.putHeader("accept", "text/html"), resp -> resp.bodyHandler(buff -> {
      assertEquals("text/html", resp.headers().get("content-type"));
      String sBuff = buff.toString();
      assertEquals(expected, sBuff);
      testComplete();
    }), 200, "OK", null);
    await();
  }

  @Test
  public void testFSBlockingTuning() throws Exception {
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setCachingEnabled(false).setMaxAvgServeTimeNs(10000));
    router.route().handler(stat);

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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setEnableRangeSupport(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setEnableRangeSupport(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setEnableRangeSupport(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setEnableRangeSupport(false));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setEnableRangeSupport(true));
    router.route().handler(stat);
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setAlwaysAsyncFS(true));
    router.route().handler(stat);
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

    stat = StaticHandler.createWithRootFileSystemAccess(file.getParent(), new StaticHandlerOptions());
    router.route().handler(stat);

    testRequest(HttpMethod.GET, "/" + file.getName(), 200, "OK", "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAccessToRootPath() throws Exception {
    router.clear();

    File file = File.createTempFile("vertx", "tmp");
    file.deleteOnExit();

    // remap stat to the temp dir
    stat = StaticHandler.create(file.getParent());
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
    router.clear();
    stat = StaticHandler.create(DEFAULT_WEB_ROOT, new StaticHandlerOptions().setDefaultContentEncoding("ISO-8859-1"));
    router.route().handler(stat);
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
    ClassLoader classLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
      @Override
      public URL getResource(String name) {
        if (name.equals("webroot/index.html")) {
          used.set(true);
          return url;
        }
        return super.getResource(name);
      }
    };
    server.close();
    awaitFuture(vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start(Promise<Void> startPromise) {
        vertx.createHttpServer(getHttpServerOptions())
          .requestHandler(router)
          .listen()
          .<Void>mapEmpty()
          .onComplete(startPromise);
      }
    }, new DeploymentOptions().setClassLoader(classLoader)));
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
