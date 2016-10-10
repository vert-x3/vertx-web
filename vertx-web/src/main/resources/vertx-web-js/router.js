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

   @public
   @param arg0 {HttpServerRequest} 
   */
  this.accept = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["accept(io.vertx.core.http.HttpServerRequest)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {Object} 
   @param arg1 {string} 
   @return {Route}
   */
  this.route = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["route()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["route(java.lang.String)"](__args[0]), Route);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(j_router["route(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {Object} 
   @param arg1 {string} 
   @return {Route}
   */
  this.routeWithRegex = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["routeWithRegex(java.lang.String)"](__args[0]), Route);
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return utils.convReturnVertxGen(j_router["routeWithRegex(io.vertx.core.http.HttpMethod,java.lang.String)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.get = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["get()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["get(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.getWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["getWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.head = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["head()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["head(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.headWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["headWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.options = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["options()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["options(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.optionsWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["optionsWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.put = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["put()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["put(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.putWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["putWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.post = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["post()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["post(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.postWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["postWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.delete = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["delete()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["delete(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.deleteWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["deleteWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.trace = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["trace()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["trace(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.traceWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["traceWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.connect = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["connect()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["connect(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.connectWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["connectWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.patch = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_router["patch()"](), Route);
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["patch(java.lang.String)"](__args[0]), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.patchWithRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnVertxGen(j_router["patchWithRegex(java.lang.String)"](arg0), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<Route>}
   */
  this.getRoutes = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_router["getRoutes()"](), Route);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Router}
   */
  this.clear = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_router["clear()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @param arg1 {Router} 
   @return {Router}
   */
  this.mountSubRouter = function(arg0, arg1) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1]._jdel) {
      j_router["mountSubRouter(java.lang.String,io.vertx.ext.web.Router)"](arg0, arg1._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   @return {Router}
   */
  this.exceptionHandler = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_router["exceptionHandler(io.vertx.core.Handler)"](function(jVal) {
      arg0(utils.convReturnThrowable(jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handleContext = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["handleContext(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handleFailure = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router["handleFailure(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_router;
};

/**

 @memberof module:vertx-web-js/router
 @param vertx {Vertx} 
 @return {Router}
 */
Router.router = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JRouter["router(io.vertx.core.Vertx)"](vertx._jdel), Router);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = Router;