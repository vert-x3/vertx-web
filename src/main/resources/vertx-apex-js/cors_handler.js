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

/** @module vertx-apex-js/cors_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCorsHandler = io.vertx.ext.apex.handler.CorsHandler;

/**
 A handler which implements server side http://www.w3.org/TR/cors/[CORS] support for Apex.

 @class
*/
var CorsHandler = function(j_val) {

  var j_corsHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_corsHandler["handle(io.vertx.ext.apex.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Add an allowed method

   @public
   @param method {Object} the method to add 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.allowedMethod = function(method) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_corsHandler["allowedMethod(io.vertx.core.http.HttpMethod)"](io.vertx.core.http.HttpMethod.valueOf(__args[0]));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add an allowed header

   @public
   @param headerName {string} the allowed header name 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.allowedHeader = function(headerName) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_corsHandler["allowedHeader(java.lang.String)"](headerName);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add a set of allowed headers

   @public
   @param headerNames {Array.<string>} the allowed header names 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.allowedHeaders = function(headerNames) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0] instanceof Array) {
      j_corsHandler["allowedHeaders(java.util.Set)"](utils.convParamSetBasicOther(headerNames));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add an exposed header

   @public
   @param headerName {string} the exposed header name 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.exposedHeader = function(headerName) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_corsHandler["exposedHeader(java.lang.String)"](headerName);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add a set of exposed headers

   @public
   @param headerNames {Array.<string>} the exposed header names 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.exposedHeaders = function(headerNames) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0] instanceof Array) {
      j_corsHandler["exposedHeaders(java.util.Set)"](utils.convParamSetBasicOther(headerNames));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set whether credentials are allowed

   @public
   @param allow {boolean} true if allowed 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.allowCredentials = function(allow) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_corsHandler["allowCredentials(boolean)"](allow);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set how long the browser should cache the information

   @public
   @param maxAgeSeconds {number} max age in seconds 
   @return {CorsHandler} a reference to this, so the API can be used fluently
   */
  this.maxAgeSeconds = function(maxAgeSeconds) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_corsHandler["maxAgeSeconds(int)"](maxAgeSeconds);
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_corsHandler;
};

/**
 Create a CORS handler

 @memberof module:vertx-apex-js/cors_handler
 @param allowedOriginPattern {string} the allowed origin pattern 
 @return {CorsHandler} the handler
 */
CorsHandler.create = function(allowedOriginPattern) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'string') {
    return new CorsHandler(JCorsHandler["create(java.lang.String)"](allowedOriginPattern));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = CorsHandler;