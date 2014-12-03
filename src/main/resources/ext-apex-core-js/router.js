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

var utils = require('vertx-js/util/utils');
var RoutingContext = require('ext-apex-core-js/routing_context');
var HttpServerRequest = require('vertx-js/http_server_request');
var FailureRoutingContext = require('ext-apex-core-js/failure_routing_context');
var Route = require('ext-apex-core-js/route');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRouter = io.vertx.ext.apex.core.Router;

/**

  @class
*/
var Router = function(j_val) {

  var j_router = j_val;
  var that = this;

  this.accept = function(request) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router.accept(request._jdel);
    } else utils.invalidArgs();
  };

  this.handleContext = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router.handleContext(context._jdel);
    } else utils.invalidArgs();
  };

  this.handleFailure = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_router.handleFailure(context._jdel);
    } else utils.invalidArgs();
  };

  this.route = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Route(j_router.route());
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Route(j_router.route(__args[0]));
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return new Route(j_router.route(io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]));
    } else utils.invalidArgs();
  };

  this.routeWithRegex = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Route(j_router.routeWithRegex(__args[0]));
    }  else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return new Route(j_router.routeWithRegex(io.vertx.core.http.HttpMethod.valueOf(__args[0]), __args[1]));
    } else utils.invalidArgs();
  };

  this.getRoutes = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_router.getRoutes(), Route);
    } else utils.invalidArgs();
  };

  this.clear = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Router(j_router.clear());
    } else utils.invalidArgs();
  };

  this.mountSubRouter = function(mountPoint, subRouter) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1]._jdel) {
      return new Router(j_router.mountSubRouter(mountPoint, subRouter._jdel));
    } else utils.invalidArgs();
  };

  this.exceptionHandler = function(exceptionHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return new Router(j_router.exceptionHandler(function(jVal) {
      exceptionHandler(utils.convReturnTypeUnknown(jVal));
    }));
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_router;
};

Router.router = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new Router(JRouter.router(vertx._jdel));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Router;