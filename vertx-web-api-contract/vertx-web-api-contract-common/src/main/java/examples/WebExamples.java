package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.validation.ParameterType;
import io.vertx.ext.web.validation.ValidationException;

import java.util.List;

/**
 * These are the examples used in the documentation.
 *
 */
public class WebExamples {

  public void example63(Vertx vertx, Router router) {
    // Create Validation Handler with some stuff
    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addQueryParam("parameterName", ParameterType.INT, true).addFormParamWithPattern("formParameterName", "a{4}", true).addPathParam("pathParam", ParameterType.FLOAT);
  }

  public void example64(Vertx vertx, Router router, HTTPRequestValidationHandler validationHandler) {
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

  public void example65(RoutingContext routingContext) {
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

  public void example66(RequestParameters params) {
    RequestParameter body = params.body();
    if (body != null) {
      JsonObject jsonBody = body.getJsonObject();
    }
  }

  public void manualContentType(Router router) {
    router.get("/api/books").produces("application/json").handler(rc -> {
      findBooks(ar -> {
        if (ar.succeeded()) {
          rc.response().putHeader("Content-Type", "application/json").end(toJson(ar.result()));
        } else {
          rc.fail(ar.cause());
        }
      });
    });
  }

  public void contentTypeHandler(Router router) {
    router.route("/api/*").handler(ResponseContentTypeHandler.create());
    router.get("/api/books").produces("application/json").handler(rc -> {
      findBooks(ar -> {
        if (ar.succeeded()) {
          rc.response().end(toJson(ar.result()));
        } else {
          rc.fail(ar.cause());
        }
      });
    });
  }

  private void findBooks(Handler<AsyncResult<List<Book>>> handler) {
    throw new UnsupportedOperationException();
  }

  class Book {
  }

  Buffer toJson(List<Book> books) {
    throw new UnsupportedOperationException();
  }

  Buffer toXML(List<Book> books) {
    throw new UnsupportedOperationException();
  }

  public void mostAcceptableContentTypeHandler(Router router) {
    router.route("/api/*").handler(ResponseContentTypeHandler.create());
    router.get("/api/books").produces("text/xml").produces("application/json").handler(rc -> {
      findBooks(ar -> {
        if (ar.succeeded()) {
          if (rc.getAcceptableContentType().equals("text/xml")) {
            rc.response().end(toXML(ar.result()));
          } else {
            rc.response().end(toJson(ar.result()));
          }
        } else {
          rc.fail(ar.cause());
        }
      });
    });
  }
}

