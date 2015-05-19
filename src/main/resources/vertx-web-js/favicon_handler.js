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

/** @module vertx-web-js/favicon_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JFaviconHandler = io.vertx.ext.web.handler.FaviconHandler;

/**
 A handler that serves favicons.
 <p>
 If no file system path is specified it will attempt to serve a resource called `favicon.ico` from the classpath.

 @class
*/
var FaviconHandler = function(j_val) {

  var j_faviconHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_faviconHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_faviconHandler;
};

/**
 Create a handler attempting to load favicon file from the specified path, and with the specified max cache time

 @memberof module:vertx-web-js/favicon_handler
 @param path {string} the path 
 @param maxAgeSeconds {number} max how long the file will be cached by browser, in seconds 
 @return {FaviconHandler} the handler
 */
FaviconHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new FaviconHandler(JFaviconHandler["create()"]());
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return new FaviconHandler(JFaviconHandler["create(java.lang.String)"](__args[0]));
  }else if (__args.length === 1 && typeof __args[0] ==='number') {
    return new FaviconHandler(JFaviconHandler["create(long)"](__args[0]));
  }else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] ==='number') {
    return new FaviconHandler(JFaviconHandler["create(java.lang.String,long)"](__args[0], __args[1]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = FaviconHandler;