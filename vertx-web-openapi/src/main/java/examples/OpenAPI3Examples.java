package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;
import io.vertx.ext.web.openapi.RouterFactory;
import io.vertx.ext.web.openapi.RouterFactoryOptions;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

@Source
public class OpenAPI3Examples {

  public void constructRouterFactory(Vertx vertx) {
    RouterFactory.create(vertx, "src/main/resources/petstore.yaml", ar -> {
      if (ar.succeeded()) {
        // Spec loaded with success
        RouterFactory routerFactory = ar.result();
      } else {
        // Something went wrong during router factory initialization
        Throwable exception = ar.cause();
      }
    });
  }

  public void constructRouterFactoryFromUrl(Vertx vertx) {
    RouterFactory.create(
      vertx,
      "https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml",
      ar -> {
        if (ar.succeeded()) {
          // Spec loaded with success
          RouterFactory routerFactory = ar.result();
        } else {
          // Something went wrong during router factory initialization
          Throwable exception = ar.cause();
        }
      });
  }

  public void constructRouterFactoryFromUrlWithAuthenticationHeader(Vertx vertx) {
    OpenAPILoaderOptions loaderOptions = new OpenAPILoaderOptions()
      .putAuthHeader("Authorization", "Bearer xx.yy.zz");
    RouterFactory.create(
      vertx,
      "https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml",
      loaderOptions,
      ar -> {
        if (ar.succeeded()) {
          // Spec loaded with success
          RouterFactory routerFactory = ar.result();
        } else {
          // Something went wrong during router factory initialization
          Throwable exception = ar.cause();
        }
      });
  }

  public void setOptions(RouterFactory routerFactory) {
    routerFactory.setOptions(new RouterFactoryOptions());
  }

  public void addRoute(Vertx vertx, RouterFactory routerFactory) {
    routerFactory
      .operation("awesomeOperation")
      .handler(routingContext -> {
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter body = params.body();
        JsonObject jsonBody = body.getJsonObject();
        // Do something with body
      }).failureHandler(routingContext -> {
        // Handle failure
      });
  }

  public void addSecurityHandler(RouterFactory routerFactory, AuthenticationHandler authenticationHandler) {
    routerFactory.securityHandler("security_scheme_name", authenticationHandler);
  }

  public void addJWT(RouterFactory routerFactory, JWTAuth jwtAuthProvider) {
    routerFactory.securityHandler("jwt_auth", JWTAuthHandler.create(jwtAuthProvider));
  }

  public void addOperationModelKey(RouterFactory routerFactory, RouterFactoryOptions options) {
    // Configure the operation model key and set options in router factory
    options.setOperationModelKey("operationModel");
    routerFactory.setOptions(options);

    // Add an handler that uses the operation model
    routerFactory
      .operation("listPets")
      .handler(
        routingContext -> {
        JsonObject operation = routingContext.get("operationModel");

        routingContext
          .response()
          .setStatusCode(200)
          .setStatusMessage("OK")
          // Write the response with operation id "listPets"
          .end(operation.getString("operationId"));
    });
  }

  public void generateRouter(Vertx vertx, RouterFactory routerFactory) {
    Router router = routerFactory.createRouter();

    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    server.requestHandler(router).listen();
  }

  public void mainExample(Vertx vertx, JWTAuth jwtAuth) {
    // Load the api spec. This operation is asynchronous
    RouterFactory.create(vertx, "src/main/resources/petstore.yaml",
      routerFactoryAsyncResult -> {
      if (routerFactoryAsyncResult.succeeded()) {
        // Spec loaded with success, retrieve the router
        RouterFactory routerFactory = routerFactoryAsyncResult.result();
        // You can enable or disable different features of router factory using RouterFactoryOptions
        RouterFactoryOptions options = new RouterFactoryOptions();
        // Set the options
        routerFactory.setOptions(options);
        // Add an handler to operation listPets
        routerFactory.operation("listPets").handler(routingContext -> {
          // Handle listPets operation
          routingContext.response().setStatusMessage("Called listPets").end();
        }).handler(routingContext -> { // Add a failure handler to the same operation
          // This is the failure handler
          Throwable failure = routingContext.failure();
          if (failure instanceof BadRequestException)
            // Handle Validation Exception
            routingContext
              .response()
              .setStatusCode(400)
              .putHeader("content-type", "application/json")
              .end(((BadRequestException)failure).toJson().toBuffer());
        });

        // Add a security handler
        // Handle security here
        routerFactory.securityHandler(
          "api_key",
          JWTAuthHandler.create(jwtAuth)
        );

        // Now you have to generate the router
        Router router = routerFactory.createRouter();

        // Now you can use your Router instance
        HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
        server.requestHandler(router).listen();

      } else {
        // Something went wrong during router factory initialization
        Throwable exception = routerFactoryAsyncResult.cause();
      }
    });
  }
}
