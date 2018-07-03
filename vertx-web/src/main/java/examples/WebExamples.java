package examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.TemplateEngine;

import java.util.List;
import java.util.Set;

/**
 * These are the examples used in the documentation.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class WebExamples {

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
      response.end("Hello World from Vert.x-Web!");
    });

    server.requestHandler(router).listen(8080);

  }

  public void example3(Router router) {

    Route route = router.route().path("/some/path/");

    route.handler(routingContext -> {
      // This handler will be called for the following request paths:

      // `/some/path`
      // `/some/path/`
      // `/some/path//`
      //
      // but not:
      // `/some/path/subdir`
    });

  }

  public void example3_1(Router router) {

    Route route = router.route().path("/some/path/*");

    route.handler(routingContext -> {
      // This handler will be called for any path that starts with
      // `/some/path/`, e.g.

      // `/some/path`
      // `/some/path/`
      // `/some/path/subdir`
      // `/some/path/subdir/blah.html`
      //
      // but not:
      // `/some/bath`
    });

  }

  public void example4(Router router) {

    Route route = router.route("/some/path/*");

    route.handler(routingContext -> {
      // This handler will be called same as previous example
    });

  }

  public void example4_1(Router router) {

    Route route = router.route(HttpMethod.POST, "/catalogue/products/:producttype/:productid/");

    route.handler(routingContext -> {

      String productType = routingContext.request().getParam("producttype");
      String productID = routingContext.request().getParam("productid");

      // Do something with them...
    });

  }


  public void example5(Router router) {

    // Matches any path ending with 'foo'
    Route route = router.route().pathRegex(".*foo");

    route.handler(routingContext -> {

      // This handler will be called for:

      // /some/path/foo
      // /foo
      // /foo/bar/wibble/foo
      // /bar/foo

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

      String productType = routingContext.request().getParam("param0");
      String productID = routingContext.request().getParam("param1");

      // Do something with them...
    });

  }

  public void example6_2(Router router) {

    // This regular expression matches paths that start with something like: "/foo/bar"
    // It uses named regex groups to capture path params
    Route route = router.routeWithRegex("\\/(?<productType>[^\\/]+)\\/(?<productId>[^\\/]+)").handler(routingContext -> {

      String productType = routingContext.request().getParam("productType");
      String productID = routingContext.request().getParam("productId");

      // Do something with them...
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
      // enable chunked responses because we will be adding data as
      // we execute over other handlers. This is only required once and
      // only if several handlers do output.
      response.setChunked(true);

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
      // enable chunked responses because we will be adding data as
      // we execute over other handlers. This is only required once and
      // only if several handlers do output.
      response.setChunked(true);

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

      // This handler will be called for any request with top level type `text`
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

      // This would be match for any PUT method to paths starting with "myapi/orders" with a
      // content-type of "application/json"
      // and an accept header matching "application/json"

    });

  }

  public void example20(Router router) {

    Route route = router.route("/some/path/");
    route.handler(routingContext -> {

      HttpServerResponse response = routingContext.response();
      // enable chunked responses because we will be adding data as
      // we execute over other handlers. This is only required once and
      // only if several handlers do output.
      response.setChunked(true);

      response.write("route1\n");

      // Call the next matching route after a 5 second delay
      routingContext.vertx().setTimer(5000, tid -> routingContext.next());
    });

    route.handler(routingContext -> {

      HttpServerResponse response = routingContext.response();
      response.write("route2\n");

      // Call the next matching route after a 5 second delay
      routingContext.vertx().setTimer(5000, tid -> routingContext.next());
    });

    route.handler(routingContext -> {

      HttpServerResponse response = routingContext.response();
      response.write("route3");

      // Now end the response
      routingContext.response().end();
    });

  }

  public void example20_1(Router router, SomeLegacyService service) {

    router.route().blockingHandler(routingContext -> {

      // Do something that might take some time synchronously
      service.doSomethingThatBlocks();

      // Now call the next handler
      routingContext.next();

    });
  }

  public void example20_2(Router router) {
    router.post("/some/endpoint").handler(ctx -> {
      ctx.request().setExpectMultipart(true);
      ctx.next();
    }).blockingHandler(ctx -> {
      // ... Do some blocking operation
    });
  }

  interface SomeLegacyService {

    void doSomethingThatBlocks();
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
    mainRouter.route("/static/*").handler(myStaticHandler);

    mainRouter.route(".*\\.templ").handler(myTemplateHandler);
  }

  public void example24(Router mainRouter, Router restAPI) {

    mainRouter.mountSubRouter("/productsAPI", restAPI);

  }

  public void example25(Router router) {

    Route route = router.get("/somepath/*");

    route.failureHandler(frc -> {

      // This will be called for failures that occur
      // when routing requests to paths starting with
      // '/somepath/'

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
    Route route3 = router.get("/somepath/*");

    route3.failureHandler(failureRoutingContext -> {

      int statusCode = failureRoutingContext.statusCode();

      // Status code will be 500 for the RuntimeException or 403 for the other failure
      HttpServerResponse response = failureRoutingContext.response();
      response.setStatusCode(statusCode).end("Sorry! Not today");

    });

  }

  public void example27(Router router) {

    // This body handler will be called for all routes
    router.route().handler(BodyHandler.create());

  }

  public void example28(Router router) {

    router.route().handler(BodyHandler.create());

    router.post("/some/path/uploads").handler(routingContext -> {

      Set<FileUpload> uploads = routingContext.fileUploads();
      // Do something with uploads....

    });
  }

  public void example29(Router router) {

    // This cookie handler will be called for all routes
    router.route().handler(CookieHandler.create());

  }

  public void example30(Router router) {

    // This cookie handler will be called for all routes
    router.route().handler(CookieHandler.create());

    router.route("some/path/").handler(routingContext -> {

      Cookie someCookie = routingContext.getCookie("mycookie");
      String cookieValue = someCookie.getValue();

      // Do something with cookie...

      // Add a cookie - this will get written back in the response automatically
      routingContext.addCookie(Cookie.cookie("othercookie", "somevalue"));
    });
  }

  public void example31(Vertx vertx) {

    // Create a local session store using defaults
    SessionStore store1 = LocalSessionStore.create(vertx);

    // Create a local session store specifying the local shared map name to use
    // This might be useful if you have more than one application in the same
    // Vert.x instance and want to use different maps for different applications
    SessionStore store2 = LocalSessionStore.create(vertx, "myapp3.sessionmap");

    // Create a local session store specifying the local shared map name to use and
    // setting the reaper interval for expired sessions to 10 seconds
    SessionStore store3 = LocalSessionStore.create(vertx, "myapp3.sessionmap", 10000);

  }

  public void example32() {

    // a clustered Vert.x
    Vertx.clusteredVertx(new VertxOptions().setClustered(true), res -> {

      Vertx vertx = res.result();

      // Create a clustered session store using defaults
      SessionStore store1 = ClusteredSessionStore.create(vertx);

      // Create a clustered session store specifying the distributed map name to use
      // This might be useful if you have more than one application in the cluster
      // and want to use different maps for different applications
      SessionStore store2 = ClusteredSessionStore.create(vertx, "myclusteredapp3.sessionmap");
    });

  }

  public void example33(Vertx vertx) {

    Router router = Router.router(vertx);

    // We need a cookie handler first
    router.route().handler(CookieHandler.create());

    // Create a clustered session store using defaults
    SessionStore store = ClusteredSessionStore.create(vertx);

    SessionHandler sessionHandler = SessionHandler.create(store);

    // Make sure all requests are routed through the session handler too
    router.route().handler(sessionHandler);

    // Now your application handlers
    router.route("/somepath/blah/").handler(routingContext -> {

      Session session = routingContext.session();
      session.put("foo", "bar");
      // etc

    });

  }

  public void example34(SessionHandler sessionHandler, Router router) {

    router.route().handler(CookieHandler.create());
    router.route().handler(sessionHandler);

    // Now your application handlers
    router.route("/somepath/blah").handler(routingContext -> {

      Session session = routingContext.session();

      // Put some data from the session
      session.put("foo", "bar");

      // Retrieve some data from a session
      int age = session.get("age");

      // Remove some data from a session
      JsonObject obj = session.remove("myobj");

    });

  }


  public void example37(Vertx vertx, AuthProvider authProvider, Router router) {

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    AuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
  }

  public void example38(Vertx vertx, AuthProvider authProvider, Router router) {

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(UserSessionHandler.create(authProvider));

    AuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);

    // All requests to paths starting with '/private/' will be protected
    router.route("/private/*").handler(basicAuthHandler);

    router.route("/someotherpath").handler(routingContext -> {

      // This will be public access - no login required

    });

    router.route("/private/somepath").handler(routingContext -> {

      // This will require a login

      // This will have the value true
      boolean isAuthenticated = routingContext.user() != null;

    });
  }

  public void example39(Vertx vertx, AuthProvider authProvider, Router router) {

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(UserSessionHandler.create(authProvider));

    AuthHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider);

    // All requests to paths starting with '/private/' will be protected
    router.route("/private/*").handler(redirectAuthHandler);

    // Handle the actual login
    // One of your pages must POST form login data
    router.post("/login").handler(FormLoginHandler.create(authProvider));

    // Set a static server to serve static resources, e.g. the login page
    router.route().handler(StaticHandler.create());

    router.route("/someotherpath").handler(routingContext -> {
      // This will be public access - no login required
    });

    router.route("/private/somepath").handler(routingContext -> {

      // This will require a login

      // This will have the value true
      boolean isAuthenticated = routingContext.user() != null;

    });

  }

  public void example40(AuthProvider authProvider, Router router) {

    AuthHandler listProductsAuthHandler = RedirectAuthHandler.create(authProvider);
    listProductsAuthHandler.addAuthority("list_products");

    // Need "list_products" authority to list products
    router.route("/listproducts/*").handler(listProductsAuthHandler);

    AuthHandler settingsAuthHandler = RedirectAuthHandler.create(authProvider);
    settingsAuthHandler.addAuthority("role:admin");

    // Only "admin" has access to /private/settings
    router.route("/private/settings/*").handler(settingsAuthHandler);

  }

  public void example41(Router router) {

    router.route("/static/*").handler(StaticHandler.create());

  }

  public void example41_0_1(Router router) {

    // Will only accept GET requests from origin "vertx.io"
    router.route().handler(CorsHandler.create("vertx\\.io").allowedMethod(HttpMethod.GET));

    router.route().handler(routingContext -> {

      // Your app handlers

    });
  }

  public void example41_2(Router router, TemplateEngine engine) {

    TemplateHandler handler = TemplateHandler.create(engine);

    router.get("/dynamic").handler(routingContext -> {

      routingContext.put("request_path", routingContext.request().path());
      routingContext.put("session_data", routingContext.session().data());

      routingContext.next();
    });

    router.get("/dynamic/").handler(handler);

  }

  public void example41_3(Router router) {

    // Any errors on paths beginning with '/somepath/' will be handled by this error handler
    router.route("/somepath/").failureHandler(ErrorHandler.create());

  }


  public void example42(Router router) {

    router.route("/foo/").handler(TimeoutHandler.create(5000));

  }

  public void example43(Vertx vertx) {

    Router router = Router.router(vertx);

    SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

    router.route("/myapp/*").handler(sockJSHandler);
  }

  public void example44(Vertx vertx) {

    Router router = Router.router(vertx);

    SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

    sockJSHandler.socketHandler(sockJSSocket -> {

      // Just echo the data back
      sockJSSocket.handler(sockJSSocket::write);
    });

    router.route("/myapp/*").handler(sockJSHandler);
  }

  public void example45(Vertx vertx) {

    Router router = Router.router(vertx);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions();
    sockJSHandler.bridge(options);

    router.route("/eventbus/*").handler(sockJSHandler);
  }

  public void example46(Vertx vertx) {

    Router router = Router.router(vertx);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);


    // Let through any messages sent to 'demo.orderMgr' from the client
    PermittedOptions inboundPermitted1 = new PermittedOptions().setAddress("demo.orderMgr");

    // Allow calls to the address 'demo.persistor' from the client as long as the messages
    // have an action field with value 'find' and a collection field with value
    // 'albums'
    PermittedOptions inboundPermitted2 = new PermittedOptions().setAddress("demo.persistor")
      .setMatch(new JsonObject().put("action", "find")
        .put("collection", "albums"));

    // Allow through any message with a field `wibble` with value `foo`.
    PermittedOptions inboundPermitted3 = new PermittedOptions().setMatch(new JsonObject().put("wibble", "foo"));

    // First let's define what we're going to allow from server -> client

    // Let through any messages coming from address 'ticker.mystock'
    PermittedOptions outboundPermitted1 = new PermittedOptions().setAddress("ticker.mystock");

    // Let through any messages from addresses starting with "news." (e.g. news.europe, news.usa, etc)
    PermittedOptions outboundPermitted2 = new PermittedOptions().setAddressRegex("news\\..+");

    // Let's define what we're going to allow from client -> server
    BridgeOptions options = new BridgeOptions().
      addInboundPermitted(inboundPermitted1).
      addInboundPermitted(inboundPermitted1).
      addInboundPermitted(inboundPermitted3).
      addOutboundPermitted(outboundPermitted1).
      addOutboundPermitted(outboundPermitted2);

    sockJSHandler.bridge(options);

    router.route("/eventbus/*").handler(sockJSHandler);
  }

  public void example47() {

    // Let through any messages sent to 'demo.orderService' from the client
    PermittedOptions inboundPermitted = new PermittedOptions().setAddress("demo.orderService");

    // But only if the user is logged in and has the authority "place_orders"
    inboundPermitted.setRequiredAuthority("place_orders");

    BridgeOptions options = new BridgeOptions().addInboundPermitted(inboundPermitted);
  }

  public void example48(Vertx vertx, AuthProvider authProvider) {

    Router router = Router.router(vertx);

    // Let through any messages sent to 'demo.orderService' from the client
    PermittedOptions inboundPermitted = new PermittedOptions().setAddress("demo.orderService");

    // But only if the user is logged in and has the authority "place_orders"
    inboundPermitted.setRequiredAuthority("place_orders");

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(new BridgeOptions().
      addInboundPermitted(inboundPermitted));

    // Now set up some basic auth handling:

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    AuthHandler basicAuthHandler = BasicAuthHandler.create(authProvider);

    router.route("/eventbus/*").handler(basicAuthHandler);


    router.route("/eventbus/*").handler(sockJSHandler);

  }

  public void example48_1(Vertx vertx) {

    Router router = Router.router(vertx);

    // Let through any messages sent to 'demo.orderService' from the client
    PermittedOptions inboundPermitted = new PermittedOptions().setAddress("demo.orderService");

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions().addInboundPermitted(inboundPermitted);

    sockJSHandler.bridge(options, be -> {
      if (be.type() == BridgeEventType.PUBLISH || be.type() == BridgeEventType.SEND) {
        // Add some headers
        JsonObject headers = new JsonObject().put("header1", "val").put("header2", "val2");
        JsonObject rawMessage = be.getRawMessage();
        rawMessage.put("headers", headers);
        be.setRawMessage(rawMessage);
      }
      be.complete(true);
    });

    router.route("/eventbus/*").handler(sockJSHandler);


  }

  public void example49(Vertx vertx) {

    Router router = Router.router(vertx);

    // Let through any messages sent to 'demo.orderMgr' from the client
    PermittedOptions inboundPermitted = new PermittedOptions().setAddress("demo.someService");

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions().addInboundPermitted(inboundPermitted);

    sockJSHandler.bridge(options, be -> {
      if (be.type() == BridgeEventType.PUBLISH || be.type() == BridgeEventType.RECEIVE) {
        if (be.getRawMessage().getString("body").equals("armadillos")) {
          // Reject it
          be.complete(false);
          return;
        }
      }
      be.complete(true);
    });

    router.route("/eventbus/*").handler(sockJSHandler);


  }

  public void handleSocketIdle(Vertx vertx, PermittedOptions inboundPermitted) {
    Router router = Router.router(vertx);

    // Initialize SockJS handler
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions().addInboundPermitted(inboundPermitted).setPingTimeout(5000);

    sockJSHandler.bridge(options, be -> {
      if (be.type() == BridgeEventType.SOCKET_IDLE) {
        // Do some custom handling...
      }

      be.complete(true);
    });

    router.route("/eventbus/*").handler(sockJSHandler);
  }

  public void example50(Vertx vertx) {

    Router router = Router.router(vertx);

    JWTAuthOptions authConfig = new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret"));

    JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

    router.route("/login").handler(ctx -> {
      // this is an example, authentication should be done with another provider...
      if ("paulo".equals(ctx.request().getParam("username")) && "secret".equals(ctx.request().getParam("password"))) {
        ctx.response().end(authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions()));
      } else {
        ctx.fail(401);
      }
    });
  }

  public void example51(Vertx vertx) {

    Router router = Router.router(vertx);

    JWTAuthOptions authConfig = new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret"));

    JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

    router.route("/protected/*").handler(JWTAuthHandler.create(authProvider));

    router.route("/protected/somepage").handler(ctx -> {
      // some handle code...
    });
  }

  public void example52(Vertx vertx) {

    JWTAuthOptions authConfig = new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret"));

    JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

    authProvider.generateToken(new JsonObject().put("sub", "paulo").put("someKey", "some value"), new JWTOptions());
  }

  public void example53(Vertx vertx) {

    Handler<RoutingContext> handler = rc -> {
      String theSubject = rc.user().principal().getString("sub");
      String someKey = rc.user().principal().getString("someKey");
    };
  }

  public void example54(Router router) {

    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("abracadabra"));
    router.route().handler(rc -> {

    });
  }

  public void example55(Router router) {

    router.get("/some/path").handler(routingContext -> {

      routingContext.put("foo", "bar");
      routingContext.next();

    });

    router.get("/some/path/B").handler(routingContext -> routingContext.response().end());

    router.get("/some/path").handler(routingContext -> routingContext.reroute("/some/path/B"));

  }

  public void example55b(Router router) {

    router.get("/my-pretty-notfound-handler").handler(ctx -> ctx.response()
      .setStatusCode(404)
      .end("NOT FOUND fancy html here!!!"));

    router.get().failureHandler(ctx -> {
      if (ctx.statusCode() == 404) {
        ctx.reroute("/my-pretty-notfound-handler");
      } else {
        ctx.next();
      }
    });
  }

  public void example55c(Router router) {

    router.get("/final-target").handler(ctx -> {
      // continue from here...
    });

    // THE WRONG WAY! (Will reroute to /final-target excluding the query string)
    router.get().handler(ctx -> ctx.reroute("/final-target?variable=value"));

    // THE CORRECT WAY!
    router.get().handler(ctx -> ctx
      .put("variable", "value")
      .reroute("/final-target"));
  }

  public void example56(Router router) {
    router.route().handler(VirtualHostHandler.create("*.vertx.io", routingContext -> {
      // do something if the request is for *.vertx.io
    }));
  }

  public void example57(Router router) {

    Route route = router.get("/localized").handler(rc -> {
      // although it might seem strange by running a loop with a switch we
      // make sure that the locale order of preference is preserved when
      // replying in the users language.
      for (LanguageHeader language : rc.acceptableLanguages()) {
        switch (language.tag()) {
          case "en":
            rc.response().end("Hello!");
            return;
          case "fr":
            rc.response().end("Bonjour!");
            return;
          case "pt":
            rc.response().end("Olá!");
            return;
          case "es":
            rc.response().end("Hola!");
            return;
        }
      }
      // we do not know the user language so lets just inform that back:
      rc.response().end("Sorry we don't speak: " + rc.preferredLanguage());
    });
  }

  public void example58(Vertx vertx, Router router) {

    // create an OAuth2 provider, clientID and clientSecret should be requested to github
    OAuth2Auth authProvider = GithubAuth.create(vertx, "CLIENT_ID", "CLIENT_SECRET");

    // create a oauth2 handler on our running server
    // the second argument is the full url to the callback as you entered in your provider management console.
    OAuth2AuthHandler oauth2 = OAuth2AuthHandler.create(authProvider, "https://myserver.com/callback");

    // setup the callback handler for receiving the GitHub callback
    oauth2.setupCallback(router.route());

    // protect everything under /protected
    router.route("/protected/*").handler(oauth2);
    // mount some handler under the protected zone
    router.route("/protected/somepage").handler(rc -> rc.response().end("Welcome to the protected resource!"));

    // welcome page
    router.get("/").handler(ctx -> ctx.response().putHeader("content-type", "text/html").end("Hello<br><a href=\"/protected/somepage\">Protected by Github</a>"));
  }

  public void example59(Vertx vertx, Router router) {

    // create an OAuth2 provider, clientID and clientSecret should be requested to Google
    OAuth2Auth authProvider = OAuth2Auth.create(vertx, OAuth2FlowType.AUTH_CODE, new OAuth2ClientOptions()
      .setClientID("CLIENT_ID")
      .setClientSecret("CLIENT_SECRET")
      .setSite("https://accounts.google.com")
      .setTokenPath("https://www.googleapis.com/oauth2/v3/token")
      .setAuthorizationPath("/o/oauth2/auth"));

    // create a oauth2 handler on our domain: "http://localhost:8080"
    OAuth2AuthHandler oauth2 = OAuth2AuthHandler.create(authProvider, "http://localhost:8080");

    // these are the scopes
    oauth2.addAuthority("profile");

    // setup the callback handler for receiving the Google callback
    oauth2.setupCallback(router.get("/callback"));

    // protect everything under /protected
    router.route("/protected/*").handler(oauth2);
    // mount some handler under the protected zone
    router.route("/protected/somepage").handler(rc -> rc.response().end("Welcome to the protected resource!"));

    // welcome page
    router.get("/").handler(ctx -> ctx.response().putHeader("content-type", "text/html").end("Hello<br><a href=\"/protected/somepage\">Protected by Google</a>"));
  }

  public void example61(Vertx vertx, Router router, OAuth2Auth provider) {
    // create a oauth2 handler pinned to myserver.com: "https://myserver.com:8447/callback"
    OAuth2AuthHandler oauth2 = OAuth2AuthHandler.create(provider, "https://myserver.com:8447/callback");
    // now allow the handler to setup the callback url for you
    oauth2.setupCallback(router.route());
  }

  public void example62(Vertx vertx, Router router) {
    // To simplify the development of the web components
    // we use a Router to route all HTTP requests
    // to organize our code in a reusable way.

    // We need cookies and sessions
    router.route()
      .handler(CookieHandler.create());
    router.route()
      .handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    // Simple auth service which uses a GitHub to
    // authenticate the user
    OAuth2Auth authProvider =
      GithubAuth.create(vertx, "YOUR PROVIDER CLIENTID", "YOUR PROVIDER CLIENT SECRET");
    // We need a user session handler too to make sure
    // the user is stored in the session between requests
    router.route()
      .handler(UserSessionHandler.create(authProvider));
    // we now protect the resource under the path "/protected"
    router.route("/protected").handler(
      OAuth2AuthHandler.create(authProvider)
        // we now configure the oauth2 handler, it will
        // setup the callback handler
        // as expected by your oauth2 provider.
        .setupCallback(router.route("/callback"))
        // for this resource we require that users have
        // the authority to retrieve the user emails
        .addAuthority("user:email")
    );
    // Entry point to the application, this will render
    // a custom template.
    router.get("/").handler(ctx -> ctx.response()
      .putHeader("Content-Type", "text/html")
      .end(
        "<html>\n" +
        "  <body>\n" +
        "    <p>\n" +
        "      Well, hello there!\n" +
        "    </p>\n" +
        "    <p>\n" +
        "      We're going to the protected resource, if there is no\n" +
        "      user in the session we will talk to the GitHub API. Ready?\n" +
        "      <a href=\"/protected\">Click here</a> to begin!</a>\n" +
        "    </p>\n" +
        "    <p>\n" +
        "      <b>If that link doesn't work</b>, remember to provide\n" +
        "      your own <a href=\"https://github.com/settings/applications/new\">\n" +
        "      Client ID</a>!\n" +
        "    </p>\n" +
        "  </body>\n" +
        "</html>"));
    // The protected resource
    router.get("/protected").handler(ctx -> {
      // at this moment your user object should contain the info
      // from the Oauth2 response, since this is a protected resource
      // as specified above in the handler config the user object is never null
      User user = ctx.user();
      // just dump it to the client for demo purposes
      ctx.response().end(user.toString());
    });
  }

  public void manualContentType(Router router) {
    router.get("/api/books").produces("application/json").handler(rc -> findBooks(ar -> {
      if (ar.succeeded()) {
        rc.response().putHeader("Content-Type", "application/json").end(toJson(ar.result()));
      } else {
        rc.fail(ar.cause());
      }
    }));
  }

  public void contentTypeHandler(Router router) {
    router.route("/api/*").handler(ResponseContentTypeHandler.create());
    router.get("/api/books").produces("application/json").handler(rc -> findBooks(ar -> {
      if (ar.succeeded()) {
        rc.response().end(toJson(ar.result()));
      } else {
        rc.fail(ar.cause());
      }
    }));
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
    router.get("/api/books").produces("text/xml").produces("application/json").handler(rc -> findBooks(ar -> {
      if (ar.succeeded()) {
        if (rc.getAcceptableContentType().equals("text/xml")) {
          rc.response().end(toXML(ar.result()));
        } else {
          rc.response().end(toJson(ar.result()));
        }
      } else {
        rc.fail(ar.cause());
      }
    }));
  }

  public void example63(Router router, AuthProvider provider) {

    ChainAuthHandler chain = ChainAuthHandler.create();

    // add http basic auth handler to the chain
    chain.append(BasicAuthHandler.create(provider));
    // add form redirect auth handler to the chain
    chain.append(RedirectAuthHandler.create(provider));

    // secure your route
    router.route("/secure/resource").handler(chain);
    // your app
    router.route("/secure/resource").handler(ctx -> {
      // do something...
    });
  }
}

