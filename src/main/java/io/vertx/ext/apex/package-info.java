/**
 * = Apex
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
 * as http://expressjs.com/[Express] in the Node.js world.
 *
 * Apex is designed to be powerful, un-opionated and fully embeddable. You just use the parts you want and nothing more.
 *
 * Apex is not a container.
 *
 * You can use Apex to create classic server side web applications, RESTful web applications, 'real-time' (server push)
 * web applications, or any other kind of web application you can think of. Apex doesn't care. It's up to you to chose the type of app you prefer, not Apex.
 *
 * Apex is a great fit for writing RESTful HTTP microservices, but we don't *force* you to write apps like that.
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
 * Apex addons include:
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
 * Most features in Apex are implemented as Handlers so you can always write your own. We envisage many more being written
 * over time.
 *
 *
 */
@Document(fileName = "index.adoc")
package io.vertx.ext.apex;

import io.vertx.docgen.Document;
