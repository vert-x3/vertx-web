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
var Buffer = require('vertx-js/buffer');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRouteContext = io.vertx.ext.rest.RouteContext;

/**

  @class
*/
var RouteContext = function(j_val) {

  var j_routeContext = j_val;
  var that = this;

  this.request = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new HttpServerRequest(j_routeContext.request());
    } else utils.invalidArgs();
  };

  this.bodyBuffer = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Buffer(j_routeContext.bodyBuffer());
    } else utils.invalidArgs();
  };

  this.bodyJson = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convJsonToJS(j_routeContext.bodyJson());
    } else utils.invalidArgs();
  };

  this.contextData = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convMap(j_routeContext.contextData());
    } else utils.invalidArgs();
  };

  this.next = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routeContext.next();
    } else utils.invalidArgs();
  };

  this._vertxgen = true;

  // Get a reference to the underlying Java delegate
  this._jdel = function() {
    return j_routeContext;
  }

};

// We export the Constructor function
module.exports = RouteContext;