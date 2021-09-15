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

package io.vertx.ext.web.it;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author Paulo Lopes
 */
public class RouterExtendedParamTest extends WebTestBase {

  @Test
  public void testRouteDashVariable() throws Exception {
    router.route("/foo/:my-id").handler(rc -> {
      assertEquals("123", rc.pathParam("my-id"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/foo/123", 200, "OK");
  }

  @Test
  public void testRouteDashVariableNOK() throws Exception {
    router.route("/flights/:from-:to").handler(rc -> {
      // from isn't set as the alphabet now includes -
      assertNull(rc.pathParam("from"));
      assertNotNull(rc.pathParam("from-"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/flights/LAX-SFO", 200, "OK");
  }
}
