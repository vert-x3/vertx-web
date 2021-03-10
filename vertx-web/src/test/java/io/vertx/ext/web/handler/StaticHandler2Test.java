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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.impl.Utils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class StaticHandler2Test extends WebTestBase {

  protected StaticHandler stat;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    stat = StaticHandler.create();
    router.route("/static/*").handler(stat);
  }

  @Test
  public void testGetDefaultIndex() throws Exception {
    // without slash... forwards to slash
    testRequest(HttpMethod.GET, "/static/swaggerui", null, res-> {
      assertEquals("/static/swaggerui/", res.getHeader("Location"));
    }, 301, "Moved Permanently", null);

    // with slash... forward to index.html
    testRequest(HttpMethod.GET, "/static/swaggerui/", null, res-> {
      assertEquals("/static/swaggerui/index.html", res.getHeader("Location"));
    }, 301, "Moved Permanently", null);

    // index.html retreives the final file
    testRequest(HttpMethod.GET, "/static/swaggerui/index.html", 200, "OK", "<html><body>Fake swagger UI</body></html>\n");
  }
}
