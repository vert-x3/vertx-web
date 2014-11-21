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

package io.vertx.ext.apex.middleware.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.middleware.Static;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticTest extends ApexTestBase {

  private final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
  {
    DATE_TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    Static stat = Static.staticSite("testwebroot");
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Index page</body></html>");
  }

  @Test
  public void testGetOtherIndex() throws Exception {
    Static stat = Static.staticSite("testwebroot").setIndexPage("otherpage.html");
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testGetOtherPage() throws Exception {
    Static stat = Static.staticSite("testwebroot");
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/otherpage.html", 200, "OK", "<html><body>Other page</body></html>");
  }

  @Test
  public void testBadPathNoLeadingSlash() throws Exception {
    Static stat = Static.staticSite("testwebroot");
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "otherpage.html", 404, "Not Found");
  }

  @Test
  public void testDateHeaderSet() throws Exception {
    Static stat = Static.staticSite("testwebroot");
    router.route().handler(stat);
    testRequest(HttpMethod.GET, "/otherpage.html", null, res -> {
      String dateHeader = res.headers().get("date");
      assertNotNull(dateHeader);
      try {
        Date date = DATE_TIME_FORMATTER.parse(dateHeader);
        long diff = System.currentTimeMillis() - date.getTime();
        assertTrue(diff > 0 && diff < 2000);
      } catch (Exception e) {
        fail(e.getMessage());
      }
    }, 200, "OK", null);
  }

  // Test all the params including invalid values


}
