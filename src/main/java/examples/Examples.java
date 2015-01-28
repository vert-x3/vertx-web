package examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.core.Route;
import io.vertx.ext.apex.core.Router;

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

      // This will match if the request is sent with an `accept` header with the values like:

      // `application/json`
      // `application/*`
      // `*/json`
      // `*/*`


      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "application/json");
      response.write(someJSON).end();
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
}

