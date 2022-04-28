package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;

public class NoopBodyProcessorGenerator implements BodyProcessorGenerator {

  public final static NoopBodyProcessorGenerator INSTANCE = new NoopBodyProcessorGenerator();

  @Override
  public boolean canGenerate(String mediaTypeName, JsonObject mediaTypeObject) {
    return true;
  }

  @Override
  public BodyProcessor generate(String mediaTypeName, JsonObject mediaTypeObject, JsonPointer mediaTypePointer,
                                GeneratorContext context) {
    // Noop body processor
    return new BodyProcessor() {
      @Override
      public boolean canProcess(String contentType) {
        return true;
      }

      @Override
      public RequestParameter process(RoutingContext requestContext) {
        return RequestParameter.create(requestContext.body().buffer());
      }
    };
  }
}
