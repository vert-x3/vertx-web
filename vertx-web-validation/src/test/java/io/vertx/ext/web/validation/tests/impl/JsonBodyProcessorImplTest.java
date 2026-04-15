package io.vertx.ext.web.validation.tests.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.tests.testutils.TestSchemas;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.SchemaRepository;
import io.vertx.json.schema.ValidationException;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.vertx.json.schema.common.dsl.Schemas.schema;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
class JsonBodyProcessorImplTest {
  private SchemaRepository repository;

  @Mock
  RoutingContext mockedContext;
  @Mock
  HttpServerRequest mockerServerRequest;
  @Mock
  RequestBody mockerRequestBody;

  @BeforeEach
  public void setUp() {
    repository = SchemaRepository.create(new JsonSchemaOptions().setDraft(Draft.DRAFT7).setBaseUri("app://"));
  }

  @Test
  public void testContentTypeCheck() {
    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(repository);
    assertThat(processor.canProcess("application/json")).isTrue();
    assertThat(processor.canProcess("application/json; charset=utf-8")).isTrue();
    assertThat(processor.canProcess("application/superapplication+json")).isTrue();
  }

  @Test
  public void testJsonObject() {
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(TestSchemas.VALID_OBJECT.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(repository);

    RequestParameter rp = processor.process(mockedContext).await();
    assertThat(rp.isJsonObject()).isTrue();
    assertThat(rp.getJsonObject())
      .isEqualTo(
        TestSchemas.VALID_OBJECT
      );
  }

  @Test
  public void testInvalidJsonObject() {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(TestSchemas.INVALID_OBJECT.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(repository);

    try {
      processor.process(mockedContext).await();
      fail();
    } catch (Exception err) {
      assertThat(err)
        .isInstanceOf(BodyProcessorException.class)
        .hasFieldOrPropertyWithValue("actualContentType", "application/json")
        .hasCauseInstanceOf(ValidationException.class);
    }
  }

  @Test
  public void testJsonArray() {
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(TestSchemas.VALID_ARRAY.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(repository);

    RequestParameter rp = processor.process(mockedContext).await();
    assertThat(rp.isJsonArray()).isTrue();
    assertThat(rp.getJsonArray())
      .isEqualTo(
        TestSchemas.VALID_ARRAY
      );
  }

  @Test
  public void testInvalidJsonArray() {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(TestSchemas.INVALID_ARRAY.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(repository);

    try {
      processor.process(mockedContext).await();
      fail();
    } catch (Exception err) {
      assertThat(err)
        .isInstanceOf(BodyProcessorException.class)
        .hasFieldOrPropertyWithValue("actualContentType", "application/json")
        .hasCauseInstanceOf(ValidationException.class);
    }
  }

  @Test
  public void testMalformedJson() {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(Buffer.buffer("{\"a"));

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(repository);

    assertThatCode(() -> processor.process(mockedContext))
      .isInstanceOf(BodyProcessorException.class)
      .hasFieldOrPropertyWithValue("actualContentType", "application/json")
      .hasCauseInstanceOf(DecodeException.class);
  }

  @Test
  public void testNull() {
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(Buffer.buffer("null"));

    BodyProcessor processor = Bodies.json(schema().withKeyword("type", "null")).create(repository);

    RequestParameter rp = processor.process(mockedContext).await();
    assertThat(rp.isNull()).isTrue();
  }

  @Test
  public void testNullBody() {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.body()).thenReturn(mockerRequestBody);
    when(mockerRequestBody.buffer()).thenReturn(null);

    BodyProcessor processor = Bodies.json(schema().withKeyword("type", "null")).create(repository);

    assertThatCode(() -> processor.process(mockedContext))
      .isInstanceOf(BodyProcessorException.class)
      .hasFieldOrPropertyWithValue("actualContentType", "application/json")
      .hasCauseInstanceOf(MalformedValueException.class);
  }
}
