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
public class CSPHandlerTest extends WebTestBase {

  @Test
  public void testCSPDefault() throws Exception {
    router.route().handler(CSPHandler.create());
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      assertEquals("default-src 'self'", resp.getHeader("Content-Security-Policy"));
    }, 200, "OK", null);
  }

  @Test
  public void testCSPCustom() throws Exception {
    router.route().handler(CSPHandler.create().addDirective("default-src", "*.trusted.com"));
    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      assertEquals("default-src 'self' *.trusted.com", resp.getHeader("Content-Security-Policy"));
    }, 200, "OK", null);
  }

  @Test
  public void testCSPMulti() throws Exception {
    router.route().handler(
      CSPHandler.create()
        .addDirective("img-src", "*")
        .addDirective("media-src", "media1.com media2.com")
        .addDirective("script-src", "userscripts.example.com"));


    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String h = resp.getHeader("Content-Security-Policy");
      assertEquals("default-src 'self'; img-src *; media-src media1.com media2.com; script-src userscripts.example.com", h);
    }, 200, "OK", null);
  }

  @Test
  public void testCSPReporting() throws Exception {
    router.route().handler(
      CSPHandler.create()
        .setReportOnly(true)
        .addDirective("report-uri", "http://reportcollector.example.com/collector.cgi"));


    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String h = resp.getHeader("Content-Security-Policy-Report-Only");
      assertEquals("default-src 'self'; report-uri http://reportcollector.example.com/collector.cgi", h);
    }, 200, "OK", null);
  }

  @Test
  public void testCSPReportingWithoutUri() throws Exception {
    router.route().handler(
      CSPHandler.create()
        .setReportOnly(true));

    router.route().handler(context -> context.response().end());
    testRequest(HttpMethod.GET, "/", 500, "Internal Server Error");
  }
}
