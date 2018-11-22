package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.api.validation.ValidationException;

/**
 * These are the examples used in the documentation.
 *
 */
public class ValidationExamples {

  public void example1(Vertx vertx, Router router) {
    // Create Validation Handler with some stuff
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addQueryParam("parameterName", ParameterType.INT, true).addFormParamWithPattern("formParameterName", "a{4}", true).addPathParam("pathParam", ParameterType.FLOAT);
  }

  public void example2(Vertx vertx, Router router, HTTPRequestValidationHandler validationHandler) {
    // BodyHandler is required to manage body parameters like forms or json body
    router.route().handler(BodyHandler.create());

    router.get("/awesome/:pathParam")
      // Mount validation handler
      .handler(validationHandler)
      //Mount your handler
      .handler((routingContext) -> {
        // Get Request parameters container
        RequestParameters params = routingContext.get("parsedParameters");

        // Get parameters
        Integer parameterName = params.queryParameter("parameterName").getInteger();
        String formParameterName = params.formParameter("formParameterName").getString();
        Float pathParam = params.pathParameter("pathParam").getFloat();
      })

      //Mount your failure handler
      .failureHandler((routingContext) -> {
        Throwable failure = routingContext.failure();
        if (failure instanceof ValidationException) {
          // Something went wrong during validation!
          String validationErrorMessage = failure.getMessage();
        }
      });
  }

  public void example3(RoutingContext routingContext) {
    RequestParameters params = routingContext.get("parsedParameters");
    RequestParameter awesomeParameter = params.queryParameter("awesomeParameter");
    if (awesomeParameter != null) {
      if (!awesomeParameter.isEmpty()) {
        // Parameter exists and isn't empty
        // ParameterTypeValidator mapped the parameter in equivalent language object
        Integer awesome = awesomeParameter.getInteger();
      } else {
        // Parameter exists, but it's empty
      }
    } else {
      // Parameter doesn't exist (it's not required)
    }
  }

  public void example4(RequestParameters params) {
    RequestParameter body = params.body();
    if (body != null) {
      JsonObject jsonBody = body.getJsonObject();
    }
  }
}

