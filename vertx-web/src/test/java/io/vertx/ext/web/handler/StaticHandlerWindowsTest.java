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

public class StaticHandlerWindowsTest extends WebTestBase {

  @Test
  public void testEscapeToClasspathFromWildcard() throws Exception {
    router.clear();
    router.route("/*").handler(StaticHandler.create("www"));
    // attempt to escape to classpath, given that the handler is mounted on a wildcard,
    // reading the wildcard must return a sanitized path and therefore not allow to escape.
    testRequest(HttpMethod.GET, "/..\\.htdigest", 404, "Not Found");
  }

  @Test
  public void testEscapeToClasspathFromNull() throws Exception {
    router.clear();
    router.route().handler(StaticHandler.create("www"));
    // attempt to escape to classpath, given that the handler is mounted on a catch all path
    testRequest(HttpMethod.GET, "/..\\.htdigest", 404, "Not Found");
  }

  @Test
  public void testEscapeToClasspathFromRegEx() throws Exception {
    router.clear();
    router.routeWithRegex(".*").handler(StaticHandler.create("www"));
    // attempt to escape to classpath, given that the handler is mounted on a regex,
    testRequest(HttpMethod.GET, "/..\\.htdigest", 404, "Not Found");
  }

  @Test
  public void testEscapeToClasspathFromFixedPath() throws Exception {
    router.clear();
    router.routeWithRegex("/").handler(StaticHandler.create("www"));
    // attempt to escape to classpath, given that the handler is mounted on a regex,
    testRequest(HttpMethod.GET, "/..\\.htdigest", 404, "Not Found");
  }
}
