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

import io.vertx.ext.web.WebTestBase;
import org.junit.Test;
import io.vertx.core.http.HttpMethod;

/**
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
public class MethodOverrideHandlerTest extends WebTestBase {

  private MethodOverrideHandler handlerWithSafeDowngrading = MethodOverrideHandler.create();
  private MethodOverrideHandler handlerWithoutSafeDowngrading = MethodOverrideHandler.create(false);

  @Test
  public void testFoo() {
    assertTrue("It is true", true);
  }

  @Test
  public void testOverridingSameMethod() throws Exception {
    testOverride(true, HttpMethod.GET, HttpMethod.GET, HttpMethod.GET);
  }

  @Test
  public void testSafeOverridingToIdempotent() throws Exception {
    testOverride(true, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.DELETE);
  }

  @Test
  public void testSafeOverridingToSafeFromNonIdempotent() throws Exception {
    testOverride(true, HttpMethod.POST, HttpMethod.GET, HttpMethod.GET);
  }

  @Test
  public void testSafeOverridingFromNonSafe() throws Exception {
    testOverride(true, HttpMethod.POST, HttpMethod.GET, HttpMethod.GET);
  }

  @Test
  public void testSafeOverridingFromIdempotentShouldFail() throws Exception {
    testOverride(true, HttpMethod.GET, HttpMethod.POST, HttpMethod.GET);
  }

  @Test
  public void testUnsafeOverridingFromIdempotent() throws Exception {
    testOverride(false, HttpMethod.GET, HttpMethod.POST, HttpMethod.POST);
  }

  private void testOverride(boolean safe, HttpMethod overridedMethod, HttpMethod overridingMethod, HttpMethod expectedMethod) throws Exception {
    MethodOverrideHandler overridingHandler = (safe) ? handlerWithSafeDowngrading : handlerWithoutSafeDowngrading;
    router.route().handler(overridingHandler);
    router.route().handler(rc -> {
      assertEquals(expectedMethod.name(), rc.request().method().name());
      rc.response().end();
    });
    testRequestWithMethodOverride(overridedMethod, overridingMethod);
  }

  private void testRequestWithMethodOverride(HttpMethod overridedMethod, HttpMethod overridingMethod) throws Exception {
    testRequest(overridedMethod, "/", req -> req.putHeader("x-http-method-override", overridingMethod.toString()), 200, "OK", null);
  }
}
