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
    
Your application is simply a set of handlers wired together in specific ways using routers.
  
You wire a `Router` to your `HttpServer` as follows:
    
    HttpServer server = ... // Create your Http Server as normal
    server.requestHandler(router::accept);
    
    
## Features

Apex core features:

* Routing (based on method, path, etc)
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





