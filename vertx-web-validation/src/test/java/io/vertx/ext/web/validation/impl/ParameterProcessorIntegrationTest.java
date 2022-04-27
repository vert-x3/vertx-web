package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.builder.Parameters;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.testutils.TestSchemas;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.ValidationException;
import io.vertx.json.schema.validator.Draft;
import io.vertx.json.schema.validator.JsonSchemaOptions;
import io.vertx.json.schema.validator.SchemaRepository;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ParameterProcessorIntegrationTest {

  SchemaRouter router;
  SchemaRepository repository;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    repository = SchemaRepository.create(
      new JsonSchemaOptions()
        .setDraft(Draft.DRAFT7)
        .setBaseUri("app://"));
  }

  @Test
  public void testJsonParam(VertxTestContext testContext) {
    ParameterProcessor processor = Parameters
      .jsonParam("myParam", TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER)
      .create(ParameterLocation.QUERY, repository);

    Map<String, List<String>> map = new HashMap<>();
    map.put("myParam", Collections.singletonList(TestSchemas.VALID_OBJECT.encode()));

    processor.process(map).onComplete(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isJsonObject()).isTrue();
        assertThat(rp.getJsonObject())
          .isEqualTo(
            TestSchemas.VALID_OBJECT
          );
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testInvalidJsonParam(VertxTestContext testContext) {
    ParameterProcessor processor = Parameters
      .jsonParam("myParam", TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER)
      .create(ParameterLocation.QUERY, repository);

    Map<String, List<String>> map = new HashMap<>();
    map.put("myParam", Collections.singletonList(TestSchemas.INVALID_OBJECT.encode()));

    processor.process(map).onComplete(testContext.failing(throwable -> {
      testContext.verify(() -> assertThat(throwable)
        .isInstanceOf(ParameterProcessorException.class)
        .hasFieldOrPropertyWithValue("errorType", ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR)
        .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
        .hasFieldOrPropertyWithValue("parameterName", "myParam")
        .hasCauseInstanceOf(ValidationException.class));
      testContext.completeNow();
    }));
  }

}
