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

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class StaticDirectoryListHandlerTest extends WebTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create("webroot").setDirectoryListing(true).setDirectoryTemplate("custom_dir_template.html");
    router.route().handler(stat);
  }

  @Test
  public void testGetSubSubDirectory() throws Exception {
    testRequest(HttpMethod.GET, "/a/b/", req -> req.putHeader("Accept", "text/html"), null, 200, "OK", "<html>\n" +
        "<body>\n" +
        "<h1>Custom Index of /a/b/</h1>\n" +
        "<a href=\"/a/\">..</a>\n" +
        "<ul id=\"files\"><li><a href=\"/a/b/test.txt\" title=\"test.txt\">test.txt</a></li></ul>\n" +
        "</body>\n" +
        "</html>");
  }

  @Test
  public void testGetDirectory() throws Exception {
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("Accept", "text/html"), null, 200, "OK", "<html>\n" +
        "<body>\n" +
        "<h1>Custom Index of /</h1>\n" +
        "<a href=\"/\">..</a>\n" +
        "<ul id=\"files\"><li><a href=\"/.hidden.html\" title=\".hidden.html\">.hidden.html</a></li><li><a href=\"/a\" title=\"a\">a</a></li><li><a href=\"/file with spaces.html\" title=\"file with spaces.html\">file with spaces.html</a></li><li><a href=\"/foo.json\" title=\"foo.json\">foo.json</a></li><li><a href=\"/index.html\" title=\"index.html\">index.html</a></li><li><a href=\"/otherpage.html\" title=\"otherpage.html\">otherpage.html</a></li><li><a href=\"/somedir\" title=\"somedir\">somedir</a></li><li><a href=\"/somedir2\" title=\"somedir2\">somedir2</a></li><li><a href=\"/somedir3\" title=\"somedir3\">somedir3</a></li><li><a href=\"/testCompressionSuffix.html\" title=\"testCompressionSuffix.html\">testCompressionSuffix.html</a></li></ul>\n" +
        "</body>\n" +
        "</html>");
  }

  @Test
  public void testGetDirectoryOnSubdirMount() throws Exception {
    router.clear();
    router.route("/c/*").handler(stat);

    testRequest(HttpMethod.GET, "/c/a/b/", req -> req.putHeader("Accept", "text/html"), null, 200, "OK", "<html>\n" +
        "<body>\n" +
        "<h1>Custom Index of /c/a/b/</h1>\n" +
        "<a href=\"/c/a/\">..</a>\n" +
        "<ul id=\"files\"><li><a href=\"/c/a/b/test.txt\" title=\"test.txt\">test.txt</a></li></ul>\n" +
        "</body>\n" +
        "</html>");
  }

  @Test
  public void testGetDirectoryOnPrefixMount() throws Exception {
    router.clear();
    router.route("/c*").handler(stat);

    // even though the prefix is matched only the prefix is ignored from the file system match
    // webroot/annot/a/b will not be found
    testRequest(HttpMethod.GET, "/cannot/a/b/", req -> req.putHeader("Accept", "text/html"), null, 404, "Not Found", null);
  }
}
