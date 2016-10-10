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
 @class
*/
var Route = function(j_val) {

  var j_route = j_val;
  var that = this;

  /**

   @public
   @param arg0 {Object} 
   @return {Route}
   */
  this.method = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["method(io.vertx.core.http.HttpMethod)"](io.vertx.core.http.HttpMethod.valueOf(arg0));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.path = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["path(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.pathRegex = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["pathRegex(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.produces = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["produces(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Route}
   */
  this.consumes = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_route["consumes(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {number} 
   @return {Route}
   */
  this.order = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_route["order(int)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Route}
   */
  this.last = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["last()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   @return {Route}
   */
  this.handler = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["handler(io.vertx.core.Handler)"](function(jVal) {
      arg0(utils.convReturnVertxGen(jVal, RoutingContext));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   @param arg1 {boolean} 
   @return {Route}
   */
  this.blockingHandler = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["blockingHandler(io.vertx.core.Handler)"](function(jVal) {
      __args[0](utils.convReturnVertxGen(jVal, RoutingContext));
    });
      return that;
    }  else if (__args.length === 2 && typeof __args[0] === 'function' && typeof __args[1] ==='boolean') {
      j_route["blockingHandler(io.vertx.core.Handler,boolean)"](function(jVal) {
      __args[0](utils.convReturnVertxGen(jVal, RoutingContext));
    }, __args[1]);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   @return {Route}
   */
  this.failureHandler = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_route["failureHandler(io.vertx.core.Handler)"](function(jVal) {
      arg0(utils.convReturnVertxGen(jVal, RoutingContext));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Route}
   */
  this.remove = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["remove()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Route}
   */
  this.disable = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["disable()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Route}
   */
  this.enable = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_route["enable()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {boolean} 
   @return {Route}
   */
  this.useNormalisedPath = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_route["useNormalisedPath(boolean)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string}
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

// We export the Constructor function
module.exports = Route;