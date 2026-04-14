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
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class StaticDirectoryListHandlerTest extends WebTestBase2 {

  protected StaticHandler stat;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
    stat = StaticHandler.create("webroot").setDirectoryListing(true).setDirectoryTemplate("custom_dir_template.html");
    router.route().handler(stat);
  }

  @Test
  public void testGetSubSubDirectory() throws Exception {
    String expected = "<html>\n" +
      "<body>\n" +
      "<h1>Custom Index of /a/b/</h1>\n" +
      "<a href=\"/a/\">..</a>\n" +
      "<ul id=\"files\"><li><a href=\"/a/b/test.txt\" title=\"test.txt\">test.txt</a></li></ul>\n" +
      "</body>\n" +
      "</html>";
    Buffer response = testRequest(webClient.get("/a/b/").putHeader("Accept", "text/html").send(), 200, "OK")
      .body();
    assertEquals(expected, normalizeLineEndingsFor(response).toString());
  }

  @Test
  public void testGetDirectory() throws Exception {
    String expected = "<html>\n" +
      "<body>\n" +
      "<h1>Custom Index of /</h1>\n" +
      "<a href=\"/\">..</a>\n" +
      "<ul id=\"files\"><li><a href=\"/.hidden.html\" title=\".hidden.html\">.hidden.html</a></li><li><a href=\"/a\" title=\"a\">a</a></li><li><a href=\"/file%20with%20spaces.html\" title=\"file with spaces.html\">file with spaces.html</a></li><li><a href=\"/foo.json\" title=\"foo.json\">foo.json</a></li><li><a href=\"/index.html\" title=\"index.html\">index.html</a></li><li><a href=\"/otherpage.html\" title=\"otherpage.html\">otherpage.html</a></li><li><a href=\"/sockjs\" title=\"sockjs\">sockjs</a></li><li><a href=\"/somedir\" title=\"somedir\">somedir</a></li><li><a href=\"/somedir2\" title=\"somedir2\">somedir2</a></li><li><a href=\"/somedir3\" title=\"somedir3\">somedir3</a></li><li><a href=\"/swaggerui\" title=\"swaggerui\">swaggerui</a></li><li><a href=\"/testCompressionSuffix.html\" title=\"testCompressionSuffix.html\">testCompressionSuffix.html</a></li></ul>\n" +
      "</body>\n" +
      "</html>";
    Buffer response = testRequest(webClient.get("/").putHeader("Accept", "text/html").send(), 200, "OK")
      .body();
    assertEquals(expected, normalizeLineEndingsFor(response).toString());
  }

  @Test
  public void testGetDirectoryFuzzyAccepts() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("Accept", "application/json, text/plain; q=0.9").send(), 200, "OK");
    assertEquals("application/json", resp.getHeader("Content-Type"));
  }

  @Test
  public void testGetDirectoryFuzzyAccepts2() throws Exception {
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("Accept", "application/json; q=0.8, text/plain; q=0.9").send(), 200, "OK");
    assertEquals("text/plain", resp.getHeader("Content-Type"));
  }

  @Test
  public void testGetDirectoryOnSubdirMount() throws Exception {
    router.clear();
    router.route("/c/*").handler(stat);

    String expected = "<html>\n" +
      "<body>\n" +
      "<h1>Custom Index of /c/a/b/</h1>\n" +
      "<a href=\"/c/a/\">..</a>\n" +
      "<ul id=\"files\"><li><a href=\"/c/a/b/test.txt\" title=\"test.txt\">test.txt</a></li></ul>\n" +
      "</body>\n" +
      "</html>";
    Buffer response = testRequest(webClient.get("/c/a/b/").putHeader("Accept", "text/html").send(), 200, "OK")
      .body();
    assertEquals(expected, normalizeLineEndingsFor(response).toString());
  }

  @Test
  public void testGetDirectoryOnPrefixMount() throws Exception {
    router.clear();
    router.route("/c*").handler(stat);

    // even though the prefix is matched only the prefix is ignored from the file system match
    // webroot/annot/a/b will not be found
    testRequest(webClient.get("/cannot/a/b/").putHeader("Accept", "text/html").send(), 404, "Not Found");
  }
}
