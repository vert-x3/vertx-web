package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.ValidationException;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.testutils.TestSchemas;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.vertx.ext.json.schema.draft7.dsl.Schemas.schema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
class JsonBodyProcessorImplTest {

  SchemaRouter router;
  SchemaParser parser;

  @Mock RoutingContext mockedContext;
  @Mock HttpServerRequest mockerServerRequest;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

  @Test
  public void testContentTypeCheck() {
    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);
    assertThat(processor.canProcess("application/json")).isTrue();
    assertThat(processor.canProcess("application/json; charset=utf-8")).isTrue();
    assertThat(processor.canProcess("application/superapplication+json")).isTrue();
  }

  @Test
  public void testJsonObject(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.VALID_OBJECT.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.succeeding(rp -> {
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
  public void testInvalidJsonObject(VertxTestContext testContext) {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.getBody()).thenReturn(TestSchemas.INVALID_OBJECT.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_OBJECT_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.failing(err -> {
      testContext.verify(() -> {
        assertThat(err)
          .isInstanceOf(BodyProcessorException.class)
          .hasFieldOrPropertyWithValue("actualContentType", "application/json")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testJsonArray(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(TestSchemas.VALID_ARRAY.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isJsonArray()).isTrue();
        assertThat(rp.getJsonArray())
          .isEqualTo(
            TestSchemas.VALID_ARRAY
          );
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testInvalidJsonArray(VertxTestContext testContext) {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.getBody()).thenReturn(TestSchemas.INVALID_ARRAY.toBuffer());

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).setHandler(testContext.failing(err -> {
      testContext.verify(() -> {
        assertThat(err)
          .isInstanceOf(BodyProcessorException.class)
          .hasFieldOrPropertyWithValue("actualContentType", "application/json")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testMalformedJson() {
    when(mockerServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/json");
    when(mockedContext.request()).thenReturn(mockerServerRequest);
    when(mockedContext.getBody()).thenReturn(Buffer.buffer("{\"a"));

    BodyProcessor processor = Bodies.json(TestSchemas.SAMPLE_ARRAY_SCHEMA_BUILDER).create(parser);

    assertThatCode(() -> processor.process(mockedContext))
      .isInstanceOf(BodyProcessorException.class)
      .hasFieldOrPropertyWithValue("actualContentType", "application/json")
      .hasCauseInstanceOf(DecodeException.class);
  }

  @Test
  public void testNull(VertxTestContext testContext) {
    when(mockedContext.getBody()).thenReturn(Buffer.buffer("null"));

    BodyProcessor processor = Bodies.json(schema().withKeyword("type", "null")).create(parser);

    processor.process(mockedContext).setHandler(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isNull()).isTrue();
      });
      testContext.completeNow();
    }));
  }
}
