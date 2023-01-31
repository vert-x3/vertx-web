/*
 * Copyright (c) 2023, SAP SE
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.openapi.contract.Operation;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

class OpenAPIRouteImplTest {

  @Test
  void testGetters() {
    Operation mockedOperation = mock(Operation.class);
    OpenAPIRoute route = new OpenAPIRouteImpl(mockedOperation);

    assertThat(route.doValidation()).isTrue();
    assertThat(route.getOperation()).isEqualTo(mockedOperation);
    assertThat(route.getHandlers()).isEmpty();
    assertThat(route.getFailureHandlers()).isEmpty();
  }

  @Test
  void testAdders() {
    Handler<RoutingContext> dummyHandler = RoutingContext::next;
    Handler<RoutingContext> dummyFailureHandler = RoutingContext::next;
    assertThat(dummyHandler).isNotSameInstanceAs(dummyFailureHandler);

    OpenAPIRoute route = new OpenAPIRouteImpl(null);
    route.addHandler(dummyHandler);
    assertThat(route.getHandlers()).containsExactly(dummyHandler);
    route.addFailureHandler(dummyFailureHandler);
    assertThat(route.getFailureHandlers()).containsExactly(dummyFailureHandler);

    assertThat(route.doValidation()).isTrue();
    route.setDoValidation(false);
    assertThat(route.doValidation()).isFalse();
  }
}
