/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module vertx-web-js/router */
var utils = require('vertx-js/util/utils');
var Route = require('vertx-web-js/route');
var HttpServerRequest = require('vertx-js/http_server_request');
var Vertx = require('vertx-js/vertx');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRouter = io.vertx.ext.web.Router;

/**

 @class
*/
var Router = function(j_val) {

  var j_router = j_val;
  var that = this;

  /**
   This method is used to provide a request to the router. Usually you take request from the
   {@link HttpServer#requestHandler} and pass it to this method. The
   router then routes it to matching routes.

   @public
   @param request {HttpServerRequest} the request 
   */
  this.accept = function(request) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["accept(io.vertx.core.http.HttpServerRequest)"](request._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches the specified HTTP method and path

   @public
   @param method {Object} the HTTP method to match 
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.route = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["route()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["route(java.lang.String)"](__args[0]));
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["route(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches the specified HTTP method and path regex

   @public
   @param method {Object} the HTTP method to match 
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.routeWithRegex = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["routeWithRegex(java.lang.String)"](__args[0]));
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["routeWithRegex(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP GET request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.get = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["get()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["get(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP GET request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.getWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["getWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP HEAD request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.head = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["head()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["head(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP HEAD request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.headWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["headWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP OPTIONS request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.options = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["options()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["options(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP OPTIONS request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.optionsWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["optionsWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP PUT request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.put = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["put()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["put(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP PUT request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.putWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["putWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP POST request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.post = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["post()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["post(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP POST request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.postWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["postWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP DELETE request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.delete = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["delete()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["delete(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP DELETE request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.deleteWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["deleteWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP TRACE request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.trace = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["trace()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["trace(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP TRACE request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.traceWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["traceWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP CONNECT request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.connect = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["connect()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["connect(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP CONNECT request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.connectWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["connectWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP PATCH request and the specified path

   @public
   @param path {string} URI paths that begin with this path will match 
   @return {Route} the route
   */
  this.patch = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Route, j_router["patch()"]());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["patch(java.lang.String)"](__args[0]));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a route that matches a HTTP PATCH request and the specified path regex

   @public
   @param regex {string} URI paths that begin with a match for this regex will match 
   @return {Route} the route
   */
  this.patchWithRegex = function(regex) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(Route, j_router["patchWithRegex(java.lang.String)"](regex));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<Route>} a list of all the routes on this router
   */
  this.getRoutes = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_router["getRoutes()"](), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Remove all the routes from this router

   @public

   @return {Router} a reference to this, so the API can be used fluently
   */
  this.clear = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_router["clear()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Mount a sub router on this router

   @public
   @param mountPoint {string} the mount point (path prefix) to mount it on 
   @param subRouter {Router} the router to mount as a sub router 
   @return {Router} a reference to this, so the API can be used fluently
   */
  this.mountSubRouter = function(mountPoint, subRouter) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1]._jdel) {
      j_router["mountSubRouter(java.lang.String,io.vertx.ext.web.Router)"](mountPoint, subRouter._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify a handler for any unhandled exceptions on this router. The handler will be called for exceptions thrown
   from handlers. This does not affect the normal failure routing logic.

   @public
   @param exceptionHandler {function} the exception handler 
   @return {Router} a reference to this, so the API can be used fluently
   */
  this.exceptionHandler = function(exceptionHandler) {
    var __args = arguments;
    if (__args.length === 1 && (typeof __args[0] === 'function' || __args[0] == null)) {
      j_router["exceptionHandler(io.vertx.core.Handler)"](exceptionHandler == null ? null : function(jVal) {
      exceptionHandler(utils.convReturnThrowable(jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Used to route a context to the router. Used for sub-routers. You wouldn't normally call this method directly.

   @public
   @param context {RoutingContext} the routing context 
   */
  this.handleContext = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["handleContext(io.vertx.ext.web.RoutingContext)"](context._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Used to route a failure to the router. Used for sub-routers. You wouldn't normally call this method directly.

   @public
   @param context {RoutingContext} the routing context 
   */
  this.handleFailure = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["handleFailure(io.vertx.ext.web.RoutingContext)"](context._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_router;
};

Router._jclass = utils.getJavaClass("io.vertx.ext.web.Router");
Router._jtype = {
  accept: function(obj) {
    return Router._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(Router.prototype, {});
    Router.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
Router._create = function(jdel) {
  var obj = Object.create(Router.prototype, {});
  Router.apply(obj, arguments);
  return obj;
}
/**
 Create a router

 @memberof module:vertx-web-js/router
 @param vertx {Vertx} the Vert.x instance 
 @return {Router} the router
 */
Router.router = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(Router, JRouter["router(io.vertx.core.Vertx)"](vertx._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = Router;