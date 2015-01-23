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

/** @module vertx-apex-core-js/routing_context */
var utils = require('vertx-js/util/utils');
var HttpServerRequest = require('vertx-js/http_server_request');
var Buffer = require('vertx-js/buffer');
var FileUpload = require('vertx-apex-core-js/file_upload');
var Route = require('vertx-apex-core-js/route');
var HttpServerResponse = require('vertx-js/http_server_response');
var Session = require('vertx-apex-core-js/session');
var Cookie = require('vertx-apex-core-js/cookie');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRoutingContext = io.vertx.ext.apex.core.RoutingContext;

/**

 @class
*/
var RoutingContext = function(j_val) {

  var j_routingContext = j_val;
  var that = this;

  /**

   @public

   @return {HttpServerRequest}
   */
  this.request = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedrequest == null) {
        that.cachedrequest = new HttpServerRequest(j_routingContext.request());
      }
      return that.cachedrequest;
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {HttpServerResponse}
   */
  this.response = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedresponse == null) {
        that.cachedresponse = new HttpServerResponse(j_routingContext.response());
      }
      return that.cachedresponse;
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.next = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routingContext.next();
    } else utils.invalidArgs();
  };

  /**

   @public
   @param statusCode {number} 
   */
  this.fail = function(statusCode) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_routingContext.fail(statusCode);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param key {string} 
   @param obj {Object} 
   */
  this.put = function(key, obj) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && true) {
      j_routingContext.put(key, utils.convParamTypeUnknown(obj));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param key {string} 
   @return {Object}
   */
  this.get = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_routingContext.get(key));
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Vertx}
   */
  this.vertx = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Vertx(j_routingContext.vertx());
    } else utils.invalidArgs();
  };

  /**

   @public
   @param handler {function} 
   @return {number}
   */
  this.addHeadersEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext.addHeadersEndHandler(handler);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param handlerID {number} 
   @return {boolean}
   */
  this.removeHeadersEndHandler = function(handlerID) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return j_routingContext.removeHeadersEndHandler(handlerID);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param handler {function} 
   @return {number}
   */
  this.addBodyEndHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return j_routingContext.addBodyEndHandler(handler);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param handlerID {number} 
   @return {boolean}
   */
  this.removeBodyEndHandler = function(handlerID) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return j_routingContext.removeBodyEndHandler(handlerID);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param handled {boolean} 
   */
  this.setHandled = function(handled) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_routingContext.setHandled(handled);
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.unhandled = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_routingContext.unhandled();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {boolean}
   */
  this.failed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.failed();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.mountPoint = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.mountPoint();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Route}
   */
  this.currentRoute = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Route(j_routingContext.currentRoute());
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.normalisedPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.normalisedPath();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.pathFromMountPoint = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.pathFromMountPoint();
    } else utils.invalidArgs();
  };

  /**

   @public
   @param name {string} 
   @return {Cookie}
   */
  this.getCookie = function(name) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_routingContext.getCookie(name));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param cookie {Cookie} 
   */
  this.addCookie = function(cookie) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext.addCookie(cookie._jdel);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param name {string} 
   @return {Cookie}
   */
  this.removeCookie = function(name) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_routingContext.removeCookie(name));
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {number}
   */
  this.cookieCount = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.cookieCount();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Array.<Cookie>}
   */
  this.cookies = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_routingContext.cookies(), Cookie);
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.getBodyAsString = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_routingContext.getBodyAsString();
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_routingContext.getBodyAsString(__args[0]);
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Object}
   */
  this.getBodyAsJson = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_routingContext.getBodyAsJson());
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Buffer}
   */
  this.getBody = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Buffer(j_routingContext.getBody());
    } else utils.invalidArgs();
  };

  /**

   @public
   @param body {Buffer} 
   */
  this.setBody = function(body) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext.setBody(body._jdel);
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Array.<FileUpload>}
   */
  this.fileUploads = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_routingContext.fileUploads(), FileUpload);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param session {Session} 
   */
  this.setSession = function(session) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_routingContext.setSession(session._jdel);
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {Session}
   */
  this.session = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new Session(j_routingContext.session());
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_routingContext;
};

// We export the Constructor function
module.exports = RoutingContext;