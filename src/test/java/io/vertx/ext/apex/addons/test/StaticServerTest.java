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
import io.vertx.core.json.JsonArray;
import io.vertx.ext.apex.addons.StaticServer;
import io.vertx.ext.apex.core.impl.Utils;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticServerTest extends ApexTestBase {

  private final DateFormat dateTimeFormatter = Utils.createISODateTimeFormatter();

  protected StaticServer stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticServer.staticServer();
    router.route().handler(stat);
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Index page</body></html>");
  }

  @Test
  public void testGetOtherIndex() throws Exception {
    stat.setIndexPage("otherpage.html");
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Other page</body></html>");
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
  }

  @Test
  public void testCantGetHiddenPageSubdir() throws Exception {
    stat.setIncludeHidden(false);
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
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String contentType = res.headers().get("content-type");
      String contentLength = res.headers().get("content-length");
      assertEquals("text/html", contentType);
      assertEquals(36, Integer.valueOf(contentLength).intValue());
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/foo.json", null, res -> {
      String contentType = res.headers().get("content-type");
      String contentLength = res.headers().get("content-length");
      assertEquals("application/json", contentType);
      assertEquals(18, Integer.valueOf(contentLength).intValue());
    }, 200, "OK", null);
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
    AtomicReference<String> lastModifiedRef = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      lastModifiedRef.set(lastModified);
      assertNotNull(cacheControl);
      assertNotNull(lastModified);
      long diff = System.currentTimeMillis() - toDateTime(lastModified);
      assertTrue(diff > 0 && diff < 2000);
      assertEquals("public, max-age=" + StaticServer.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    }, 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", req -> {
      req.putHeader("if-modified-since", lastModifiedRef.get());
    }, null, 304, "Not Modified", null);
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
  public void testCacheGetNew() throws Exception {
    AtomicReference<String> lastModifiedRef = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      String lastModified = res.headers().get("last-modified");
      lastModifiedRef.set(lastModified);
      assertNotNull(cacheControl);
      assertNotNull(lastModified);
      long diff = System.currentTimeMillis() - toDateTime(lastModified);
      assertTrue(diff > 0 && diff < 2000);
      assertEquals("public, max-age=" + StaticServer.DEFAULT_MAX_AGE_SECONDS, cacheControl);
    }, 200, "OK", "<html><body>Other page</body></html>");
    testRequest(HttpMethod.GET, "/otherpage.html", req -> {
      req.putHeader("if-modified-since", dateTimeFormatter.format(toDateTime(lastModifiedRef.get()) - 1));
    }, res -> {
    }, 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testSetMaxAge() throws Exception {
    long maxAge = 60 * 60;
    stat.setMaxAgeSeconds(maxAge);
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String cacheControl = res.headers().get("cache-control");
      assertEquals("public, max-age=" + maxAge, cacheControl);
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
  public void testServeFilesFromFilesystemWebRootConstructor() throws Exception {
    stat = StaticServer.staticServer("src/test/filesystemwebroot");
    router.clear();
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/fspage.html", 200, "OK", "<html><body>File system page</body></html>");
  }

  @Test
  public void testCacheFilesNotReadOnly() throws Exception {
    stat.setFilesReadOnly(false);
    stat.setWebRoot("src/test/filesystemwebroot");
    long modified = new File("src/test/filesystemwebroot", "fspage.html").lastModified();
    testRequest(HttpMethod.GET, "/fspage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
    }, 200, "OK", "<html><body>File system page</body></html>");
    testRequest(HttpMethod.GET, "/fspage.html", req -> {
      req.putHeader("if-modified-since", dateTimeFormatter.format(modified));
    }, null, 304, "Not Modified", null);
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
    testRequest(HttpMethod.GET, "/fspage.html", req -> {
      req.putHeader("if-modified-since", dateTimeFormatter.format(modified));
    }, null, 304, "Not Modified", null);
  }

  @Test
  public void testCacheFilesEntryOld() throws Exception {
    stat.setFilesReadOnly(false);
    stat.setWebRoot("src/test/filesystemwebroot");
    stat.setCacheEntryTimeout(2000);
    File resource = new File("src/test/filesystemwebroot", "fspage.html");
    long modified = resource.lastModified();
    testRequest(HttpMethod.GET, "/fspage.html", null, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified, toDateTime(lastModified));
      // Now update the web resource
      resource.setLastModified(modified + 1000);
    }, 200, "OK", "<html><body>File system page</body></html>");
    // But it should return a new entry as the entry is now old
    Thread.sleep(2001);
    testRequest(HttpMethod.GET, "/fspage.html", req -> {
      req.putHeader("if-modified-since", dateTimeFormatter.format(modified));
    }, res -> {
      String lastModified = res.headers().get("last-modified");
      assertEquals(modified + 1000, toDateTime(lastModified));
    }, 200, "OK", "<html><body>File system page</body></html>");
  }

  // FIXME - currently ignored as FileResolver doesn't recursively unpack directories

  @Test
  @Ignore
  public void testDirectoryListingText() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "foo.json", "index.html", "otherpage.html", "somedir"));

    testRequest(HttpMethod.GET, "/", null, resp -> {
      resp.bodyHandler(buff -> {
        String sBuff = buff.toString();
        String[] elems = sBuff.split("\n");
        assertEquals(expected.size(), elems.length);
        for (String elem: elems) {
          assertTrue(expected.contains(elem));
        }
      });
    }, 200, "OK", null);
  }

  @Test
  @Ignore
  public void testDirectoryListingTextNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "index.html", "otherpage.html", "somedir"));

    testRequest(HttpMethod.GET, "/", null, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("text/plain", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        String[] elems = sBuff.split("\n");
        assertEquals(expected.size(), elems.length);
        for (String elem: elems) {
          assertTrue(expected.contains(elem));
        }
      });
    }, 200, "OK", null);
  }

  @Test
  @Ignore
  public void testDirectoryListingJson() throws Exception {
    stat.setDirectoryListing(true);
    Set<String> expected = new HashSet<>(Arrays.asList(".hidden.html", "foo.json", "index.html", "otherpage.html", "somedir"));

    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("accept", "application/json");
    }, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("application/json", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        JsonArray arr = new JsonArray(sBuff);
        assertEquals(expected.size(), arr.size());
        for (Object elem: arr) {
          assertTrue(expected.contains(elem));
        }
        testComplete();
      });
    }, 200, "OK", null);
    await();
  }

  @Test
  @Ignore
  public void testDirectoryListingJsonNoHidden() throws Exception {
    stat.setDirectoryListing(true);
    stat.setIncludeHidden(false);
    Set<String> expected = new HashSet<>(Arrays.asList("foo.json", "index.html", "otherpage.html", "somedir"));

    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("accept", "application/json");
    }, resp -> {
      resp.bodyHandler(buff -> {
        assertEquals("application/json", resp.headers().get("content-type"));
        String sBuff = buff.toString();
        JsonArray arr = new JsonArray(sBuff);
        assertEquals(expected.size(), arr.size());
        for (Object elem: arr) {
          assertTrue(expected.contains(elem));
        }
      });
    }, 200, "OK", null);
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

  // TODO
  // 1.Test all the params including invalid values
  // 2. Make sure exists isn't being called too many times

  private long toDateTime(String header) {
    try {
      Date date = dateTimeFormatter.parse(header);
      return date.getTime();
    } catch (Exception e) {
      fail(e.getMessage());
      return -1;
    }
  }



}
