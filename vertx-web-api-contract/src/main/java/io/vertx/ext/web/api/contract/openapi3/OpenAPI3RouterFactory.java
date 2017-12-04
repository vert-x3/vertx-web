package io.vertx.ext.web.api.contract.openapi3;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.DesignDrivenRouterFactory;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RouterFactoryImpl;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * Interface for OpenAPI3RouterFactory. <br/>
 * To add an handler, use {@link OpenAPI3RouterFactory#addHandlerByOperationId(String, Handler)}, in this
 * class is better than generic {@link DesignDrivenRouterFactory#addHandler(HttpMethod, String, Handler)}<br/>
 * If you want to use {@link DesignDrivenRouterFactory#addHandler(HttpMethod, String, Handler)} remember that <b>you have to pass path as declared in openapi specification</b>
 * Usage example:
 * <pre>
 * {@code
 * OpenAPI3RouterFactory.createRouterFactoryFromFile(vertx, "src/resources/spec.yaml", asyncResult -> {
 *  if (!asyncResult.succeeded()) {
 *     // IO failure or spec invalid
 *  } else {
 *     OpenAPI3RouterFactory routerFactory = asyncResult.result();
 *     routerFactory.addHandlerByOperationId("operation_id", routingContext -> {
 *        // Do something
 *     }, routingContext -> {
 *        // Do something with failure handler
 *     });
 *     Router router = routerFactory.getRouter();
 *  }
 * });
 * }
 * </pre>
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface OpenAPI3RouterFactory extends DesignDrivenRouterFactory<OpenAPI> {

  /**
   * Add a particular scope validator. The main security schema will not be called if a specific scope validator is
   * configured
   *
   * @param securitySchemaName
   * @param scopeName
   * @param handler
   * @return
   */
  @Fluent
  OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName,
                                                        Handler<RoutingContext> handler);

  /**
   * Add an handler by operation_id field in Operation object
   *
   * @param operationId
   * @param handler
   * @return
   */
  @Fluent
  OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler);

  /**
   * Add a failure handler by operation_id field in Operation object
   *
   * @param operationId
   * @param failureHandler
   * @return
   */
  @Fluent
  OpenAPI3RouterFactory addFailureHandlerByOperationId(String operationId, Handler<RoutingContext> failureHandler);

  /**
   * Create a new OpenAPI3RouterFactory from a filename
   *
   * @param vertx
   * @param filename
   * @param handler  When specification is loaded, this handler will be called with AsyncResult<OpenAPI3RouterFactory>
   */
  static void createRouterFactoryFromFile(Vertx vertx, String filename, Handler<AsyncResult<OpenAPI3RouterFactory>>
    handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      File spec = new File(filename);
      if (!spec.exists())
        future.fail(RouterFactoryException.createSpecNotExistsException(filename));

      ParseOptions options = new ParseOptions();
      options.setResolve(true);
      options.setResolveCombinators(false);
      options.setResolveFully(true);
      SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readLocation(spec.getAbsolutePath(), null, options);

      if (swaggerParseResult.getMessages().isEmpty()) future.complete(new OpenAPI3RouterFactoryImpl(vertx, swaggerParseResult.getOpenAPI()));
      else {
          future.fail(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(),", ")));
      }
    }, handler);
  }

  /**
   * Create a new OpenAPI3RouterFactory from an url
   *
   * @param vertx
   * @param url
   * @param handler  When specification is loaded, this handler will be called with AsyncResult<OpenAPI3RouterFactory>
   */
  static void createRouterFactoryFromURL(Vertx vertx, String url, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    createRouterFactoryFromFile(vertx, url, handler);
  }
}
