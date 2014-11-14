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
var HttpServerRequest = require('vertx-js/http_server_request');
var Route = require('ext-rest-js/route');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRouter = io.vertx.ext.rest.Router;

/**

  @class
*/
var Router = function(j_val) {

  var j_router = j_val;
  var that = this;

  this.accept = function(request) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._vertxgen) {
      j_router.accept(request._jdel());
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
      return j_router.getRoutes();
    } else utils.invalidArgs();
  };

  this.clear = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Router(j_router.clear());
    } else utils.invalidArgs();
  };

  this._vertxgen = true;

  // Get a reference to the underlying Java delegate
  this._jdel = function() {
    return j_router;
  }

};

Router.router = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new Router(JRouter.router());
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Router;