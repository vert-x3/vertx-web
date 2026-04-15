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
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.tests.WebTestBase;
import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandler3Test extends WebTestBase {

  protected StaticHandler stat;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
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
    HttpResponse<Buffer> resp = testRequest(webClient.get("/somedir").followRedirects(false).send(), 301, "Moved Permanently");
    assertEquals("/somedir/", resp.getHeader("Location"));

    testRequest(HttpMethod.GET, "/somedir/", 200, "OK", "<html><body>Subdirectory index page</body></html>");
  }
}
