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
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.WebTestBase;
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
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class StaticDirectoryListHandlerTest extends WebTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create("webroot").setDirectoryListing(true);
    router.route().handler(stat);
  }

  @Test
  public void testGetSubSubDirectory() throws Exception {
    testRequest(HttpMethod.GET, "/a/b/", req -> {
      req.putHeader("Accept", "text/html");
    }, null, 200, "OK", "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "    <meta charset='utf-8'>\n" +
      "    <title>listing directory /a/b/</title>\n" +
      "    <style>\n" +
      "        body {\n" +
      "            margin: 0;\n" +
      "            padding: 80px 100px;\n" +
      "            font: 13px \"Helvetica Neue\", \"Lucida Grande\", \"Arial\";\n" +
      "            background: #ECE9E9 -webkit-gradient(linear, 0% 0%, 0% 100%, from(#fff), to(#ECE9E9));\n" +
      "            background: #ECE9E9 -moz-linear-gradient(top, #fff, #ECE9E9);\n" +
      "            background-repeat: no-repeat;\n" +
      "            color: #555;\n" +
      "            -webkit-font-smoothing: antialiased;\n" +
      "        }\n" +
      "        h1, h2, h3 {\n" +
      "            margin: 0;\n" +
      "            font-size: 22px;\n" +
      "            color: #343434;\n" +
      "        }\n" +
      "        h1 em, h2 em {\n" +
      "            padding: 0 5px;\n" +
      "            font-weight: normal;\n" +
      "        }\n" +
      "        h1 {\n" +
      "            font-size: 60px;\n" +
      "        }\n" +
      "        h2 {\n" +
      "            margin-top: 10px;\n" +
      "        }\n" +
      "        h3 {\n" +
      "            margin: 5px 0 10px 0;\n" +
      "            padding-bottom: 5px;\n" +
      "            border-bottom: 1px solid #eee;\n" +
      "            font-size: 18px;\n" +
      "        }\n" +
      "        ul {\n" +
      "            margin: 0;\n" +
      "            padding: 0;\n" +
      "        }\n" +
      "        ul li {\n" +
      "            margin: 5px 0;\n" +
      "            padding: 3px 8px;\n" +
      "            list-style: none;\n" +
      "        }\n" +
      "        ul li:hover {\n" +
      "            cursor: pointer;\n" +
      "            color: #2e2e2e;\n" +
      "        }\n" +
      "        ul li .path {\n" +
      "            padding-left: 5px;\n" +
      "            font-weight: bold;\n" +
      "        }\n" +
      "        ul li .line {\n" +
      "            padding-right: 5px;\n" +
      "            font-style: italic;\n" +
      "        }\n" +
      "        ul li:first-child .path {\n" +
      "            padding-left: 0;\n" +
      "        }\n" +
      "        p {\n" +
      "            line-height: 1.5;\n" +
      "        }\n" +
      "        a {\n" +
      "            color: #555;\n" +
      "            text-decoration: none;\n" +
      "        }\n" +
      "        a:hover {\n" +
      "            color: #303030;\n" +
      "        }\n" +
      "        .directory h1 {\n" +
      "            margin-bottom: 15px;\n" +
      "            font-size: 18px;\n" +
      "        }\n" +
      "        ul#files {\n" +
      "            width: 100%;\n" +
      "            height: 500px;\n" +
      "        }\n" +
      "        ul#files li {\n" +
      "            padding: 0;\n" +
      "        }\n" +
      "        ul#files li img {\n" +
      "            position: absolute;\n" +
      "            top: 5px;\n" +
      "            left: 5px;\n" +
      "        }\n" +
      "        ul#files li a {\n" +
      "            position: relative;\n" +
      "            display: block;\n" +
      "            margin: 1px;\n" +
      "            width: 30%;\n" +
      "            height: 25px;\n" +
      "            line-height: 25px;\n" +
      "            text-indent: 8px;\n" +
      "            float: left;\n" +
      "            border: 1px solid transparent;\n" +
      "            -webkit-border-radius: 5px;\n" +
      "            -moz-border-radius: 5px;\n" +
      "            border-radius: 5px;\n" +
      "            overflow: hidden;\n" +
      "            text-overflow: ellipsis;\n" +
      "        }\n" +
      "        ul#files li a.icon {\n" +
      "            text-indent: 25px;\n" +
      "        }\n" +
      "        ul#files li a:focus,\n" +
      "        ul#files li a:hover {\n" +
      "            outline: none;\n" +
      "            background: rgba(255,255,255,0.65);\n" +
      "            border: 1px solid #ececec;\n" +
      "        }\n" +
      "        ul#files li a.highlight {\n" +
      "            -webkit-transition: background .4s ease-in-out;\n" +
      "            background: #ffff4f;\n" +
      "            border-color: #E9DC51;\n" +
      "        }\n" +
      "    </style>\n" +
      "</head>\n" +
      "<body class=\"directory\">\n" +
      "<div id=\"wrapper\">\n" +
      "    <h1>Index of /a/b/</h1>\n" +
      "    <a href=\"/a/\">..</a>\n" +
      "    <ul id=\"files\"><li><a href=\"/a/b/test.txt\" title=\"test.txt\">test.txt</a></li></ul>\n" +
      "</div>\n" +
      "</body>\n" +
      "</html>");
  }

  @Test
  public void testGetDirectory() throws Exception {
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("Accept", "text/html");
    }, null, 200, "OK", "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "    <meta charset='utf-8'>\n" +
      "    <title>listing directory /</title>\n" +
      "    <style>\n" +
      "        body {\n" +
      "            margin: 0;\n" +
      "            padding: 80px 100px;\n" +
      "            font: 13px \"Helvetica Neue\", \"Lucida Grande\", \"Arial\";\n" +
      "            background: #ECE9E9 -webkit-gradient(linear, 0% 0%, 0% 100%, from(#fff), to(#ECE9E9));\n" +
      "            background: #ECE9E9 -moz-linear-gradient(top, #fff, #ECE9E9);\n" +
      "            background-repeat: no-repeat;\n" +
      "            color: #555;\n" +
      "            -webkit-font-smoothing: antialiased;\n" +
      "        }\n" +
      "        h1, h2, h3 {\n" +
      "            margin: 0;\n" +
      "            font-size: 22px;\n" +
      "            color: #343434;\n" +
      "        }\n" +
      "        h1 em, h2 em {\n" +
      "            padding: 0 5px;\n" +
      "            font-weight: normal;\n" +
      "        }\n" +
      "        h1 {\n" +
      "            font-size: 60px;\n" +
      "        }\n" +
      "        h2 {\n" +
      "            margin-top: 10px;\n" +
      "        }\n" +
      "        h3 {\n" +
      "            margin: 5px 0 10px 0;\n" +
      "            padding-bottom: 5px;\n" +
      "            border-bottom: 1px solid #eee;\n" +
      "            font-size: 18px;\n" +
      "        }\n" +
      "        ul {\n" +
      "            margin: 0;\n" +
      "            padding: 0;\n" +
      "        }\n" +
      "        ul li {\n" +
      "            margin: 5px 0;\n" +
      "            padding: 3px 8px;\n" +
      "            list-style: none;\n" +
      "        }\n" +
      "        ul li:hover {\n" +
      "            cursor: pointer;\n" +
      "            color: #2e2e2e;\n" +
      "        }\n" +
      "        ul li .path {\n" +
      "            padding-left: 5px;\n" +
      "            font-weight: bold;\n" +
      "        }\n" +
      "        ul li .line {\n" +
      "            padding-right: 5px;\n" +
      "            font-style: italic;\n" +
      "        }\n" +
      "        ul li:first-child .path {\n" +
      "            padding-left: 0;\n" +
      "        }\n" +
      "        p {\n" +
      "            line-height: 1.5;\n" +
      "        }\n" +
      "        a {\n" +
      "            color: #555;\n" +
      "            text-decoration: none;\n" +
      "        }\n" +
      "        a:hover {\n" +
      "            color: #303030;\n" +
      "        }\n" +
      "        .directory h1 {\n" +
      "            margin-bottom: 15px;\n" +
      "            font-size: 18px;\n" +
      "        }\n" +
      "        ul#files {\n" +
      "            width: 100%;\n" +
      "            height: 500px;\n" +
      "        }\n" +
      "        ul#files li {\n" +
      "            padding: 0;\n" +
      "        }\n" +
      "        ul#files li img {\n" +
      "            position: absolute;\n" +
      "            top: 5px;\n" +
      "            left: 5px;\n" +
      "        }\n" +
      "        ul#files li a {\n" +
      "            position: relative;\n" +
      "            display: block;\n" +
      "            margin: 1px;\n" +
      "            width: 30%;\n" +
      "            height: 25px;\n" +
      "            line-height: 25px;\n" +
      "            text-indent: 8px;\n" +
      "            float: left;\n" +
      "            border: 1px solid transparent;\n" +
      "            -webkit-border-radius: 5px;\n" +
      "            -moz-border-radius: 5px;\n" +
      "            border-radius: 5px;\n" +
      "            overflow: hidden;\n" +
      "            text-overflow: ellipsis;\n" +
      "        }\n" +
      "        ul#files li a.icon {\n" +
      "            text-indent: 25px;\n" +
      "        }\n" +
      "        ul#files li a:focus,\n" +
      "        ul#files li a:hover {\n" +
      "            outline: none;\n" +
      "            background: rgba(255,255,255,0.65);\n" +
      "            border: 1px solid #ececec;\n" +
      "        }\n" +
      "        ul#files li a.highlight {\n" +
      "            -webkit-transition: background .4s ease-in-out;\n" +
      "            background: #ffff4f;\n" +
      "            border-color: #E9DC51;\n" +
      "        }\n" +
      "    </style>\n" +
      "</head>\n" +
      "<body class=\"directory\">\n" +
      "<div id=\"wrapper\">\n" +
      "    <h1>Index of /</h1>\n" +
      "    <a href=\"/\">..</a>\n" +
      "    <ul id=\"files\"><li><a href=\"/.hidden.html\" title=\".hidden.html\">.hidden.html</a></li><li><a href=\"/a\" title=\"a\">a</a></li><li><a href=\"/file with spaces.html\" title=\"file with spaces.html\">file with spaces.html</a></li><li><a href=\"/foo.json\" title=\"foo.json\">foo.json</a></li><li><a href=\"/index.html\" title=\"index.html\">index.html</a></li><li><a href=\"/otherpage.html\" title=\"otherpage.html\">otherpage.html</a></li><li><a href=\"/somedir\" title=\"somedir\">somedir</a></li><li><a href=\"/somedir2\" title=\"somedir2\">somedir2</a></li></ul>\n" +
      "</div>\n" +
      "</body>\n" +
      "</html>");
  }
}
