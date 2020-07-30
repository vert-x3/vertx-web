package io.vertx.ext.web.validation.testutils;

import io.vertx.core.Vertx;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.json.schema.openapi3.OpenAPI3SchemaParser;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;


//TODO not working because I can't retrieve Vertx object from VertxExtension :(
public class JsonSchemaValidatorExtension implements ParameterResolver {

  private static String ROUTER = "router";
  private static String DRAFT7_PARSER = "draft7parser";
  private static String OPENAPI3_PARSER = "openapi3parser";

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return type.equals(SchemaParser.class) || type.equals(SchemaRouter.class) || type.equals(Draft7SchemaParser.class) || type.equals(OpenAPI3SchemaParser.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(JsonSchemaValidatorExtension.class, extensionContext));
    if (SchemaRouter.class.equals(type))
      return getRouter(extensionContext, store);
    if (Draft7SchemaParser.class.equals(type) || SchemaParser.class.equals(type))
      return store.getOrComputeIfAbsent(
        DRAFT7_PARSER,
        s -> Draft7SchemaParser.create(getRouter(extensionContext, store)),
        Draft7SchemaParser.class
      );
    if (OpenAPI3SchemaParser.class.equals(type))
      return store.getOrComputeIfAbsent(
        OPENAPI3_PARSER,
        s -> OpenAPI3SchemaParser.create(getRouter(extensionContext, store)),
        OpenAPI3SchemaParser.class
      );
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }

  private SchemaRouter getRouter(ExtensionContext extensionContext, ExtensionContext.Store myStore) {
    Vertx vertx = extensionContext.getStore(ExtensionContext.Namespace.create(VertxExtension.class, extensionContext)).get("VertxInstance", Vertx.class);
    return myStore.getOrComputeIfAbsent(ROUTER, s -> SchemaRouter.create(vertx, new SchemaRouterOptions()), SchemaRouter.class);
  }
}
