package examples;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.openapi.SecurityHandlerProvider;
import io.vertx.ext.web.openapi.OpenAPILoaderOptions;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

@Source
public class OpenAPI3Examples {

  public void constructRouterBuilder(Vertx vertx) {
    RouterBuilder.create(vertx, "src/main/resources/petstore.yaml")
      .onSuccess(routerBuilder -> {
        // Spec loaded with success
      })
      .onFailure(err -> {
        // Something went wrong during router builder initialization
      });
  }

  public void constructRouterBuilderFromUrl(Vertx vertx) {
    RouterBuilder.create(
      vertx,
      "https://raw.githubusercontent" +
        ".com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml")
      .onSuccess(routerBuilder -> {
        // Spec loaded with success
      })
      .onFailure(err -> {
        // Something went wrong during router builder initialization
      });
  }

  public void constructRouterBuilderFromUrlWithAuthenticationHeader(Vertx vertx) {
    OpenAPILoaderOptions loaderOptions = new OpenAPILoaderOptions()
      .putAuthHeader("Authorization", "Bearer xx.yy.zz");
    RouterBuilder.create(
      vertx,
      "https://raw.githubusercontent" +
        ".com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml",
      loaderOptions)
      .onSuccess(routerBuilder -> {
        // Spec loaded with success
      })
      .onFailure(err -> {
        // Something went wrong during router builder initialization
      });
  }

  public void setOptions(RouterBuilder routerBuilder) {
    routerBuilder.setOptions(new RouterBuilderOptions());
  }

  public void addRoute(Vertx vertx, RouterBuilder routerBuilder) {
    routerBuilder
      .operation("awesomeOperation")
      .handler(routingContext -> {
        RequestParameters params =
          routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter body = params.body();
        JsonObject jsonBody = body.getJsonObject();
        // Do something with body
      }).failureHandler(routingContext -> {
      // Handle failure
    });
  }

  public void authnFactoryBasic(RouterBuilder routerBuilder, AuthenticationProvider authProvider) {
    routerBuilder.createSecurityHandlers(
      SecurityHandlerProvider.create()
        .add(
        "basicAuth",
        config -> Future.succeededFuture(BasicAuthHandler.create(authProvider))))
      .onSuccess(v -> {
        // Creation completed with success
      })
      .onFailure(err -> {
        // Something went wrong
      });
  }

  public void authnFactoryApiKey(RouterBuilder routerBuilder, AuthenticationProvider authProvider) {
    routerBuilder.createSecurityHandlers(
      SecurityHandlerProvider.create()
        .add(
        "ApiKeyAuth",
        config ->
          Future.succeededFuture(
            APIKeyHandler.create(authProvider)
              .header(config.getString("name")))))
      .onSuccess(v -> {
        // Creation completed with success
      })
      .onFailure(err -> {
        // Something went wrong
      });
  }

  public void authnFactoryOidc(Vertx vertx, RouterBuilder routerBuilder) {
    routerBuilder.createSecurityHandlers(
      SecurityHandlerProvider.create()
      .add(
        "openId",
        config ->
          OpenIDConnectAuth
            .discover(vertx, new OAuth2Options()
              .setClientId("client-id") // user provided
              .setClientSecret("client-secret") // user provided
              .setSite(config.getString("openIdConnectUrl")))
          .compose(authProvider -> {
            AuthenticationHandler handler =
              OAuth2AuthHandler.create(vertx, authProvider);
            return Future.succeededFuture(handler);
          })))
      .onSuccess(v -> {
        // Creation completed with success
      })
      .onFailure(err -> {
        // Something went wrong
      });
  }

  public void addSecurityHandler(RouterBuilder routerBuilder,
                                 AuthenticationHandler authenticationHandler) {
    routerBuilder.securityHandler(
        "security_scheme_name",
        authenticationHandler);
  }

  public void addJWT(RouterBuilder routerBuilder, JWTAuth jwtAuthProvider) {
    routerBuilder.securityHandler("jwt_auth",
      JWTAuthHandler.create(jwtAuthProvider));
  }

  public void addOperationModelKey(RouterBuilder routerBuilder,
                                   RouterBuilderOptions options) {
    // Configure the operation model key and set options in router builder
    options.setOperationModelKey("operationModel");
    routerBuilder.setOptions(options);

    // Add an handler that uses the operation model
    routerBuilder
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

  public void generateRouter(Vertx vertx, RouterBuilder routerBuilder) {
    Router router = routerBuilder.createRouter();

    HttpServer server =
      vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost(
        "localhost"));
    server.requestHandler(router).listen();
  }

  public void mainExample(Vertx vertx, JWTAuth jwtAuth) {
    // Load the api spec. This operation is asynchronous
    RouterBuilder.create(vertx, "src/main/resources/petstore.yaml")
      .onSuccess(routerBuilder -> {
        // You can enable or disable different features of router builder using
        //RouterBuilderOptions
        RouterBuilderOptions options = new RouterBuilderOptions();
        // Set the options
        routerBuilder.setOptions(options);
        // Add an handler to operation listPets
        routerBuilder.operation("listPets").handler(routingContext -> {
          // Handle listPets operation
          routingContext.response().setStatusMessage("Called listPets").end();
        }).handler(routingContext -> { // Add a failure handler to the same
          // operation
          // This is the failure handler
          Throwable failure = routingContext.failure();
          if (failure instanceof BadRequestException)
            // Handle Validation Exception
            routingContext
              .response()
              .setStatusCode(400)
              .putHeader("content-type", "application/json")
              .end(((BadRequestException) failure).toJson().toBuffer());
        });

        // Add a security handler
        // Handle security here
        routerBuilder.securityHandler(
          "api_key",
          JWTAuthHandler.create(jwtAuth)
        );

        // Now you have to generate the router
        Router router = routerBuilder.createRouter();

        // Now you can use your Router instance
        HttpServer server =
          vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost(
            "localhost"));
        server.requestHandler(router).listen();
      }).onFailure(exception -> {
      // Something went wrong during router builder initialization
    });
  }

  public void subRouter(Vertx vertx, RouterBuilder routerBuilder) {
    Router global = Router.router(vertx);

    Router generated = routerBuilder.createRouter();
    global.mountSubRouter("/v1", generated);
  }
}
