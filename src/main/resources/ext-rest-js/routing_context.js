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
var HttpServerResponse = require('vertx-js/http_server_response');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRoutingContext = io.vertx.ext.rest.RoutingContext;

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

  this._vertxgen = true;

  // Get a reference to the underlying Java delegate
  this._jdel = function() {
    return j_routingContext;
  }

};

// We export the Constructor function
module.exports = RoutingContext;