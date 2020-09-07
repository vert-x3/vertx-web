package io.vertx.ext.web.openapi.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAPI3UtilsTest {

  @Test
  public void testOperationIdSanitizer() {
    assertThat(OpenAPI3Utils.sanitizeOperationId("operationId")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation id")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation Id")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation-id")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation_id")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation__id-")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation_- id ")).isEqualTo("operationId");
    assertThat(OpenAPI3Utils.sanitizeOperationId("operation_- A B")).isEqualTo("operationAB");
  }

}
