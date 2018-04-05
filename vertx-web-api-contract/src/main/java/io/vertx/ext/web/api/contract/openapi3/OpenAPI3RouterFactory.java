package io.vertx.ext.web.api.contract.openapi3;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactory;
import io.vertx.ext.web.api.contract.RouterFactoryException;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenAPI3RouterFactoryImpl;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * Interface for OpenAPI3RouterFactory. <br/>
 * To add an handler, use {@link OpenAPI3RouterFactory#addHandlerByOperationId(String, Handler)}<br/>
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
public interface OpenAPI3RouterFactory extends RouterFactory<OpenAPI> {

  /**
   * Add a particular scope validator. The main security schema will not be called if a specific scope validator is
   * configured
   *
   * @param securitySchemaName
   * @param scopeName
   * @param handler
   * @return this factory
   */
  @Fluent
  OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName,
                                                        Handler<RoutingContext> handler);

  /**
   * Add an handler by operation_id field in Operation object
   *
   * @param operationId
   * @param handler
   * @return this factory
   */
  @Fluent
  OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler);

  /**
   * Add a failure handler by operation_id field in Operation object
   *
   * @param operationId
   * @param failureHandler
   * @return this factory
   */
  @Fluent
  OpenAPI3RouterFactory addFailureHandlerByOperationId(String operationId, Handler<RoutingContext> failureHandler);

  @Fluent
  OpenAPI3RouterFactory mountAllOperationsToEventBus();

  @Fluent
  OpenAPI3RouterFactory mountOperationToEventBus(String operationId, String address);

  /**
   * Create a new OpenAPI3RouterFactory
   *
   * @param vertx
   * @param url location of your spec. It can be an absolute path, a local path or remote url (with HTTP protocol)
   * @param handler  When specification is loaded, this handler will be called with AsyncResult<OpenAPI3RouterFactory>
   */
  static void create(Vertx vertx, String url, Handler<AsyncResult<OpenAPI3RouterFactory>>
    handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readLocation(url, null, OpenApi3Utils.getParseOptions());
      if (swaggerParseResult.getMessages().isEmpty()) {
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, swaggerParseResult.getOpenAPI()));
      } else {
        if (swaggerParseResult.getMessages().size() == 1 && swaggerParseResult.getMessages().get(0).matches("unable to read location `?\\Q" + url + "\\E`?"))
          future.fail(RouterFactoryException.createSpecNotExistsException(url));
        else
          future.fail(RouterFactoryException.createSpecInvalidException(StringUtils.join(swaggerParseResult.getMessages(), ", ")));
      }
    }, handler);
  }
}
