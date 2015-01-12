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

/** @module vertx-apex-core-js/failure_routing_context */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-core-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JFailureRoutingContext = io.vertx.ext.apex.core.FailureRoutingContext;

/**

 @class
*/
var FailureRoutingContext = function(j_val) {

  var j_failureRoutingContext = j_val;
  var that = this;
  RoutingContext.call(this, j_val);

  /**

   @public

   @return {todo}
   */
  this.failure = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedfailure == null) {
        that.cachedfailure = utils.convReturnTypeUnknown(j_failureRoutingContext.failure());
      }
      return that.cachedfailure;
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {number}
   */
  this.statusCode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedstatusCode == null) {
        that.cachedstatusCode = j_failureRoutingContext.statusCode();
      }
      return that.cachedstatusCode;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_failureRoutingContext;
};

// We export the Constructor function
module.exports = FailureRoutingContext;