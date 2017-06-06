package io.vertx.ext.web.designdriven;

import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.val.ValidationResults;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.designdriven.impl.OpenAPI3RouterFactoryImpl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Interface for OpenAPI3RouterFactory. <br/>
 * To add an handler, use {@link OpenAPI3RouterFactory#addHandlerByOperationId(String, Handler, Handler)}, in this class is better than generic {@link DesignDrivenRouterFactory#addHandler(HttpMethod, String, Handler, Handler)}<br/>
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
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface OpenAPI3RouterFactory extends DesignDrivenRouterFactory<OpenApi3> {

  /**
   * Add a particular scope validator. The main security schema will not be called if a specific scope validator is configured
   *
   * @param securitySchemaName
   * @param scopeName
   * @param handler
   * @return
   */
  @Fluent
  OpenAPI3RouterFactory addSecuritySchemaScopeValidator(String securitySchemaName, String scopeName, Handler<RoutingContext> handler);

  /**
   * Add an handler by operation_id field in Operation object
   * @param operationId
   * @param handler
   * @param failureHandler
   * @return
   */
  @Fluent
  OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler, Handler<RoutingContext> failureHandler);

  /**
   * Create a new OpenAPI3RouterFactory from a filename
   *
   * @param vertx
   * @param filename
   * @param handler  When specification is loaded, this handler will be called with AsyncResult<OpenAPI3RouterFactory>
   */
  static void createRouterFactoryFromFile(Vertx vertx, String filename, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      OpenApi3 model = (OpenApi3) new OpenApiParser().parse(new File(filename), true);
      if (model.isValid())
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      else {
        if (model.getValidationResults().getSeverity() == ValidationResults.Severity.ERROR || model.getValidationResults().getSeverity() == ValidationResults.Severity.MAX_SEVERITY)
          future.fail(RouterFactoryException.createSpecInvalidException(model.getValidationResults().toString()));
        else
          future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      }
    }, handler);
  }

  /**
   * Create a new OpenAPI3RouterFactory from an url
   *
   * @param vertx
   * @param url
   * @param handler When specification is loaded, this handler will be called with AsyncResult<OpenAPI3RouterFactory>
   */
  static void createRouterFactoryFromURL(Vertx vertx, String url, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      OpenApi3 model = null;
      try {
        model = (OpenApi3) new OpenApiParser().parse(new URL(url), true);
      } catch (MalformedURLException e) {
        future.fail("Invalid url");
      }
      if (model.isValid())
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      else {
        if (model.getValidationResults().getSeverity() == ValidationResults.Severity.ERROR || model.getValidationResults().getSeverity() == ValidationResults.Severity.MAX_SEVERITY)
          future.fail(RouterFactoryException.createSpecInvalidException(model.getValidationResults().toString()));
        else
          future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      }
    }, handler);
  }

}
