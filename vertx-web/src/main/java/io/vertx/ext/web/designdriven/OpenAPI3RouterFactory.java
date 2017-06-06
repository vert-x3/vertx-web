package io.vertx.ext.web.designdriven;

import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
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
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface OpenAPI3RouterFactory extends DesignDrivenRouterFactory {

  @Fluent
  OpenAPI3RouterFactory addOAuth2ScopeValidator(String securitySchemaName, String scopeName, Handler<RoutingContext> handler);

  @Fluent
  OpenAPI3RouterFactory addHandlerByOperationId(String operationId, Handler<RoutingContext> handler, Handler<RoutingContext> failureHandler);

  public static void createRouterFactoryFromFile(Vertx vertx, String filename, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      OpenApi3 model = (OpenApi3) new OpenApiParser().parse(new File(filename), true);
      if (model.isValid())
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      else
        future.fail(RouterFactoryException.createSpecInvalidException(model.getValidationResults().toString()));
    }, handler);
  }

  public static void createRouterFactoryFromURL(Vertx vertx, String url, Handler<AsyncResult<OpenAPI3RouterFactory>> handler) {
    vertx.executeBlocking((Future<OpenAPI3RouterFactory> future) -> {
      OpenApi3 model = null;
      try {
        model = (OpenApi3) new OpenApiParser().parse(new URL(url), true);
      } catch (MalformedURLException e) {
        future.fail("Invalid url");
      }
      if (model.isValid())
        future.complete(new OpenAPI3RouterFactoryImpl(vertx, model));
      else
        future.fail(RouterFactoryException.createSpecInvalidException(model.getValidationResults().toString()));
    }, handler);
  }

}
