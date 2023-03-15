package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RequestExtractor;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.ValidatedRequest;

public class RouterExamples {

  private OpenAPIContract getContract() {
    return null;
  }

  void createRouter(Vertx vertx) {
    OpenAPIContract contract = getContract();
    RouterBuilder routerBuilder = RouterBuilder.create(vertx, contract);
    Router router = routerBuilder.createRouter();

    // In case that a BodyHandler was applied before, it is necessary to pass a RequestExtractor
    RouterBuilder.create(vertx, contract, RequestExtractor.withBodyHandler());
  }

  void modifyRoutes(Vertx vertx, RouterBuilder routerBuilder) {
    OpenAPIRoute getPetsRoute = routerBuilder.getRoute("getPets");

    // Disables validation for this route.
    getPetsRoute.setDoValidation(false);

    for (OpenAPIRoute route : routerBuilder.getRoutes()) {
      // Access the operation object from the contract
      Operation operation = route.getOperation();

      // Add a custom handler
      route.addHandler(routingContext -> {
        // do something
      });

      // Add a failure handler
      route.addFailureHandler(routingContext -> {
        // do something
      });
    }
  }

  void accessValidatedRequest(Vertx vertx, RouterBuilder routerBuilder) {
    OpenAPIRoute putPetRoute = routerBuilder.getRoute("putPet");

    putPetRoute.addHandler(routingContext -> {
      ValidatedRequest validatedRequest =
        routingContext.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);

      validatedRequest.getBody(); // returns the body
      validatedRequest.getHeaders(); // returns the header
      // ..
      // ..
    });
  }

  void securityConfiguration(Vertx vertx, OpenAPIContract contract, AuthenticationProvider provider, JWTAuth providerJWT) {
    RouterBuilder routerBuilder = RouterBuilder.create(vertx, contract);

    // Add a security handler for "api_key" security scheme
    // configuration will be automatically read from the contract and applied
    // to the handler
    routerBuilder
      .security("api_key")
      .apiKeyHandler(APIKeyHandler.create(provider));

    // Add a security handler for "http" "bearer" security scheme
    routerBuilder
      .security("http_bearer")
      .httpHandler(JWTAuthHandler.create(providerJWT));

    // same as before for "basic" or "digest" security schemes
  }

  void securityConfigurationOauth2(Vertx vertx, OpenAPIContract contract, OAuth2Auth providerOAuth2) {
    RouterBuilder routerBuilder = RouterBuilder.create(vertx, contract);

    // Add a security handler for "oauth2" security scheme
    routerBuilder
      .security("oauth2")
      .oauth2Handler("/callback", flowsConfig -> {
        // flowsConfig is a JsonObject with the configuration for the flows
        // you can use it to create the "providerOAuth2" instance
        return OAuth2AuthHandler.create(
          vertx,
          providerOAuth2,
          // there should a relation between this origin and the callback above
          "https://my-application-server.com/callback");
      });
  }

  void securityConfigurationOpenIdConnect(Vertx vertx, OpenAPIContract contract) {
    RouterBuilder routerBuilder = RouterBuilder.create(vertx, contract);

    // Add a security handler for "openIdConnect" security scheme
    routerBuilder
      .security("openIdConnect")
      .openIdConnectHandler("/callback", discoveryUrl -> {
        return OpenIDConnectAuth.discover(
          vertx,
          new OAuth2Options()
            .setClientId("client-id")
            .setClientSecret("client-secret"))
          .map(openIdConnect -> {
            return OAuth2AuthHandler.create(
              vertx,
              openIdConnect,
              // there should a relation between this origin and the callback above
              "https://my-application-server.com/callback");
          });
      });

    // In this case, the configuration is fetched from the contract, leaving less
    // room for mistakes. While the example is using "OpenIDConnectAuth", you can
    // use any of the OAuth2 providers that may reduce even further the
    // configuration needs.
  }
}
