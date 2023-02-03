package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.contract.Operation;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.validation.ValidatedRequest;

public class RouterExamples {

  private OpenAPIContract getContract() {
    return null;
  }

  void createRouter(Vertx vertx) {
    OpenAPIContract contract = getContract();
    RouterBuilder routerBuilder = RouterBuilder.create(vertx, contract);
    Router router = routerBuilder.createRouter();
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
}
