/**
 * = Apex
 * :toc: left
 *
 * Apex is a set of building blocks for building web applications with Vert.x. Think of it as a Swiss Army Knife for building
 * modern, scalable, web apps.
 *
 * Vert.x core provides a fairly low level set of functionality for handling HTTP, and for some applications
 * that will be sufficient.
 *
 * Vert.x Apex builds on Vert.x core to provide a richer set of functionality for building real web applications, more
 * easily.
 *
 * It's the successor to http://pmlopes.github.io/yoke/[Yoke] in Vert.x 2.x, and takes inspiration from projects such
 * as http://expressjs.com/[Express] in the Node.js world and http://www.sinatrarb.com/[Sinatra] in the Ruby world.
 *
 * Apex is designed to be powerful, un-opionated and fully embeddable. You just use the parts you want and nothing more.
 *
 * Apex is not a container.
 *
 * You can use Apex to create classic server-side web applications, RESTful web applications, 'real-time' (server push)
 * web applications, or any other kind of web application you can think of. Apex doesn't care.
 *
 * It's up to you to chose the type of app you prefer, not Apex.
 *
 * Apex is a great fit for writing *RESTful HTTP micro-services*, but we don't *force* you to write apps like that.
 *
 * Some of the key features of Apex include:
 *
 * * Routing (based on method, path, etc)
 * * Regex pattern matching for paths
 * * Extraction of parameters from paths
 * * Content negotiation
 * * Request body handling
 * * Body size limits
 * * Cookie parsing and handling
 * * Multipart forms
 * * Multipart file uploads
 * * Sub routers
 *
 * Apex add-ons include:
 *
 * * Session support - both local (for sticky sessions) and clustered (for non sticky)
 * * CORS (Cross Origin Resource Sharing) support
 * * Error page template
 * * Basic Authentication
 * * Redirect based authentication
 * * User/role/permission authorisation
 * * Favicon handling
 * * Template support for server side rendering. Supports: Handlebars, Jade, MVEL and Thymeleaf out of the box
 * * Response time handler
 * * Static file serving, including caching logic and directory listing.
 * * Request timeout support
 *
 * Most features in Apex are implemented as handlers so you can always write your own. We envisage many more being written
 * over time.
 *
 * We'll discuss all these features in this manual.
 *
 * == Re-cap on Vert.x core HTTP servers
 *
 * Apex uses and exposes API from Vert.x core, so it's well worth getting familiar with the basic concepts of writing
 * HTTP servers using Vert.x core, if you're not already.
 *
 * The Vert.x core HTTP documentation goes into a lot of detail on this.
 *
 * Here's a hello world web server written using Vert.x core. At this point there is no Apex involved:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example1}
 * ----
 *
 * We create an HTTP server instance, and we set a request handler on it. The request handler will be called whenever
 * a request arrives on the server.
 *
 * When that happens we are just going to set the content type to `text/plain`, and write `Hello World!` and end the
 * response.
 *
 * We then tell the server to listen at port `8080` (default host is `localhost`).
 *
 * You can run this, and point your browser at `http://localhost:8080` to verify that it works as expected.
 *
 * == Basic Apex concepts
 *
 * Here's the 10000 foot view:
 *
 * A {@link io.vertx.ext.apex.core.Router} is one of the core concepts of Apex.
 *
 * A router is an object which maintains zero or more {@link io.vertx.ext.apex.core.Route}s.
 *
 * A router handles an HTTP request and finds the first matching route for that request, and passes the request to that route.
 *
 * The route can have a *handler* associated with it, which then receives the request.
 *
 * You then *do something* with the request, and then, either end it or pass it to the next matching handler.
 *
 * Here's a simple router example:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example2}
 * ----
 *
 * It's basically does the same thing as the Vert.x Core HTTP server hello world example from the previous section,
 * but this time using Apex.
 *
 * We create an HTTP server as before, then we create a router.
 *
 * Once we've done that we create a simple route with no matching criteria so it will match *all* requests that arrive on the server.
 *
 * We then specify a handler for that route. That handler will be called for all requests that arrive on the server.
 *
 * The object that gets passed into the handler is a {@link io.vertx.ext.apex.core.RoutingContext} - this contains
 * the standard Vert.x {@link io.vertx.core.http.HttpServerRequest} and {@link io.vertx.core.http.HttpServerResponse}
 * but also various other useful stuff that makes working with Apex simpler.
 *
 * For every request that is routed there is a unique routing context instance, and the same instance is passed to
 * all handlers for that request.
 *
 * Once we've set up the handler, we set the request handler of the HTTP server to pass all incoming requests
 * to {@link io.vertx.ext.apex.core.Router#accept}.
 *
 * So, that's the basics. Now we'll look at things in more detail:
 *
 * == Handling requests and calling the next handler
 *
 * When a route matches the handler for the route will be called, passing in an instance of {@link io.vertx.ext.apex.core.RoutingContext}.
 *
 * If you don't end the request in your handler, you can call {@link io.vertx.ext.apex.core.RoutingContext#next} then the router
 * will call the next matching route handler (if any).
 *
 * You don't have to call {@link io.vertx.ext.apex.core.RoutingContext#next} before the handler has finished executing.
 * You can do this some time later, if you want:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example20}
 * ----
 *
 * In the above example `route1` is written to the response, then 5 seconds later `route2` is written to the response,
 * then 5 seconds later `route3` is written to the response and the response is ended.
 *
 * Note, all this happens without any thread blocking.
 *
 * == Routing by path
 *
 * A route can be set-up to match the path from the request URI.
 *
 * In this case it will match any request which has a path that *starts with* the specified path.
 *
 * In the following example the handler will be called for all requests with a URI path that starts with
 * `/some/path/`.
 *
 * For example `/some/path/foo.html` and `/some/path/otherdir/blah.css` would both match.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example3}
 * ----
 *
 * Alternatively the path can be specified when creating the route:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example4}
 * ----
 *
 * == Capturing path parameters
 *
 * It's possible to match paths using placeholders for parameters which are then available in the request
 * {@link io.vertx.core.http.HttpServerRequest#params}.
 *
 * Here's an example
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example4_1}
 * ----
 *
 * In the above example, if a POST request is made to path: `/catalogue/products/tools/drill123/` then the route will match
 * and `productType` will receive the value `tools` and productID will receive the value `drill123`.
 *
 * == Routing with regular expressions
 *
 * Regular expressions can also be used to match URI paths in routes.
 *
 * As in straight path matching the regex is not an *exact match* for the path, but matches the start of the path.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example5}
 * ----
 *
 * Alternatively the regex can be specified when creating the route:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example6}
 * ----
 *
 * == Capturing path parameters with regular expressions
 *
 * You can also capture path parameters when using regular expressions, here's an example:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example6_1}
 * ----
 *
 * In the above example, if a request is made to path: `/tools/drill123/` then the route will match
 * and `productType` will receive the value `tools` and productID will receive the value `drill123`.
 *
 * Captures are denoted in regular expressions with capture groups (i.e. surrounding the capture with round brackets)
 *
 * == Routing by HTTP method
 *
 * By default a route will match all HTTP methods.
 *
 * If you want a route to only match for a specific HTTP method you can use {@link io.vertx.ext.apex.core.Route#method}
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example7}
 * ----
 *
 * Or you can specify this with a path when creating the route:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example8}
 * ----
 *
 * If you want to route for a specific HTTP method you can also use the methods such as {@link io.vertx.ext.apex.core.Router#get},
 * {@link io.vertx.ext.apex.core.Router#post} and {@link io.vertx.ext.apex.core.Router#put} named after the HTTP
 * method name. For example:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example8_1}
 * ----
 *
 * If you want to specify a route will match for more than HTTP method you can call {@link io.vertx.ext.apex.core.Route#method}
 * multiple times:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example9}
 * ----
 *
 *
 *
 * == Route order
 *
 * By default routes are matched in the order they are added to the router.
 *
 * When a request arrives the router will step through each route and check if it matches, if it matches then
 * the handler for that route will be called.
 *
 * If the handler subsequently calls {@link io.vertx.ext.apex.core.RoutingContext#next} the handler for the next
 * matching route (if any) will be called. And so on.
 *
 * Here's an example to illustrate this:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example10}
 * ----
 *
 * In the above example the response will contain:
 *
 * ----
 * route1
 * route2
 * route3
 * ----
 *
 * As the routes have been called in that order for any request that starts with `/some/path`.
 *
 * If you want to override the default ordering for routes, you can do so using {@link io.vertx.ext.apex.core.Route#order},
 * specifying an integer value.
 *
 * Default routes are assigned an implicit order corresponding to the order in which they were added to the router, with
 * the first route numbered `0`, the second route numbered `1`, and so on.
 *
 * By specifying an order for the route you can override the default ordering. Order can also be negative, e.g. if you
 * want to ensure a route is evaluated before route number `0`.
 *
 * Let's change the ordering of route2 so it runs before route1:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example11}
 * ----
 *
 * then the response will now contain:
 *
 * ----
 * route2
 * route1
 * route3
 * ----
 *
 * If two matching routes have the same value of order, then they will be called in the order they were added.
 *
 * You can also specify a route is handled last, with {@link io.vertx.ext.apex.core.Route#last}
 *
 * == Routing based on MIME type of request
 *
 * You can specify that a route will match against matching request MIME types using {@link io.vertx.ext.apex.core.Route#consumes}.
 *
 * In this case, the request will contain a `content-type` header specifying the MIME type of the request body.
 *
 * This will be matched against the value specified in {@link io.vertx.ext.apex.core.Route#consumes}.
 *
 * Basically, `consumes` is describing which MIME types the route will consume.
 *
 * Matching can be done on exact MIME type matches:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example12}
 * ----
 *
 * Multiple exact matches can also be specified:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example13}
 * ----
 *
 * Matching on wildcards for the sub-type is supported:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example14}
 * ----
 *
 * And you can also match on the top level type
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example15}
 * ----
 *
 * If you don't specify a `/` in the consumers, it will assume you meant the sub-type.
 *
 * == Routing based on MIME types acceptable by the client
 *
 * The HTTP `accept` header is used to signify which MIME types of the response are acceptable to the client.
 *
 * An `accept` header can have multiple MIME types separated by `,`. MIME types can also have a `q` value appended to them
 * which signifies a weighting to apply if more than one response MIME type is available matching the accept header. The
 * q value is a number between 0 and 1.0. If omitted it defaults to 1.0.
 *
 * For example, the following `accept` header signifies the client will accept a MIME type of only `text/plain`:
 *
 *  Accept: text/plain
 *
 *  With the following the client will accept `text/plain` or `text/html` with no preference.
 *
 *  Accept: text/plain, text/html
 *
 *  With the following the client will accept `text/plain` or `text/html` but prefers `text/html` as it has a higher `q` value
 *  (the default value is q=1.0)
 *
 *  Accept: text/plain; q=0.9, text/html
 *
 *  If the server can provide both text/plain and text/html it should provide the text/html in this case.
 *
 * By using {@link io.vertx.ext.apex.core.Route#produces} you define which MIME type(s) the route produces, e.g. the
 * following handler produces a response with MIME type `application/json`.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example16}
 * ----
 *
 * In this case the route will match with any request with an `accept` header that matches `application/json`.
 *
 * Here are some examples of `accept` headers that will match:
 *
 *  Accept: application/json
 *  Accept: application/*
 *  Accept: *&#47;json
 *  Accept: application/json, text/html
 *  Accept: application/json;q=0.7, text/html;q=0.8, text/plain
 *
 *  You can also mark your route as producing more than one MIME type. If this is the case, then you use
 *  {@link io.vertx.ext.apex.core.RoutingContext#getAcceptableContentType} to find out the actual MIME type that
 *  was accepted.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example17}
 * ----
 *
 * In the above example, if I sent a request with the following `accept` header:
 *
 *  Accept: application/json; q=0.7, text/html
 *
 * Then the route would match and `acceptableContentType` would contain `text/html` as both are
 * acceptable but that has a higher `q` value.
 *
 * == Combining routing criteria
 *
 * You can combine all the above routing criteria in many different ways, for example:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example18}
 * ----
 *
 * == Enabling and disabling routes
 *
 * You can disable a route with {@link io.vertx.ext.apex.core.Route#disable}.
 *
 * A disabled route will be ignored when matching.
 *
 * You can re-enable a disabled route with {@link io.vertx.ext.apex.core.Route#enable}
 *
 * == Context data
 *
 * You can use the context data in the {@link io.vertx.ext.apex.core.RoutingContext} to maintain any data that you
 * want to share between handlers for the lifetime of the request.
 *
 * Here's an example where one handler sets some data in the context data a subsequent handler retrieves it:
 *
 * You can use the {@link io.vertx.ext.apex.core.RoutingContext#put} to put any object, and
 * {@link io.vertx.ext.apex.core.RoutingContext#get} to retrieve any object from the context data.
 *
 * A request sent to path `/some/path` will match both routes.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example21}
 * ----
 *
 * Alternatively you can access the entire context data map with {@link io.vertx.ext.apex.core.RoutingContext#contextData}.
 *
 * == Sub-routers
 *
 * Sometimes if you have a lot of handlers it can make sense to split them up into multiple routers. This is also useful
 * if you want to reuse a set of handlers in a different application, rooted at a different path root.
 *
 * To do this you can mount a router at a _mount point_ in another router. The router that is mounted is called a
 * _sub-router_. Sub routers can mount other sub routers so you can have several levels of sub-routers if you like.
 *
 * Let's look at a simple example of a sub-router mounted with another router.
 *
 * The sub-router will maintain the set of handlers that corresponds to a simple fictional REST API. We will mount that on another
 * router. The full implementation of the REST API is not shown.
 *
 * Here's the sub-router:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example22}
 * ----
 *
 * If this router was used as a top level router, then GET/PUT/DELETE requests to urls like `/products/product1234`
 * would invoke the  API.
 *
 * However, let's say we already have a web-site as described by another router:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example23}
 * ----
 *
 * We can now mount the sub router on the main router, against a mount point, in this case `/productsAPI`
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example24}
 * ----
 *
 * This means the REST API is not accessible via paths like: `/productsAPI/products/product1234`
 *
 * == Default 404 Handling
 *
 * If no routes match for any particular request, Apex will signal a 404 error. This can then be handled by your
 * own error handler, or perhaps the augmented error handler that we supply to use, or if no error handler is provided
 * Apex will send back a basic 404 (Not Found) response.
 *
 * == Error handling
 *
 * As well as setting handlers to handle requests you can also set handlers to handle errors in Vert.x.
 *
 * Error handlers can be used with the exact same route matching criteria that you can use with normal handlers.
 *
 * For example you can provide an error handler that will only handle errors on certain paths, or for certain HTTP methods.
 *
 * This allows you to set different error handlers for different parts of your web application.
 *
 * Here's an example error handler that will only be called for errors that occur when routing to GET requests
 * to paths that start with `\somepath\`:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example25}
 * ----
 *
 * Error routing will occur if a handler throws an exception, or if a handler calls
 * {@link io.vertx.ext.apex.core.RoutingContext#fail} specifying an HTTP status code to deliberately signal a failure.
 *
 * If an exception is caught from a handler this will result in a failure with status code `500` being signalled.
 *
 * When handling the failure, the failure handler is passed an instance of {@link io.vertx.ext.apex.core.FailureRoutingContext}
 * which is like {@link io.vertx.ext.apex.core.RoutingContext} but which also allows the failure or failure code
 * to be retrieved so the failure handler can use that to generate a failure response.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example26}
 * ----
 *
 * == Using the BodyHandler
 *
 * The {@link io.vertx.ext.apex.core.BodyHandler} allows you to retrieve request bodies, limit body sizes and handle
 * file uploads.
 *
 * You should make sure a body handler is on a matching route for any requests that require this functionality.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example27}
 * ----
 *
 * === Getting the request body
 *
 * If you know the request body is JSON, then you can use {@link io.vertx.ext.apex.core.RoutingContext#getBodyAsJson},
 * if you know it's a string you can use {@link io.vertx.ext.apex.core.RoutingContext#getBodyAsString}, or to
 * retrieve it as a buffer use {@link io.vertx.ext.apex.core.RoutingContext#getBody()}.
 *
 * === Limiting body size
 *
 * To limit the size of a request body, create the body handler with {@link io.vertx.ext.apex.core.BodyHandler#bodyHandler(long)}
 * specifying the maximum body size, in bytes. This is useful to avoid running out of memory with very large bodies.
 *
 * If an attempt to send a body greater than the maximum size is made, an HTTP status code of 413 - `Request Entity Too Large`,
 * will be sent.
 *
 * There is no body limit by default.
 *
 * === Handling file uploads
 *
 * Body handler can also be used to handle multi-part file uploads. If a body handler is on a matching route for the
 * request, any file uploads will be automatically streamed to the uploads directory, which is `file-uploads` by default.
 * Each file will be given an automatically generated file name, and the file uploads will be available on the routing
 * context with {@link io.vertx.ext.apex.core.RoutingContext#fileUploads()}.
 *
 * Here's an example:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example28}
 * ----
 *
 * Each file upload is described by a {@link io.vertx.ext.apex.core.FileUpload} instance, which allows various properties
 * such as the name, file-name and size to be accessed.
 *
 * == Handling cookies
 *
 * Apex has cookies support using the {@link io.vertx.ext.apex.core.CookieHandler}.
 *
 * You should make sure a cookie handler is on a matching route for any requests that require this functionality.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example29}
 * ----
 *
 * == Apex add-ons
 *
 * Whereas Apex core contains basic routing functionality, Apex also provides a set of useful "add-ons" that you can
 * use to build real web applications more easily.
 *
 * === Retrieving cookies
 *
 * To retrieve cookies you can use {@link io.vertx.ext.apex.core.RoutingContext#getCookie(java.lang.String)} to retrieve
 * one by name, or use {@link io.vertx.ext.apex.core.RoutingContext#cookies()} to retrieve the entire set.
 *
 * To remove a cookie, use {@link io.vertx.ext.apex.core.RoutingContext#removeCookie(java.lang.String)}.
 *
 * To add a cookie use {@link io.vertx.ext.apex.core.RoutingContext#addCookie(io.vertx.ext.apex.core.Cookie)}.
 *
 * The set of cookies will be written back in the response automatically when the response headers are written so the
 * browser can update any values.
 *
 * Cookies are described by instances of {@link io.vertx.ext.apex.core.Cookie}. This allows you to retrieve the name,
 * value, domain, path and other normal cookie properties.
 *
 * Here's an example of querying and adding cookies:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example30}
 * ----
 *
 * === Session Handling
 *
 * If you want to enable sessions in your Apex application, you need a {@link io.vertx.ext.apex.core.SessionHandler}
 * on a matching route before your application logic.
 *
 * The session handler should be created with a {@link io.vertx.ext.apex.core.SessionStore} instance. Apex comes with
 * two session store implementations:
 *
 * Clustered session store::
 *
 * Local session store::
 *
 * Your session is available on the routing context with {@link io.vertx.ext.apex.core.RoutingContext#session()}.
 *
 */
@Document(fileName = "index.adoc")
package io.vertx.ext.apex;

import io.vertx.docgen.Document;
