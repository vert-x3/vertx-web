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

/** @module vertx-web-js/static_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JStaticHandler = io.vertx.ext.web.handler.StaticHandler;

/**
 A handler for serving static resources from the file system or classpath.

 @class
*/
var StaticHandler = function(j_val) {

  var j_staticHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_staticHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the web root

   @public
   @param webRoot {string} the web root 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setWebRoot = function(webRoot) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_staticHandler["setWebRoot(java.lang.String)"](webRoot);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether files are read-only and will never change

   @public
   @param readOnly {boolean} whether files are read-only 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setFilesReadOnly = function(readOnly) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setFilesReadOnly(boolean)"](readOnly);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set value for max age in caching headers

   @public
   @param maxAgeSeconds {number} maximum time for browser to cache, in seconds 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setMaxAgeSeconds = function(maxAgeSeconds) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_staticHandler["setMaxAgeSeconds(long)"](maxAgeSeconds);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether cache header handling is enabled

   @public
   @param enabled {boolean} true if enabled 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setCachingEnabled = function(enabled) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setCachingEnabled(boolean)"](enabled);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether directory listing is enabled

   @public
   @param directoryListing {boolean} true if enabled 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setDirectoryListing = function(directoryListing) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setDirectoryListing(boolean)"](directoryListing);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether hidden files should be served

   @public
   @param includeHidden {boolean} true if hidden files should be served 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setIncludeHidden = function(includeHidden) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setIncludeHidden(boolean)"](includeHidden);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the server cache entry timeout when caching is enabled

   @public
   @param timeout {number} the timeout, in ms 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setCacheEntryTimeout = function(timeout) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_staticHandler["setCacheEntryTimeout(long)"](timeout);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the index page

   @public
   @param indexPage {string} the index page 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setIndexPage = function(indexPage) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_staticHandler["setIndexPage(java.lang.String)"](indexPage);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the max cache size, when caching is enabled

   @public
   @param maxCacheSize {number} the max cache size 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setMaxCacheSize = function(maxCacheSize) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_staticHandler["setMaxCacheSize(int)"](maxCacheSize);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether async filesystem access should always be used

   @public
   @param alwaysAsyncFS {boolean} true for always async FS access 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setAlwaysAsyncFS = function(alwaysAsyncFS) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setAlwaysAsyncFS(boolean)"](alwaysAsyncFS);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether async/sync filesystem tuning should enabled

   @public
   @param enableFSTuning {boolean} true to enabled FS tuning 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setEnableFSTuning = function(enableFSTuning) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setEnableFSTuning(boolean)"](enableFSTuning);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the max serve time in ns, above which serves are considered slow

   @public
   @param maxAvgServeTimeNanoSeconds {number} max serve time, in ns 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setMaxAvgServeTimeNs = function(maxAvgServeTimeNanoSeconds) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_staticHandler["setMaxAvgServeTimeNs(long)"](maxAvgServeTimeNanoSeconds);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the directory template to be used when directory listing

   @public
   @param directoryTemplate {string} the directory template 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setDirectoryTemplate = function(directoryTemplate) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_staticHandler["setDirectoryTemplate(java.lang.String)"](directoryTemplate);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set whether range requests (resumable downloads; media streaming) should be enabled.

   @public
   @param enableRangeSupport {boolean} true to enable range support 
   @return {StaticHandler} a reference to this, so the API can be used fluently
   */
  this.setEnableRangeSupport = function(enableRangeSupport) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_staticHandler["setEnableRangeSupport(boolean)"](enableRangeSupport);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_staticHandler;
};

/**
 Create a handler, specifying web-root

 @memberof module:vertx-web-js/static_handler
 @param root {string} the web-root 
 @return {StaticHandler} the handler
 */
StaticHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(JStaticHandler["create()"](), StaticHandler);
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return utils.convReturnVertxGen(JStaticHandler["create(java.lang.String)"](__args[0]), StaticHandler);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = StaticHandler;