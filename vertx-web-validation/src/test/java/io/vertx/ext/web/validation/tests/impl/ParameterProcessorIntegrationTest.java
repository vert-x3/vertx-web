package io.vertx.ext.web.validation.tests.impl;

import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.builder.Parameters;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.ext.web.validation.tests.testutils.TestSchemas;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.SchemaRepository;
import io.vertx.json.schema.ValidationException;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@VertxTest
@ExtendWith(MockitoExtension.class)
public class ParameterProcessorIntegrationTest {
  private SchemaRepository repository;

  @BeforeEach
  public void setUp() {
    repository = SchemaRepository.create(new JsonSchemaOptions().setDraft(Draft.DRAFT7).setBaseUri("app://"));
  }

  @Test
  public void testJsonParam() {
    ParameterProcessor processor = Parameters
      .jsonParam("myParam", TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER)
      .create(ParameterLocation.QUERY, repository);

    Map<String, List<String>> map = new HashMap<>();
    map.put("myParam", Collections.singletonList(TestSchemas.VALID_OBJECT.encode()));

    RequestParameter rp = processor.process(map).await();
    assertThat(rp.isJsonObject()).isTrue();
    assertThat(rp.getJsonObject())
      .isEqualTo(
        TestSchemas.VALID_OBJECT
      );
  }

  @Test
  public void testInvalidJsonParam() {
    ParameterProcessor processor = Parameters
      .jsonParam("myParam", TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER)
      .create(ParameterLocation.QUERY, repository);

    Map<String, List<String>> map = new HashMap<>();
    map.put("myParam", Collections.singletonList(TestSchemas.INVALID_OBJECT.encode()));

    try {
      processor.process(map).await();
      fail();
    } catch (Exception err) {
      assertThat(err)
        .isInstanceOf(ParameterProcessorException.class)
        .hasFieldOrPropertyWithValue("errorType",
          ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR)
        .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
        .hasFieldOrPropertyWithValue("parameterName", "myParam")
        .hasCauseInstanceOf(ValidationException.class);
    }
  }
}
