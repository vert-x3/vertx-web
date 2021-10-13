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
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandler3Test extends WebTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create();
    router.route("/*").handler(stat);
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    // without slash... at root defaults to /
    testRequest(HttpMethod.GET, "", 200, "OK", "<html><body>Index page</body></html>");

    // with slash
    testRequest(HttpMethod.GET, "/", 200, "OK", "<html><body>Index page</body></html>");

    // and directly
    testRequest(HttpMethod.GET, "/index.html", 200, "OK", "<html><body>Index page</body></html>");
  }

  @Test
  public void testGetDefaultIndexWithSlash() throws Exception {
    // with / returns index page
    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory index page</body></html>");
  }

  @Test
  public void testGetDefaultIndexWithoutSlash() throws Exception {
    // without / should redirect first
    testRequest(HttpMethod.GET, "/somedir", null, res-> {
      assertEquals("/somedir/", res.getHeader("Location"));
    }, 301, "Moved Permanently", null);

    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory index page</body></html>");
  }
}
