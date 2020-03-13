package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.ValidationException;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.builder.Parameters;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.testutils.TestSchemas;
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
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

  @Test
  public void testJsonParam(VertxTestContext testContext) {
    ParameterProcessor processor = Parameters
      .jsonParam("myParam", TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER)
      .create(ParameterLocation.QUERY, parser);

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
      .create(ParameterLocation.QUERY, parser);

    Map<String, List<String>> map = new HashMap<>();
    map.put("myParam", Collections.singletonList(TestSchemas.INVALID_OBJECT.encode()));

    processor.process(map).onComplete(testContext.failing(throwable -> {
      testContext.verify(() -> {
        assertThat(throwable)
          .isInstanceOf(ParameterProcessorException.class)
          .hasFieldOrPropertyWithValue("errorType", ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR)
          .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
          .hasFieldOrPropertyWithValue("parameterName", "myParam")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }

}
