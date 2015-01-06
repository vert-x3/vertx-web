# Apex

Apex is a set of building blocks for building web applications with Vert.x. Think of it as a Swiss Army Knife for building 
modern, scalable, web apps.

It's the successor to [Yoke](http://pmlopes.github.io/yoke/) in Vert.x 2.x, and takes inspiration from projects such
as [Express](http://expressjs.com/) in the Node.js world.

Apex is designed to be powerful, unopionated and fully embeddable. You just use the parts you want and nothing more. It's not a
container.

You can use Apex to create classic server side web applications, RESTful web applications, 'real-time' (server push) web applications,
or any other kind of web application you can think of. Apex doesn't care. It's up to you to chose the type of app you prefer, not Apex.
 

## Introduction

Everything in Apex is based around wiring together handlers. The object that lets you wire them together is called a 
`Router`. A `Router` typically corresponds to a single web application or perhaps a part of larger web application.

You tell a `Router` to route incoming requests to specific handlers depending on various criteria such as the HTTP method,
the path, and `content-type` or `accepts` headers (content negotiation).

Here's the obligatory hello world router which responds to all requests with the string "hello world".

    Router router = Router.router();
    router.route().handler(rc -> rc.response.end("hello world");
    
Here's a more complex example
    
    Router router = Router.router();
    
    // Paths starting with `/static` serve as static resources (from filesystem or classpath)
    router.route("/static").handler(StaticServer.staticServer());
    
    // Paths starting with `/dynamic` return pages generated from handlebars templates 
    router.route("/dynamic").handler(TemplateHandler.templateHandler(HandlebarsTemplateEngine.create()));

    // Create a sub router for our REST API
    Router apiRouter = Router.router();

    // We need body parsing
    apiRouter.route(BodyHandler.bodyHandler());
    
    apiRouter.route("/orders")
             .method(POST)
             .consumes("application/json")
             .handler(context -> {
               JsonObject order = context.getBodyAsJson();
               // .... store the order
               context.response().end(); // Send back 200-OK
             });
             
    // ... more API
                 
    // attach the sub router to the main router at the mount point "/api"
    router.mountSubRouter("/api", apiRouter);
            
    
Your application is simply a set of handlers wired together in specific ways using routers.
  
You wire a `Router` to your `HttpServer` as follows:

    // First create your server
    Vertx vertx = Vert.vertx();
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("foo.com");
    
    // Connect your router to your Http Server
    server.requestHandler(router::accept);
    
    server.listen();
    
    
## Features

Apex core features:

* Routing (based on method, path, etc)
* Regex pattern matching for paths
* Extraction of parameters from paths
* Content negotiation
* Request body handling
* Body size limits
* Cookie parsing and handling
* Multipart forms
* Multipart file uploads
* Sub routers

Apex addons include:

* Session support - both local (for sticky sessions) and clustered (for non sticky)
* CORS (Cross Origin Resource Sharing) support
* Error page template
* Basic Authentication
* Redirect based authentication
* User/role/permission authorisation
* Favicon handling
* Template support for server side rendering. Supports: Handlebars, Jade, MVEL and Thymeleaf out of the box
* Response time handler
* Static file serving, including caching logic and directory listing.
* Request timeout support

Most features in Apex are implemented as Handlers so you can always write your own. We envisage many more being written
over time.

## Don't want to use Java?

Apex uses Vert.x 3 codegen so the Apex interfaces are automatically translated to other languages. So you can use Apex
 in any language that Vert.x supports (JavaScript, Ruby, Groovy, etc)
 
You choose the language you want to use. 

## Find out more

See the documentation for detailed information about Apex.





