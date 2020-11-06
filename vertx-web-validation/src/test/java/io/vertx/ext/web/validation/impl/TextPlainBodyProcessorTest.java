package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.testutils.TestSchemas;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.draft7.Draft7SchemaParser;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TextPlainBodyProcessorTest {

  SchemaRouter router;
  SchemaParser parser;

  @Mock
  RoutingContext mockedContext;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

  @Test
  public void testString(VertxTestContext testContext) {
    when(mockedContext.getBodyAsString()).thenReturn(TestSchemas.VALID_STRING);

    BodyProcessor processor = Bodies.textPlain(TestSchemas.SAMPLE_STRING_SCHEMA_BUILDER).create(parser);

    processor.process(mockedContext).onComplete(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isString()).isTrue();
        assertThat(rp.getString())
          .isEqualTo(
            TestSchemas.VALID_STRING
          );
      });
      testContext.completeNow();
    }));
  }

}
