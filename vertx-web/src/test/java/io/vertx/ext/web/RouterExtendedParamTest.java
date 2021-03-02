/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.vertx.core.Future.succeededFuture;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RouterExtendedParamTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    System.setProperty("io.vertx.web.route.param.extended-pattern", "true");
    super.setUp();
    System.setProperty("io.vertx.web.route.param.extended-pattern", "false");
  }

  @Test
  public void testRouteDashVariable() throws Exception {
    router.route("/foo/:my-id").handler(rc -> {
      assertEquals("123", rc.pathParam("my-id"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/foo/123", 200, "OK");
  }
}
