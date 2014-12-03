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
var Route = require('ext-apex-core-js/route');
var HttpServerResponse = require('vertx-js/http_server_response');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRoutingContext = io.vertx.ext.apex.core.RoutingContext;

/**

  @class
*/
var RoutingContext = function(j_val) {

  var j_routingContext = j_val;
  var that = this;

  this.request = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedrequest == null) {
        that.cachedrequest = new HttpServerRequest(j_routingContext.request());
      }
      return that.cachedrequest;
    } else utils.invalidArgs();
  };

  this.response = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedresponse == null) {
        that.cachedresponse = new HttpServerResponse(j_routingContext.response());
      }
      return that.cachedresponse;
    } else utils.invalidArgs();
  };

  this.next = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routingContext.next();
    } else utils.invalidArgs();
  };

  this.fail = function(statusCode) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_routingContext.fail(statusCode);
    } else utils.invalidArgs();
  };

  this.put = function(key, obj) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && true) {
      j_routingContext.put(key, utils.convParamTypeUnknown(obj));
    } else utils.invalidArgs();
  };

  this.get = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_routingContext.get(key));
    } else utils.invalidArgs();
  };

  this.vertx = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Vertx(j_routingContext.vertx());
    } else utils.invalidArgs();
  };

  this.addHeadersEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_routingContext.addHeadersEndHandler(handler);
    } else utils.invalidArgs();
  };

  this.removeHeadersEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext.removeHeadersEndHandler(handler);
    } else utils.invalidArgs();
  };

  this.addBodyEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_routingContext.addBodyEndHandler(handler);
    } else utils.invalidArgs();
  };

  this.removeBodyEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext.removeBodyEndHandler(handler);
    } else utils.invalidArgs();
  };

  this.setHandled = function(handled) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_routingContext.setHandled(handled);
    } else utils.invalidArgs();
  };

  this.unhandled = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routingContext.unhandled();
    } else utils.invalidArgs();
  };

  this.failed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.failed();
    } else utils.invalidArgs();
  };

  this.mountPoint = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.mountPoint();
    } else utils.invalidArgs();
  };

  this.currentRoute = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Route(j_routingContext.currentRoute());
    } else utils.invalidArgs();
  };

  this.normalisedPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.normalisedPath();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_routingContext;
};

RoutingContext.getContext = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new RoutingContext(JRoutingContext.getContext());
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = RoutingContext;