package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.core.Route;
import io.vertx.ext.apex.core.Router;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Examples {

  public void example1(Vertx vertx) {
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(request -> {

      // This handler gets called for each request that arrives on the server
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");

      // Write to the response and end it
      response.end("Hello World!");
    });
    server.listen(8080);
  }

  public void example2(Vertx vertx) {
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(routingContext -> {
      // This handler will be called for every request
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain");
      // Write to the response and end it
      response.end("Hello World from Apex!");
    });
    server.requestHandler(router::accept);
    server.listen(8080);
  }

  public void example3(Router router) {

    Route route = router.route().path("/some/path/");
    route.handler(routingContext -> {
      // This handler will be called for any request with
      // a URI path that starts with `/some/path`
    });

  }

  public void example4(Router router) {

    Route route = router.route("/some/path/");
    route.handler(routingContext -> {
      // This handler will be called same as previous example
    });

  }

  public void example4_1(Router router) {

    Route route = router.route(HttpMethod.POST, "/catalogue/products/:productype/:productid/");
    route.handler(routingContext -> {
      String productType = routingContext.request().params().get("producttype");
      String productID = routingContext.request().params().get("productid");
    });

  }


  public void example5(Router router) {

    Route route = router.route().pathRegex(".*foo");
    route.handler(routingContext -> {
      // This handler will be called for:

      // /some/path/foo
      // /foo
      // /foo/bar/wibble/foo
      // /foo/bar

      // But not:
      // /bar/wibble
    });

  }

  public void example6(Router router) {

    Route route = router.routeWithRegex(".*foo");
    route.handler(routingContext -> {
      // This handler will be called same as previous example
    });

  }

  public void example6_1(Router router) {

    Route route = router.routeWithRegex(".*foo");
    // This regular expression matches paths that start with something like:
    // "/foo/bar" - where the "foo" is captured into param0 and the "bar" is captured into
    // param1
    route.pathRegex("\\/([^\\/]+)\\/([^\\/]+)").handler(routingContext -> {
      String productType = routingContext.request().params().get("param0");
      String productID = routingContext.request().params().get("param1");
    });

  }

  public void example7(Router router) {

    Route route = router.route().method(HttpMethod.POST);

    route.handler(routingContext -> {
      // This handler will be called for any POST request
    });

  }

  public void example8(Router router) {

    Route route = router.route(HttpMethod.POST, "/some/path/");

    route.handler(routingContext -> {
      // This handler will be called for any POST request to a URI path starting with /some/path/
    });

  }

  public void example8_1(Router router) {

    router.get().handler(routingContext -> {
      // Will be called for any GET request
    });

    router.get("/some/path/").handler(routingContext -> {
      // Will be called for any GET request to a path
      // starting with /some/path
    });

    router.getWithRegex(".*foo").handler(routingContext -> {
      // Will be called for any GET request to a path
      // ending with `foo`
    });

    // There are also equivalents to the above for PUT, POST, DELETE, HEAD and OPTIONS

  }

  public void example9(Router router) {

    Route route = router.route().method(HttpMethod.POST).method(HttpMethod.PUT);

    route.handler(routingContext -> {
      // This handler will be called for any POST or PUT request
    });

  }

  public void example10(Router router) {

    Route route1 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route1\n");
      // Now call the next matching route
      routingContext.next();
    });
    Route route2 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route2\n");
      // Now call the next matching route
      routingContext.next();
    });
    Route route3 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route3");
      // Now end the response
      routingContext.response().end();
    });

  }

  public void example11(Router router) {

    Route route1 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route1\n");
      // Now call the next matching route
      routingContext.next();
    });
    Route route2 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route2\n");
      // Now call the next matching route
      routingContext.next();
    });
    Route route3 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route3");
      // Now end the response
      routingContext.response().end();
    });
    // Change the order of route2 so it runs before route1
    route2.order(-1);

  }

  public void example12(Router router) {
    // Exact match
    router.route().consumes("text/html").handler(routingContext -> {
      // This handler will be called for any request with
      // content-type header set to `text/html`
    });
  }

  public void example13(Router router) {
    // Multiple exact matches
    router.route().consumes("text/html").consumes("text/plain").handler(routingContext -> {
      // This handler will be called for any request with
      // content-type header set to `text/html` or `text/plain`.
    });
  }

  public void example14(Router router) {
    // Sub-type wildcard match
    router.route().consumes("text/*").handler(routingContext -> {
      // This handler will be called for any request with top level type `text
      // e.g. content-type header set to `text/html` or `text/plain` will both match
    });
  }

  public void example15(Router router) {
    // Top level type wildcard match
    router.route().consumes("*/json").handler(routingContext -> {
      // This handler will be called for any request with sub-type json
      // e.g. content-type header set to `text/json` or `application/json` will both match
    });
  }

  public void example16(Router router, String someJSON) {

    router.route().produces("application/json").handler(routingContext -> {

      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "application/json");
      response.write(someJSON).end();
    });
  }

  public void example17(Router router, String whatever) {

    // This route can produce two different MIME types
    router.route().produces("application/json").produces("text/html").handler(routingContext -> {

      HttpServerResponse response = routingContext.response();

      // Get the actual MIME type acceptable
      String acceptableContentType = routingContext.getAcceptableContentType();

      response.putHeader("content-type", acceptableContentType);
      response.write(whatever).end();
    });
  }

  public void example18(Router router) {

    Route route = router.route(HttpMethod.PUT, "myapi/orders")
                        .consumes("application/json")
                        .produces("application/json");
    route.handler(routingContext -> {
      // This would be match for any PUT method to paths starting with "myapi/orders" with a content-type of "application/json"
      // and an accept header matching "application/json"
    });

  }

  public void example20(Router router) {

    Route route1 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route1\n");
      // Call the next matching route after a 5 second delay
      routingContext.vertx().setTimer(5000, tid -> routingContext.next());
    });
    Route route2 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route2\n");
      // Call the next matching route after a 5 second delay
      routingContext.vertx().setTimer(5000, tid ->  routingContext.next());
    });
    Route route3 = router.route("/some/path/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.write("route3");
      // Now end the response
      routingContext.response().end();
    });
    // Change the order of route2 so it runs before route1
    route2.order(-1);

  }

  public void example21(Router router) {

    router.get("/some/path").handler(routingContext -> {
      routingContext.put("foo", "bar");
      routingContext.next();
    });

    router.get("/some/path/other").handler(routingContext -> {
      String bar = routingContext.get("foo");
      // Do something with bar
      routingContext.response().end();
    });

  }

  public void example22(Vertx vertx, String productJSON) {
    Router restAPI = Router.router(vertx);

    restAPI.get("/products/:productID").handler(rc -> {
      // TODO Handle the lookup of the product....
      rc.response().write(productJSON);
    });
    restAPI.put("/products/:productID").handler(rc -> {
      // TODO Add a new product...
      rc.response().end();
    });
    restAPI.delete("/products/:productID").handler(rc -> {
      // TODO delete the product...
      rc.response().end();
    });
  }

  public void example23(Vertx vertx, Handler<RoutingContext> myStaticHandler, Handler<RoutingContext> myTemplateHandler) {
    Router mainRouter = Router.router(vertx);

    // Handle static resources
    mainRouter.route("/static").handler(myStaticHandler);

    mainRouter.route(".*\\.templ").handler(myTemplateHandler);
  }

  interface MyLookupService {
    void lookupProduct(String productID, Handler<AsyncResult<String>> resultHandler);
  }

  public void example24(Router mainRouter, Router restAPI) {
    mainRouter.mountSubRouter("/productsAPI", restAPI);
  }

  public void example25(Router router) {

    Route route = router.get("/somepath/");

    route.failureHandler(frc -> {
      // This will be called for failures that occur
      // when routing requests to paths starting with
      // '/somepath'
    });
  }

  public void example26(Router router) {

    Route route1 = router.get("/somepath/path1/");
    route1.handler(routingContext -> {
      // Let's say this throws a RuntimeException
      throw new RuntimeException("something happened!");
    });

    Route route2 = router.get("/somepath/path2");
    route2.handler(routingContext -> {
      // This one deliberately fails the request passing in the status code
      // E.g. 403 - Forbidden
      routingContext.fail(403);
    });

    // Define a failure handler
    // This will get called for any failures in the above handlers
    Route route3 = router.get("/somepath/");
    route3.failureHandler(failureRoutingContext -> {
      int statusCode = failureRoutingContext.statusCode();
      // Status code will be 500 for the RuntimeException or 403 for the other failure
      HttpServerResponse response = failureRoutingContext.response();
      response.setStatusCode(statusCode).end("Sorry! Not today");
    });

  }

}

