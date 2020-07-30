package io.vertx.ext.web.validation.testutils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.RequestPredicateException;
import io.vertx.ext.web.validation.impl.ParameterLocation;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationTestUtils {

  public static void mountRouterFailureHandler(Router router) {
    router.errorHandler(400, routingContext -> {
      Throwable failure = routingContext.failure();
      if (failure instanceof BadRequestException) {
        routingContext
          .response()
          .putHeader("content-type", "application/json")
          .setStatusCode(400)
          .end(((BadRequestException) failure).toJson().toBuffer());
      } else {
        failure.printStackTrace();
        routingContext.response().setStatusCode(500).setStatusMessage("Unknown failure: " + failure.toString()).end();
      }
    });
    router.errorHandler(500, routingContext -> {
      Throwable failure = routingContext.failure();
      failure.printStackTrace();
      routingContext.response().setStatusCode(500).setStatusMessage("Unknown failure: " + failure.toString()).end();
    });
  }

  public static Consumer<HttpResponse<Buffer>> badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType errorType) {
    return req -> {
      JsonObject obj = req.bodyAsJsonObject();
      assertThat(obj.getString("type")).isEqualTo(ParameterProcessorException.class.getSimpleName());
      assertThat(obj.getString("errorType")).isEqualTo(errorType.name());
    };
  }

  public static Consumer<HttpResponse<Buffer>> badParameterResponse(ParameterProcessorException.ParameterProcessorErrorType errorType, String parameterName, ParameterLocation location) {
    return req -> {
      JsonObject obj = req.bodyAsJsonObject();
      assertThat(obj.getString("type")).isEqualTo(ParameterProcessorException.class.getSimpleName());
      assertThat(obj.getString("errorType")).isEqualTo(errorType.name());
      assertThat(obj.getString("parameterName")).isEqualTo(parameterName);
      assertThat(obj.getString("location")).isEqualTo(location.name());
    };
  }

  public static Consumer<HttpResponse<Buffer>> badBodyResponse(BodyProcessorException.BodyProcessorErrorType errorType) {
    return req -> {
      JsonObject obj = req.bodyAsJsonObject();
      assertThat(obj.getString("type")).isEqualTo(BodyProcessorException.class.getSimpleName());
      assertThat(obj.getString("errorType")).isEqualTo(errorType.name());
    };
  }

  public static Consumer<HttpResponse<Buffer>> failurePredicateResponse() {
    return req -> {
      JsonObject obj = req.bodyAsJsonObject();
      assertThat(obj.getString("type")).isEqualTo(RequestPredicateException.class.getSimpleName());
    };
  }

}
