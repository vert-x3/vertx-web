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
import io.vertx.core.http.*;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.tests.WebTestBase;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandler2Test extends WebTestBase {

  protected StaticHandler stat;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    super.setUp(vertx);
    stat = StaticHandler.create();
    router.route("/static/*").handler(stat);
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    // without slash... forwards to slash
    HttpResponse<Buffer> resp = testRequest(webClient.get("/static/swaggerui").followRedirects(false).send(), 301, "Moved Permanently");
    assertEquals("/static/swaggerui/", resp.getHeader("Location"));

    String expected = "<html><body>Fake swagger UI</body></html>\n";
    Buffer response = testRequest(HttpMethod.GET, "/static/swaggerui/", 200, "OK").body();
    assertEquals(expected, normalizeLineEndingsFor(response).toString());

    // also index.html retreives the final file
    response = testRequest(HttpMethod.GET, "/static/swaggerui/index.html", 200, "OK").body();
    assertEquals(expected, normalizeLineEndingsFor(response).toString());
  }
}
