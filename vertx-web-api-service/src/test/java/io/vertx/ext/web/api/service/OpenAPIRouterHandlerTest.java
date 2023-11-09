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

package io.vertx.ext.web.api.service;

import io.vertx.core.json.JsonObject;
import io.vertx.openapi.contract.Operation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.ext.web.api.service.OpenAPIRouterHandler.OPENAPI_EXTENSION;
import static io.vertx.ext.web.api.service.OpenAPIRouterHandler.OPENAPI_EXTENSION_ADDRESS;
import static io.vertx.ext.web.api.service.OpenAPIRouterHandler.OPENAPI_EXTENSION_METHOD_NAME;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenAPIRouterHandlerTest {
  @Test
  void testCreateNoEBConfig() {
    Operation operationMock = mockOperation(emptyMap());

    NullPointerException exception =
      assertThrows(NullPointerException.class, () -> OpenAPIRouterHandler.create(null, operationMock, null));
    String msg = "No eventbus configuration found for Operation: " + operationMock.getOperationId();
    assertThat(exception).hasMessageThat().isEqualTo(msg);
  }

  static Stream<Arguments> testInvalidEBConfig() {
    Arguments ebExtensionNoStringOrObject = Arguments.of("No String or Object", new JsonObject().put(OPENAPI_EXTENSION,
      1337));
    Arguments objectNoAddress = Arguments.of("Object, but no address", new JsonObject().put(OPENAPI_EXTENSION,
      new JsonObject()));
    Arguments objectNoMethod = Arguments.of("Object, but method is no String",
      new JsonObject().put(OPENAPI_EXTENSION,
        new JsonObject().put(OPENAPI_EXTENSION_ADDRESS, "foo").put(OPENAPI_EXTENSION_METHOD_NAME, 1337)));

    return Stream.of(ebExtensionNoStringOrObject, objectNoAddress, objectNoMethod);
  }

  @ParameterizedTest(name = "{index} EB config is invalid because: {0}")
  @MethodSource
  void testInvalidEBConfig(String cause, JsonObject model) {
    Operation operationMock = mockOperation(model.getMap());

    RuntimeException exception =
      assertThrows(RuntimeException.class, () -> OpenAPIRouterHandler.create(null, operationMock, null));
    String msg = "Invalid eventbus configuration found for Operation: " + operationMock.getOperationId();
    assertThat(exception).hasMessageThat().isEqualTo(msg);
  }

  Operation mockOperation(Map<String, Object> model) {
    Operation operationMock = mock(Operation.class);
    when(operationMock.getExtensions()).thenReturn(model);
    return operationMock;
  }
}
