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
 * Apex is not a container.
 *
 * You can use Apex to create classic server-side web applications, RESTful web applications, 'real-time' (server push)
 * web applications, or any other kind of web application you can think of. Apex doesn't care. It's up to you to chose
 * the type of app you prefer, not Apex.
 *
 * Apex is a great fit for writing *RESTful HTTP micro-services*, but we don't *force* you to write apps like that.
 *
 * Some of the key features of Apex include:
 *
 * * Routing (based on method, path, etc)
 * * Regular expression pattern matching for paths
 * * Extraction of parameters from paths
 * * Content negotiation
 * * Request body handling
 * * Body size limits
 * * Cookie parsing and handling
 * * Multipart forms
 * * Multipart file uploads
 * * Sub routers
 * * Session support - both local (for sticky sessions) and clustered (for non sticky)
 * * CORS (Cross Origin Resource Sharing) support
 * * Error page handler
 * * Basic Authentication
 * * Redirect based authentication
 * * User/role/permission authorisation
 * * Favicon handling
 * * Template support for server side rendering, including support for the following template engines out of the box:
 * ** Handlebars
 * ** Jade,
 * ** MVEL
 * ** Thymeleaf
 * * Response time handler
 * * Static file serving, including caching logic and directory listing.
 * * Request timeout support
 * * SockJS support
 * * Event-bus bridge
 *
 * Most features in Apex are implemented as handlers so you can always write your own. We envisage many more being written
 * over time.
 *
 * We'll discuss all these features in this manual.
 *
 * == Re-cap on Vert.x core HTTP servers
 *
 * Apex uses and exposes the API from Vert.x core, so it's well worth getting familiar with the basic concepts of writing
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
 * A {@link io.vertx.ext.apex.Router} is one of the core concepts of Apex. It's an object which maintains zero or more
 * {@link io.vertx.ext.apex.Route Routes} .
 *
 * A router takes an HTTP request and finds the first matching route for that request, and passes the request to that route.
 *
 * The route can have a _handler_ associated with it, which then receives the request. You then _do something_ with the
 * request, and then, either end it or pass it to the next matching handler.
 *
 * Here's a simple router example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example2}
 * ----
 *
 * It basically does the same thing as the Vert.x Core HTTP server hello world example from the previous section,
 * but this time using Apex.
 *
 * We create an HTTP server as before, then we create a router. Once we've done that we create a simple route with
 * no matching criteria so it will match _all_ requests that arrive on the server.
 *
 * We then specify a handler for that route. That handler will be called for all requests that arrive on the server.
 *
 * The object that gets passed into the handler is a {@link io.vertx.ext.apex.RoutingContext} - this contains
 * the standard Vert.x {@link io.vertx.core.http.HttpServerRequest} and {@link io.vertx.core.http.HttpServerResponse}
 * but also various other useful stuff that makes working with Apex simpler.
 *
 * For every request that is routed there is a unique routing context instance, and the same instance is passed to
 * all handlers for that request.
 *
 * Once we've set up the handler, we set the request handler of the HTTP server to pass all incoming requests
 * to {@link io.vertx.ext.apex.Router#accept}.
 *
 * So, that's the basics. Now we'll look at things in more detail:
 *
 * == Handling requests and calling the next handler
 *
 * When Apex decides to route a request to a matching route, it calls the handler of the route passing in an instance
 * of {@link io.vertx.ext.apex.RoutingContext}.
 *
 * If you don't end the response in your handler, you should call {@link io.vertx.ext.apex.RoutingContext#next} so another
 * matching route can handle the request (if any).
 *
 * You don't have to call {@link io.vertx.ext.apex.RoutingContext#next} before the handler has finished executing.
 * You can do this some time later, if you want:
 *
 * [source,$lang]
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
 * A route can be set-up to match the path from the request URI. In this case it will match any request which has a path
 * that _starts with_ the specified path.
 *
 * In the following example the handler will be called for all requests with a URI path that starts with
 * `/some/path/`.
 *
 * For example `/some/path/foo.html` and `/some/path/otherdir/blah.css` would both match.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example3}
 * ----
 *
 * Alternatively the path can be specified when creating the route:
 *
 * [source,$lang]
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
 * [source,$lang]
 * ----
 * {@link examples.Examples#example4_1}
 * ----
 *
 * The placeholders consist of `:` followed by the parameter name. Parameter names consist of any alphabetic character,
 * numeric character or underscore.
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
 * [source,$lang]
 * ----
 * {@link examples.Examples#example5}
 * ----
 *
 * Alternatively the regex can be specified when creating the route:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example6}
 * ----
 *
 * == Capturing path parameters with regular expressions
 *
 * You can also capture path parameters when using regular expressions, here's an example:
 *
 * [source,$lang]
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
 * If you want a route to only match for a specific HTTP method you can use {@link io.vertx.ext.apex.Route#method}
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example7}
 * ----
 *
 * Or you can specify this with a path when creating the route:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example8}
 * ----
 *
 * If you want to route for a specific HTTP method you can also use the methods such as {@link io.vertx.ext.apex.Router#get},
 * {@link io.vertx.ext.apex.Router#post} and {@link io.vertx.ext.apex.Router#put} named after the HTTP
 * method name. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example8_1}
 * ----
 *
 * If you want to specify a route will match for more than HTTP method you can call {@link io.vertx.ext.apex.Route#method}
 * multiple times:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example9}
 * ----
 *
 * == Route order
 *
 * By default routes are matched in the order they are added to the router.
 *
 * When a request arrives the router will step through each route and check if it matches, if it matches then
 * the handler for that route will be called.
 *
 * If the handler subsequently calls {@link io.vertx.ext.apex.RoutingContext#next} the handler for the next
 * matching route (if any) will be called. And so on.
 *
 * Here's an example to illustrate this:
 *
 * [source,$lang]
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
 * If you want to override the default ordering for routes, you can do so using {@link io.vertx.ext.apex.Route#order},
 * specifying an integer value.
 *
 * Routes are assigned an order at creation time corresponding to the order in which they were added to the router, with
 * the first route numbered `0`, the second route numbered `1`, and so on.
 *
 * By specifying an order for the route you can override the default ordering. Order can also be negative, e.g. if you
 * want to ensure a route is evaluated before route number `0`.
 *
 * Let's change the ordering of route2 so it runs before route1:
 *
 * [source,$lang]
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
 * You can also specify that a route is handled last, with {@link io.vertx.ext.apex.Route#last}
 *
 * == Routing based on MIME type of request
 *
 * You can specify that a route will match against matching request MIME types using {@link io.vertx.ext.apex.Route#consumes}.
 *
 * In this case, the request will contain a `content-type` header specifying the MIME type of the request body.
 * This will be matched against the value specified in {@link io.vertx.ext.apex.Route#consumes}.
 *
 * Basically, `consumes` is describing which MIME types the handler can _consume_.
 *
 * Matching can be done on exact MIME type matches:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example12}
 * ----
 *
 * Multiple exact matches can also be specified:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example13}
 * ----
 *
 * Matching on wildcards for the sub-type is supported:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example14}
 * ----
 *
 * And you can also match on the top level type
 *
 * [source,$lang]
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
 * An `accept` header can have multiple MIME types separated by '`,`'.
 *
 * MIME types can also have a `q` value appended to them* which signifies a weighting to apply if more than one
 * response MIME type is available matching the accept header. The q value is a number between 0 and 1.0.
 * If omitted it defaults to 1.0.
 *
 * For example, the following `accept` header signifies the client will accept a MIME type of only `text/plain`:
 *
 *  Accept: text/plain
 *
 * With the following the client will accept `text/plain` or `text/html` with no preference.
 *
 *  Accept: text/plain, text/html
 *
 * With the following the client will accept `text/plain` or `text/html` but prefers `text/html` as it has a higher
 * `q` value (the default value is q=1.0)
 *
 *  Accept: text/plain; q=0.9, text/html
 *
 * If the server can provide both text/plain and text/html it should provide the text/html in this case.
 *
 * By using {@link io.vertx.ext.apex.Route#produces} you define which MIME type(s) the route produces, e.g. the
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
 *  Accept: application/json, text/html
 *  Accept: application/json;q=0.7, text/html;q=0.8, text/plain
 *
 * You can also mark your route as producing more than one MIME type. If this is the case, then you use
 * {@link io.vertx.ext.apex.RoutingContext#getAcceptableContentType} to find out the actual MIME type that
 * was accepted.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example17}
 * ----
 *
 * In the above example, if you sent a request with the following `accept` header:
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
 * [source,$lang]
 * ----
 * {@link examples.Examples#example18}
 * ----
 *
 * == Enabling and disabling routes
 *
 * You can disable a route with {@link io.vertx.ext.apex.Route#disable}. A disabled route will be ignored when matching.
 *
 * You can re-enable a disabled route with {@link io.vertx.ext.apex.Route#enable}
 *
 * == Context data
 *
 * You can use the context data in the {@link io.vertx.ext.apex.RoutingContext} to maintain any data that you
 * want to share between handlers for the lifetime of the request.
 *
 * Here's an example where one handler sets some data in the context data and a subsequent handler retrieves it:
 *
 * You can use the {@link io.vertx.ext.apex.RoutingContext#put} to put any object, and
 * {@link io.vertx.ext.apex.RoutingContext#get} to retrieve any object from the context data.
 *
 * A request sent to path `/some/path/other` will match both routes.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example21}
 * ----
 *
 * Alternatively you can access the entire context data map with {@link io.vertx.ext.apex.RoutingContext#data}.
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
 * This sub-router will maintain the set of handlers that corresponds to a simple fictional REST API. We will mount that on another
 * router. The full implementation of the REST API is not shown.
 *
 * Here's the sub-router:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example22}
 * ----
 *
 * If this router was used as a top level router, then GET/PUT/DELETE requests to urls like `/products/product1234`
 * would invoke the  API.
 *
 * However, let's say we already have a web-site as described by another router:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example23}
 * ----
 *
 * We can now mount the sub router on the main router, against a mount point, in this case `/productsAPI`
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example24}
 * ----
 *
 * This means the REST API is not accessible via paths like: `/productsAPI/products/product1234`
 *
 * == Default 404 Handling
 *
 * If no routes match for any particular request, Apex will signal a 404 error.
 *
 * This can then be handled by your own error handler, or perhaps the augmented error handler that we supply to use,
 * or if no error handler is provided Apex will send back a basic 404 (Not Found) response.
 *
 * == Error handling
 *
 * As well as setting handlers to handle requests you can also set handlers to handle failures in routing.
 *
 * Failure handlers are used with the exact same route matching criteria that you use with normal handlers.
 *
 * For example you can provide a failure handler that will only handle failures on certain paths, or for certain HTTP methods.
 *
 * This allows you to set different failure handlers for different parts of your application.
 *
 * Here's an example failure handler that will only be called for failure that occur when routing to GET requests
 * to paths that start with `/somepath/`:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example25}
 * ----
 *
 * Failure routing will occur if a handler throws an exception, or if a handler calls
 * {@link io.vertx.ext.apex.RoutingContext#fail} specifying an HTTP status code to deliberately signal a failure.
 *
 * If an exception is caught from a handler this will result in a failure with status code `500` being signalled.
 *
 * When handling the failure, the failure handler is passed the routing context which also allows the failure or failure code
 * to be retrieved so the failure handler can use that to generate a failure response.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example26}
 * ----
 *
 * == Request body handling
 *
 * The {@link io.vertx.ext.apex.handler.BodyHandler} allows you to retrieve request bodies, limit body sizes and handle
 * file uploads.
 *
 * You should make sure a body handler is on a matching route for any requests that require this functionality.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example27}
 * ----
 *
 * === Getting the request body
 *
 * If you know the request body is JSON, then you can use {@link io.vertx.ext.apex.RoutingContext#getBodyAsJson},
 * if you know it's a string you can use {@link io.vertx.ext.apex.RoutingContext#getBodyAsString}, or to
 * retrieve it as a buffer use {@link io.vertx.ext.apex.RoutingContext#getBody()}.
 *
 * === Limiting body size
 *
 * To limit the size of a request body, create the body handler with {@link io.vertx.ext.apex.handler.BodyHandler#create(long)}
 * specifying the maximum body size, in bytes. This is useful to avoid running out of memory with very large bodies.
 *
 * If an attempt to send a body greater than the maximum size is made, an HTTP status code of 413 - `Request Entity Too Large`,
 * will be sent.
 *
 * There is no body limit by default.
 *
 * === Handling file uploads
 *
 * Body handler is also used to handle multi-part file uploads.
 *
 * If a body handler is on a matching route for the request, any file uploads will be automatically streamed to the
 * uploads directory, which is `file-uploads` by default.
 *
 * Each file will be given an automatically generated file name, and the file uploads will be available on the routing
 * context with {@link io.vertx.ext.apex.RoutingContext#fileUploads()}.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example28}
 * ----
 *
 * Each file upload is described by a {@link io.vertx.ext.apex.FileUpload} instance, which allows various properties
 * such as the name, file-name and size to be accessed.
 *
 * == Handling cookies
 *
 * Apex has cookies support using the {@link io.vertx.ext.apex.handler.CookieHandler}.
 *
 * You should make sure a cookie handler is on a matching route for any requests that require this functionality.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example29}
 * ----
 *
 * === Manipulating cookies
 *
 * You use {@link io.vertx.ext.apex.RoutingContext#getCookie(java.lang.String)} to retrieve
 * a cookie by name, or use {@link io.vertx.ext.apex.RoutingContext#cookies()} to retrieve the entire set.
 *
 * To remove a cookie, use {@link io.vertx.ext.apex.RoutingContext#removeCookie(java.lang.String)}.
 *
 * To add a cookie use {@link io.vertx.ext.apex.RoutingContext#addCookie(Cookie)}.
 *
 * The set of cookies will be written back in the response automatically when the response headers are written so the
 * browser can store them.
 *
 * Cookies are described by instances of {@link io.vertx.ext.apex.Cookie}. This allows you to retrieve the name,
 * value, domain, path and other normal cookie properties.
 *
 * Here's an example of querying and adding cookies:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example30}
 * ----
 *
 * == Handling sessions
 *
 * Apex provides out of the box support for sessions.
 *
 * Sessions last between HTTP requests for the length of a browser session and give you a place where you can add
 * session-scope information, such as a shopping basket.
 *
 * Apex uses session cookies to identify a session. The session cookie is temporary and will be deleted by your browser
 * when it's closed.
 *
 * We don't put the actual data of your session in the session cookie - the cookie simply uses an identifier to look-up
 * the actual session on the server. The identifier is a random UUID generated using a secure random, so it should
 * be effectively unguessable.
 *
 * Cookies are passed across the wire in HTTP requests and responses so it's always wise to make sure you are using
 * HTTPS when sessions are being used. Vert.x will warn you if you attempt to use sessions over straight HTTP.
 *
 * To enable sessions in your application you must have a {@link io.vertx.ext.apex.handler.SessionHandler}
 * on a matching route before your application logic.
 *
 * The session handler handles the creation of session cookies and the lookup of the session so you don't have to do
 * that yourself.
 *
 * === Session stores
 *
 * To create a session handler you need to have a session store instance. The session store is the object that
 * holds the actual sessions for your application.
 *
 * Apex comes with two session store implementations out of the box, and you can also write your own if you prefer.
 *
 * ==== Local session store
 *
 * With this store, sessions are stored locally in memory and only available in this instance.
 *
 *
 * This store is appropriate if you are using sticky sessions in your application and have configured your load balancer
 * (if you have one) to always route HTTP requests to the same Vert.x instance.
 *
 * If you can't ensure your requests will all terminate on the same server then don't use this store as your
 * requests might end up on a server which doesn't know about your session.
 *
 * Local session stores are implemented by using a shared local map, and have a reaper which clears out expired sessions.
 *
 * The reaper period can be configured with
 * {@link io.vertx.ext.apex.sstore.LocalSessionStore#create(io.vertx.core.Vertx, java.lang.String, long)}.
 *
 * Here are some examples of creating a {@link io.vertx.ext.apex.sstore.LocalSessionStore}
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example31}
 * ----
 *
 * ==== Clustered session store
 *
 * With this store, sessions are stored in a distributed map which is accessible across the Vert.x cluster.
 *
 * This store is appropriate if you're _not_ using sticky sessions, i.e. your load balancer is distributing different
 * requests from the same browser to different servers.
 *
 * Your session is accessible from any node in the cluster using this store.
 *
 * To you use a clustered session store you should make sure your Vert.x instance is clustered.
 *
 * Here are some examples of creating a {@link io.vertx.ext.apex.sstore.ClusteredSessionStore}
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example32}
 * ----
 *
 * === Creating the session handler
 *
 * Once you've created a session store you can create a session handler, and add it to a route. You should make sure
 * your session handler is routed to before your application handlers.
 *
 * You'll also need to include a {@link io.vertx.ext.apex.handler.CookieHandler} as the session handler uses cookies to
 * lookup the session. The cookie handler should be before the session handler when routing.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example33}
 * ----
 *
 * The session handler will ensure that your session is automatically looked up (or created if no session exists)
 * from the session store and set on the routing context before it gets to your application handlers.
 *
 * === Using the session
 *
 * In your handlers you an access the session instance with {@link io.vertx.ext.apex.RoutingContext#session()}.
 *
 * You put data into the session with {@link io.vertx.ext.apex.Session#put(java.lang.String, java.lang.Object)},
 * you get data from the session with {@link io.vertx.ext.apex.Session#get(java.lang.String)}, and you remove
 * data from the session with {@link io.vertx.ext.apex.Session#remove(java.lang.String)}.
 *
 * The keys for items in the session are always strings. The values can be any type for a local session store, and for
 * a clustered session store they can be any basic type, or {@link io.vertx.core.buffer.Buffer}, {@link io.vertx.core.json.JsonObject},
 * {@link io.vertx.core.json.JsonArray} or a serializable object, as the values have to serialized across the cluster.
 *
 * Here's an example of manipulating session data:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example34}
 * ----
 *
 * Sessions are automatically written back to the store after after every response that has been routed through the
 * session handler has been written.
 *
 * You can manually destroy a session using {@link io.vertx.ext.apex.Session#destroy()}. This will remove the session
 * from the context and the session store. Note that if there is no session a new one will be automatically created
 * for the next request from the browser that's routed through the session handler.
 *
 * == Authentication / authorisation
 *
 * Vert.x comes with some out of the box handlers for handling both authentication (login) and authorisation (seeing
 * whether you have rights for some resource).
 *
 * === Creating an auth handler
 *
 * To create an auth handler you need an instance of {@link io.vertx.ext.auth.AuthService}. Auth service is
 * (unsurprisingly) a Vert.x service that is used for authentication and authorisation of users. It uses a simple
 * role/permission model and, by default, is backed by Apache Shiro. For full information on the auth service and how
 * to use and configure it please consult the auth service documentation.
 *
 * Like many services in Vert.x they can be instantiated locally, or you can create a proxy to an existing auth service
 * deployed as a verticle somewhere on the network. The latter case is useful if you have an app composed of many verticles
 * that want to do auth and you don't want each verticle to have its own auth service instance, or perhaps you have a single
 * auth service managed somewhere on your network and you want all auth request to go through that.
 *
 * Here's a simple example of creating a basic auth service that gets user data from a properties file and creating
 * an auth handler from that, but it's the same principle whatever concrete auth service you use.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example35}
 * ----
 *
 * And here's an example of creating an auth service proxy to an existing auth service that is deployed elsewhere:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example36}
 * ----
 *
 * You'll also need cookies and sessions enabled for auth handling to work:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example37}
 * ----
 *
 * === Handling auth in your application
 *
 * Let's say you want all requests to paths that start with `/private/` to be subject to auth. To do that you make sure
 * your auth handler is before your application handlers on those paths:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example38}
 * ----
 *
 * If the auth handler has successfully authenticated and authorised the user it will set the principal (username) on the
 * session object, and the session will be marked as logged in. You can query the logged in status and get the
 * principal with {@link io.vertx.ext.apex.Session#isLoggedIn()} and {@link io.vertx.ext.apex.Session#getPrincipal()}.
 *
 * If you want to cause the user to be logged out you can call {@link io.vertx.ext.apex.Session#logout()}.
 *
 * === HTTP Basic Authentication
 *
 * http://en.wikipedia.org/wiki/Basic_access_authentication[HTTP Basic Authentication] is a simple means of authentication
 * that can be appropriate for simple applications.
 *
 * With basic auth, credentials are sent unencrypted across the wire in HTTP headers so it's essential that you serve
 * your application using HTTPS not HTTP.
 *
 * With basic auth, if a user requests a resource that requires authorisation, the basic auth handler will send back
 * a `401` response with the header `WWW-Authenticate` set. This prompts the browser to show a log-in dialogue and
 * prompt the user to enter their username and password.
 *
 * The request is made to the resource again, this time with the `Authorization` header set, containing the username
 * and password encoded in Base64.
 *
 * When the basic auth handler receives this information, it calls the configured {@link io.vertx.ext.auth.AuthService auth service}
 * with the username and password to authenticate the user. If the authentication is successful the handler attempts
 * to authorise the user. If that is successful then the routing of the request is allowed to continue to the application
 * handlers, otherwise a `403` response is returned to signify that access is denied.
 *
 * The auth handler can be set-up with a set of permissions and/or roles that are required for access to the resources to
 * be granted.
 *
 * === Redirect auth handler
 *
 * With redirect auth handling the user is redirected to towards a login page in the case they are trying to access
 * a protected resource and they are not logged in.
 *
 * The user then fills in the login form and submits it. This is handled by the server which authenticates
 * the user and, if authenticated redirects the user back to the original resource.
 *
 * To use redirect auth you configure an instance of {@link io.vertx.ext.apex.handler.RedirectAuthHandler} instead of a
 * basic auth handler.
 *
 * You will also need to setup handlers to serve your actual login page, and a handler to handle the actual login itself.
 * To handle the login we provide a prebuilt handler {@link io.vertx.ext.apex.handler.FormLoginHandler} for the purpose.
 *
 * Here's an example of a simple app, using a redirect auth handler on the default redirect url `/loginpage`.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example39}
 * ----
 *
 * === Configuring required roles and permissions
 *
 * With any auth handler you can also configure required roles and permissions to access the resource.
 *
 * By default, if no roles/permissions are configured then it is sufficient to be logged in to access the resource, otherwise
 * the user must be both logged in (authenticated) and have the required roles/permissions.
 *
 * Here's an example of configuring an app so that different roles/permissions are required for different parts of the
 * app:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example40}
 * ----
 *
 * === OAuth
 *
 * TODO
 *
 * == Serving static resources
 *
 * Apex comes with an out of the box handler for serving static web resources so you can write static web servers
 * very easily.
 *
 * To serve static resources such as `.html`, `.css`, `.js` or any other static resource, you use an instance of
 * {@link io.vertx.ext.apex.handler.StaticHandler}.
 *
 * Any requests to paths handled by the static handler will result in files being served from a directory on the file system
 * or from the classpath. The default static file directory is `webroot` but this can be configured.
 *
 * In the following example all requests to paths starting with `/static/` will get served from the directory `webroot`:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example41}
 * ----
 *
 * For example, if there was a request with path `/static/css/mystyles.css` the static serve will look for a file in the
 * directory `webroot/static/css/mystyle.css`.
 *
 * It will also look for a file on the classpath called `webroot/static/css/mystyle.css`. This means you can package up all your
 * static resources into a jar file (or fatjar) and distribute them like that.
 *
 * When Vert.x finds a resource on the classpath for the first time it extracts it and caches it in a temporary directory
 * on disk so it doesn't have to do this each time.
 *
 * === Configuring caching
 *
 * By default the static handler will set cache headers to enable browsers to effectively cache files.
 *
 * Apex sets the headers `cache-control`,`last-modified`, and `date`.
 *
 * `cache-control` is set to `max-age=86400` by default. This corresponds to one day. This can be configured with
 * {@link io.vertx.ext.apex.handler.StaticHandler#setMaxAgeSeconds(long)} if required.
 *
 * If a browser sends a GET or a HEAD request with an `if-modified-since` header and the resource has not been modified
 * since that date, a `304` status is returned which tells the browser to use its locally cached resource.
 *
 * If handling of cache headers is not required, it can be disabled with {@link io.vertx.ext.apex.handler.StaticHandler#setCachingEnabled(boolean)}.
 *
 * When cache handling is enabled Apex will cache the last modified date of resources in memory, this avoids a disk hit
 * to check the actual last modified date every time.
 *
 * Entries in the cache have an expiry time, and after that time, the file on disk will be checked again and the cache
 * entry updated.
 *
 * If you know that your files never change on disk, then the cache entry will effectively never expire. This is the
 * default.
 *
 * If you know that your files might change on disk when the server is running then you can set files read only to false with
 * {@link io.vertx.ext.apex.handler.StaticHandler#setFilesReadOnly(boolean)}.
 *
 * To enable the maximum number of entries that can be cached in memory at any one time you can use
 * {@link io.vertx.ext.apex.handler.StaticHandler#setMaxCacheSize(int)}.
 *
 * To configure the expiry time of cache entries you can use {@link io.vertx.ext.apex.handler.StaticHandler#setCacheEntryTimeout(long)}.
 *
 * === Configuring the index page
 *
 * Any requests to the root path `/` will cause the index page to be served. By default the index page is `index.html`.
 * This can be configured with {@link io.vertx.ext.apex.handler.StaticHandler#setIndexPage(java.lang.String)}.
 *
 * === Changing the web root
 *
 * By default static resources will be served from the directory `webroot`. To configure this use
 * {@link io.vertx.ext.apex.handler.StaticHandler#setWebRoot(java.lang.String)}.
 *
 * === Serving hidden files
 *
 * By default the serve will serve hidden files (files starting with `.`).
 *
 * If you do not want hidden files to be served you can configure it with {@link io.vertx.ext.apex.handler.StaticHandler#setIncludeHidden(boolean)}.
 *
 * === Directory listing
 *
 * The server can also perform directory listing. By default directory listing is disabled. To enabled it use
 * {@link io.vertx.ext.apex.handler.StaticHandler#setDirectoryListing(boolean)}.
 *
 * When directory listing is enabled the content returned depends on the content type in the `accept` header.
 *
 * For `text/html` directory listing, the template used to render the directory listing page can be configured with
 * {@link io.vertx.ext.apex.handler.StaticHandler#setDirectoryTemplate(java.lang.String)}.
 *
 * == CORS handling
 *
 * http://en.wikipedia.org/wiki/Cross-origin_resource_sharing[Cross Origin Resource Sharing] is a safe mechanism for
 * allowing resources to be requested from one domain and served from another.
 *
 * Apex includes a handler {@link io.vertx.ext.apex.handler.CorsHandler} that handles the CORS protocol for you.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example41_0_1}
 * ----
 *
 * TODO more CORS docs
 *
 * == Templates
 *
 * Apex includes dynamic page generation capabilities by including out of the box support for several popular template
 * engines. You can also easily add your own.
 *
 * Template engines are described by {@link io.vertx.ext.apex.templ.TemplateEngine}. In order to render a template
 * {@link io.vertx.ext.apex.templ.TemplateEngine#render} is used.
 *
 * The simplest way to use templates is not to call the template engine directly but to use the
 * {@link io.vertx.ext.apex.handler.TemplateHandler}.
 * This handler calls the template engine for you based on the path in the HTTP request.
 *
 * By default the template handler will look for templates in a directory called `templates`. This can be configured.
 *
 * The handler will return the results of rendering with a content type of `text/html` by default. This can also be configured.
 *
 * When you create the template handler you pass in an instance of the template engine you want.
 *
 * Here are some examples
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example41_1}
 * ----
 *
 * === MVEL template engine
 *
 * When using the {@link io.vertx.ext.apex.templ.MVELTemplateEngine MVEL template engine}, it will by default look for
 * templates with the `.templ` extension if no extension is specified in the file name.
 *
 * The routing context {@link io.vertx.ext.apex.RoutingContext} is available
 * in the MVEL template as the `context` variable, this means you can render the template based on anything in the context
 * including the request, response, session or context data.
 *
 * Here are some examples:
 *
 * ----
 * The request path is @{context.request().path()}
 *
 * The variable 'foo' from the session is @{context.session().get('foo')}
 *
 * The value 'bar' from the context data is @{context.get('bar')}
 * ----
 *
 * Please consult the http://mvel.codehaus.org/MVEL+2.0+Templating+Guide[MVEL templates documentation] for how to write
 * MVEL templates.
 *
 * === Jade template engine
 *
 * When using the {@link io.vertx.ext.apex.templ.JadeTemplateEngine Jade template engine}, it will by default look for
 * templates with the `.jade` extension if no extension is specified in the file name.
 *
 * The routing context {@link io.vertx.ext.apex.RoutingContext} is available
 * in the Jade template as the `context` variable, this means you can render the template based on anything in the context
 * including the request, response, session or context data.
 *
 * Here are some examples:
 *
 * ----
 * !!! 5
 * html
 *   head
 *     title= context.get('foo') + context.request().path()
 *   body
 * ----
 *
 * Please consult the https://github.com/neuland/jade4j[Jade4j documentation] for how to write
 * Jade templates.
 *
 * === Handlebars template engine
 *
 * When using the {@link io.vertx.ext.apex.templ.HandlebarsTemplateEngine Handlebars template engine}, it will by default look for
 * templates with the `.hbs` extension if no extension is specified in the file name.
 *
 * Handlebars templates are not able to call arbitrary methods in objects so we can't just pass the routing context
 * into the template and let the template introspect it like we can with other template engines.
 *
 * Instead, the context {@link io.vertx.ext.apex.RoutingContext#data()} is available in the template.
 *
 * If you want to have access to other data like the request path, request params or session data you should
 * add it the context data in a handler before the template handler. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example41_2}
 * ----
 *
 * Please consult the https://github.com/jknack/handlebars.java[Handlebars Java port documentation] for how to write
 * handlebars templates.
 *
 * === Thymeleaf template engine
 * 
 * When using the {@link io.vertx.ext.apex.templ.ThymeleafTemplateEngine Thymeleaf template engine}, it will by default look for
 * templates with the `.html` extension if no extension is specified in the file name.
 *
 * The routing context {@link io.vertx.ext.apex.RoutingContext} is available
 * in the Thymeleaf template as the `context` variable, this means you can render the template based on anything in the context
 * including the request, response, session or context data.
 *
 * Here are some examples:
 *
 * ----
 * [snip]
 * &lt;p th:text="${context.get('foo')}"&gt;&lt;/p&gt;
 * &lt;p th:text="${context.get('bar')}"&gt;&lt;/p&gt;
 * &lt;p th:text="${context.normalisedPath()}"&gt;&lt;/p&gt;
 * &lt;p th:text="${context.request().params().get('param1')}"&gt;&lt;/p&gt;
 * &lt;p th:text="${context.request().params().get('param2')}"&gt;&lt;/p&gt;
 * [snip]
 * ----
 *
 * Please consult the http://www.thymeleaf.org/[Thymeleaf documentation] for how to write
 * Thymeleaf templates.
 *
 * == Error handler
 *
 * You can render your own errors using a template handler or otherwise but Apex also includes an out of the boxy
 * "pretty" error handler that can render error pages for you.
 *
 * The handler is {@link io.vertx.ext.apex.handler.ErrorHandler}. To use the error handler just set it as a
 * failure handler for any paths that you want covered.
 *
 * == Request logger
 *
 * Apex includes a handler {@link io.vertx.ext.apex.handler.LoggerHandler} that you can use to log HTTP requests.
 *
 *
 * By default requests are logged to the Vert.x logger which can be configured to use JUL logging, log4j or SLF4J.
 *
 * == Serving favicons
 *
 * Apex includes the handler {@link io.vertx.ext.apex.handler.FaviconHandler} especially for serving favicons.
 *
 * Favicons can be specified using a path to the filesystem, or by default Apex will look for a file on the classpath
 * with the name `favicon.ico`. This means you bundle the favicon in the jar of your application.
 *
 * == Timeout handler
 *
 * Apex includes a timeout handler that you can use to timeout requests if they take too long to process.
 *
 * This is configured using an instance of {@link io.vertx.ext.apex.handler.TimeoutHandler}.
 *
 * If a request times out before the response is written a `408` response will be returned to the client.
 *
 * Here's an example of using a timeout handler which will timeout all requests to paths starting with `/foo` after 5
 * seconds:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example42}
 * ----
 *
 * == Response time handler
 *
 * This handler sets the header `x-response-time` response header containing the time from when the request was received
 * to when the response headers were written, in ms., e.g.:
 *
 *  x-response-time: 1456ms
 *
 * == SockJS
 *
 * SockJS is a client side JavaScript library and protocol which provides a simple WebSocket-like interface allowing you
 * to make connections to SockJS servers irrespective of whether the actual browser or network will allow real WebSockets.
 *
 * It does this by supporting various different transports between browser and server, and choosing one at run-time
 * according to browser and network capabilities.
 *
 * All this is transparent to you - you are simply presented with the WebSocket-like interface which _just works_.
 *
 * Please see the https://github.com/sockjs/sockjs-client[SockJS website] for more information on SockJS.
 *
 * === SockJS handler
 *
 * Vert.x provides an out of the box handler called {@link io.vertx.ext.apex.handler.sockjs.SockJSHandler} for
 * using SockJS in your Apex applications.
 *
 * You should create one handler per SockJS application using {@link io.vertx.ext.apex.handler.sockjs.SockJSHandler#create}.
 * You can also specify configuration options when creating the instance. The configuration options are described with
 * an instance of {@link io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions}.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example43}
 * ----
 *
 * === Handling SockJS sockets
 *
 * On the server-side you set a handler on the SockJS handler, and
 * this will be called every time a SockJS connection is made from a client:
 *
 * The object passed into the handler is a {@link io.vertx.ext.apex.handler.sockjs.SockJSSocket}. This has a familiar
 * socket-like interface which you can read and write to similarly to a {@link io.vertx.core.net.NetSocket} or
 * a {@link io.vertx.core.http.WebSocket}. It also implements {@link io.vertx.core.streams.ReadStream} and
 * {@link io.vertx.core.streams.WriteStream} so you can pump it to and from other read and write streams.
 *
 * Here's an example of a simple SockJS handler that simply echoes back any back any data that it reads:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example44}
 * ----
 *
 * === The client side
 *
 * In client side JavaScript you use the SockJS client side library to make connections.
 *
 * You can find that http://cdn.sockjs.org/sockjs-0.3.4.js[here].
 * The minified version is http://cdn.sockjs.org/sockjs-0.3.4.min.js[here].
 *
 * Full details for using the SockJS JavaScript client are on the https://github.com/sockjs/sockjs-client[SockJS website],
 * but in summary you use it something like this:
 *
 * ----
 * var sock = new SockJS('http://mydomain.com/myapp');
 *
 * sock.onopen = function() {
 *   console.log('open');
 * };
 *
 * sock.onmessage = function(e) {
 *   console.log('message', e.data);
 * };
 *
 * sock.onclose = function() {
 *   console.log('close');
 * };
 *
 * sock.send('test');
 *
 * sock.close();
 * ----
 *
 * === Configuring the SockJS handler
 *
 * The handler can be configured with various options using {@link io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions}.
 *
 * `insertJSESSIONID`:: Insert a JSESSIONID cookie so load-balancers ensure requests for a specific SockJS session
 * are always routed to the correct server. Default is `true`.
 * `sessionTimeout`:: The server sends a `close` event when a client receiving connection have not been seen for a while.
 * This delay is configured by this setting. By default the `close` event will be emitted when a receiving
 * connection wasn't seen for 5 seconds.
 * `heartbeaPperiod`:: In order to keep proxies and load balancers from closing long running http
 * requests we need to pretend that the connection is active and send a heartbeat packet once in a while.
 * This setting controls how often this is done. By default a heartbeat packet is sent every 25 seconds.
 * `maxBytesStreaming`:: Most streaming transports save responses on the client side and don't free memory used
 * by delivered messages. Such transports need to be garbage-collected once in a while. `max_bytes_streaming` sets a
 * minimum number of bytes that can be send over a single http streaming request before it will be closed. After that
 * client needs to open new request. Setting this value to one effectively disables streaming and will make streaming
 * transports to behave like polling transports. The default value is 128K.
 * `libraryURL`:: Transports which don't support cross-domain communication natively ('eventsource' to name one)
 * use an iframe trick. A simple page is served from the SockJS server (using its foreign domain) and is placed in an
 * invisible iframe. Code run from this iframe doesn't need to worry about cross-domain issues, as it's being run from
 * domain local to the SockJS server. This iframe also does need to load SockJS javascript client library, and this option
 * lets you specify its url (if you're unsure, point it to the latest minified SockJS client release, this is the default).
 * The default value is `http://cdn.sockjs.org/sockjs-0.3.4.min.js`
 * `disabledTransports`:: This is a list of transports that you want to disable. Possible values are
 * WEBSOCKET, EVENT_SOURCE, HTML_FILE, JSON_P, XHR.
 *
 * == SockJS event bus bridge
 *
 * Apex comes with a built-in SockJS socket handler called the event bus bridge which effectively extends the server-side
 * Vert.x event bus into client side JavaScript.
 *
 * This creates a distributed event bus which not only spans multiple Vert.x instances on the server side, but includes
 * client side JavaScript running in browsers.
 *
 * We can therefore create a huge distributed bus encompassing many browsers and servers. The browsers don't have to
 * be connected to the same server as long as the servers are connected.
 *
 * This is done by providing a simple client side JavaScript library called `vertxbus.js` which provides an API
 * very similar to the server-side Vert.x event-bus API, which allows you to send and publish messages to the event bus
 * and register handlers to receive messages.
 *
 * This JavaScript library uses the JavaScript SockJS client to tunnel the event bus traffic over SockJS connections
 * terminating at at a {@link io.vertx.ext.apex.handler.sockjs.SockJSHandler} on the server-side.
 *
 * A special SockJS socket handler is then installed on the {@link io.vertx.ext.apex.handler.sockjs.SockJSHandler} which
 * handles the SockJS data and bridges it to and from the server side event bus.
 *
 * To activate the bridge you simply call
 * {@link io.vertx.ext.apex.handler.sockjs.SockJSHandler#bridge(io.vertx.ext.apex.handler.sockjs.BridgeOptions)} on the
 * SockJS handler.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example45}
 * ----
 *
 * In client side JavaScript you use the 'vertxbus.js` library to create connections to the event bus and to send
 * and receive messages:
 *
 * ----
 * <script src="http://cdn.sockjs.org/sockjs-0.3.4.min.js"></script>
 * <script src='vertxbus.js'></script>
 *
 * <script>
 *
 * var eb = new vertx.EventBus('http://localhost:8080/eventbus');
 *
 * eb.onopen = function() {
 *
 *   // set a handler to receive a message
 *   eb.registerHandler('some-address', function(message) {
 *     console.log('received a message: ' + JSON.stringify(message);
 *   });
 *
 *   // send a message
 *   eb.send('some-address', {name: 'tim', age: 587});
 *
 * }
 *
 * </script>
 * ----
 *
 * The first thing the example does is to create a instance of the event bus
 *
 *  var eb = new vertx.EventBus('http://localhost:8080/eventbus');
 *
 * The parameter to the constructor is the URI where to connect to the event bus. Since we create our bridge with
 * the prefix `eventbus` we will connect there.
 *
 * You can't actually do anything with the connection until it is opened. When it is open the `onopen` handler will be called.
 *
 * === Securing the Bridge
 *
 * If you started a bridge like in the above example without securing it, and attempted to send messages through
 * it you'd find that the messages mysteriously disappeared. What happened to them?
 *
 * For most applications you probably don't want client side JavaScript being able to send just any message to any
 * handlers on the server side or to all other browsers.
 *
 * For example, you may have a service on the event bus which allows data to be accessed or deleted. We don't want
 * badly behaved or malicious clients being able to delete all the data in your database!
 *
 * Also, we don't necessarily want any client to be able to listen in on any event bus address.
 *
 * To deal with this, a SockJS bridge will by default refuse to let through any messages. It's up to you to tell the
 * bridge what messages are ok for it to pass through. (There is an exception for reply messages which are always allowed through).
 *
 * In other words the bridge acts like a kind of firewall which has a default _deny-all_ policy.
 *
 * Configuring the bridge to tell it what messages it should pass through is easy.
 *
 * You can specify which _matches_ you want to allow for inbound and outbound traffic using the
 * {@link io.vertx.ext.apex.handler.sockjs.BridgeOptions} that you pass in when calling bridge.
 *
 * Each match is a Json object that can contain several fields.
 *
 * `address`:: This represents the exact address the message is being sent to. If you want to allow messages based on
 * an exact address you use this field.
 * `address_re`:: This is a regular expression that will be matched against the address. If you want to allow messages
 * based on a regular expression you use this field. If the `address` field is specified this field will be ignored.
 * `match`:: This allows you to allow messages based on their structure. Any fields in the match must exist in the
 * message with the same values for them to be allowed. This currently only works with JSON messages.
 *
 * If a message is _in-bound_ (i.e. being sent from client side JavaScript to the server) when it is received Apex
 * will look through any inbound permitted matches. If any match, it will be allowed through.
 *
 * If a message is _out-bound_ (i.e. being sent from the server to client side JavaScript) before it is sent to the client
 * Apex will look through any inbound permitted matches. If any match, it will be allowed through.
 *
 * The actual matching works as follows:
 *
 * If an `address` field has been specified then the `address` must match _exactly_ with the address of the message
 * for it to be considered matched.
 *
 * If an `address` field has not been specified and an `address_re` field has been specified then the regular expression
 * in `address_re` must match with the address of the message for it to be considered matched.
 *
 * If a `match` field has been specified, then also the structure of the message must match. Structuring matching works
 * by looking at all the fields and values in the match object and checking they all exist in the actual message body.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example46}
 * ----
 *
 * === Requiring authorisation for messages
 *
 * The event bus bridge can also be configured to use the Apex authorisation functionality to require
 * authorisation for messages, either in-bound or out-bound on the bridge.
 *
 * To do this, you can add extra fields to the match described in the previous section that determine what role and/or
 * permission is required for the match.
 *
 * To declare that a specific role for the logged-in user is required in order to access allow the messages you use the
 * `required_role` field.
 *
 * To declare that a specific permission for the logged-in user is required in order to access allow the messages you use the
 * `required_permission` field.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example47}
 * ----
 *
 * For the user to be authorised they must be first logged in and secondly have the required role.
 *
 * To handle the login and actually auth you can configure the normal Vert.x auth handlers. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example48}
 * ----
 *
 *
 *
 *
 *
 *
 */
@Document(fileName = "index.adoc")
@GenModule(name = "vertx-apex")
package io.vertx.ext.apex;

import io.vertx.codegen.annotations.GenModule;
import io.vertx.docgen.Document;
