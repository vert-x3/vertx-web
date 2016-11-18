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

/** @module vertx-web-js/route */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRoute = io.vertx.ext.web.Route;

/**
 A route is a holder for a set of criteria which determine whether an HTTP request or failure should be routed
 to a handler.

 @class
*/
var Route = function(j_val) {

  var j_route = j_val;
  var that = this;

  /**
   Add an HTTP method for this route. By default a route will match all HTTP methods. If any are specified then the route
   will only match any of the specified methods

   @public
   @param method {Object} the HTTP method to add 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.method = function(method) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["method(io.vertx.core.http.HttpMethod)"](io.vertx.core.http.HttpMethod.valueOf(method));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the path prefix for this route. If set then this route will only match request URI paths which start with this
   path prefix. Only a single path or path regex can be set for a route.

   @public
   @param path {string} the path prefix 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.path = function(path) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["path(java.lang.String)"](path);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the path prefix as a regular expression. If set then this route will only match request URI paths, the beginning
   of which match the regex. Only a single path or path regex can be set for a route.

   @public
   @param path {string} the path regex 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.pathRegex = function(path) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["pathRegex(java.lang.String)"](path);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a content type produced by this route. Used for content based routing.

   @public
   @param contentType {string} the content type 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.produces = function(contentType) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["produces(java.lang.String)"](contentType);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a content type consumed by this route. Used for content based routing.

   @public
   @param contentType {string} the content type 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.consumes = function(contentType) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["consumes(java.lang.String)"](contentType);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify the order for this route. The router tests routes in that order.

   @public
   @param order {number} the order 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.order = function(order) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_route["order(int)"](order);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify this is the last route for the router.

   @public

   @return {Route} a reference to this, so the API can be used fluently
   */
  this.last = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["last()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify a request handler for the route. The router routes requests to handlers depending on whether the various
   criteria such as method, path, etc match. There can be only one request handler for a route. If you set this more
   than once it will overwrite the previous handler.

   @public
   @param requestHandler {function} the request handler 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.handler = function(requestHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["handler(io.vertx.core.Handler)"](function(jVal) {
      requestHandler(utils.convReturnVertxGen(RoutingContext, jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify a blocking request handler for the route.
   This method works just like {@link Route#handler} excepted that it will run the blocking handler on a worker thread
   so that it won't block the event loop. Note that it's safe to call context.next() from the
   blocking handler as it will be executed on the event loop context (and not on the worker thread.
  
   If the blocking handler is ordered it means that any blocking handlers for the same context are never executed
   concurrently but always in the order they were called. The default value of ordered is true. If you do not want this
   behaviour and don't mind if your blocking handlers are executed in parallel you can set ordered to false.

   @public
   @param requestHandler {function} the blocking request handler 
   @param ordered {boolean} if true handlers are executed in sequence, otherwise are run in parallel 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.blockingHandler = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["blockingHandler(io.vertx.core.Handler)"](function(jVal) {
      __args[0](utils.convReturnVertxGen(RoutingContext, jVal));
    });
      return that;
    }  else if (__args.length === 2 && typeof __args[0] === 'function' && typeof __args[1] ==='boolean') {
      j_route["blockingHandler(io.vertx.core.Handler,boolean)"](function(jVal) {
      __args[0](utils.convReturnVertxGen(RoutingContext, jVal));
    }, __args[1]);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Specify a failure handler for the route. The router routes failures to failurehandlers depending on whether the various
   criteria such as method, path, etc match. There can be only one failure handler for a route. If you set this more
   than once it will overwrite the previous handler.

   @public
   @param failureHandler {function} the request handler 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.failureHandler = function(failureHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["failureHandler(io.vertx.core.Handler)"](function(jVal) {
      failureHandler(utils.convReturnVertxGen(RoutingContext, jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Remove this route from the router

   @public

   @return {Route} a reference to this, so the API can be used fluently
   */
  this.remove = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["remove()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Disable this route. While disabled the router will not route any requests or failures to it.

   @public

   @return {Route} a reference to this, so the API can be used fluently
   */
  this.disable = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["disable()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Enable this route.

   @public

   @return {Route} a reference to this, so the API can be used fluently
   */
  this.enable = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["enable()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   If true then the normalised request path will be used when routing (e.g. removing duplicate /)
   Default is true

   @public
   @param useNormalisedPath {boolean} use normalised path for routing? 
   @return {Route} a reference to this, so the API can be used fluently
   */
  this.useNormalisedPath = function(useNormalisedPath) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_route["useNormalisedPath(boolean)"](useNormalisedPath);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string} the path prefix (if any) for this route
   */
  this.getPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_route["getPath()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_route;
};

Route._jclass = utils.getJavaClass("io.vertx.ext.web.Route");
Route._jtype = {
  accept: function(obj) {
    return Route._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(Route.prototype, {});
    Route.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
Route._create = function(jdel) {
  var obj = Object.create(Route.prototype, {});
  Route.apply(obj, arguments);
  return obj;
}
module.exports = Route;