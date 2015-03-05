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

/** @module vertx-apex-js/static_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JStaticHandler = io.vertx.ext.apex.handler.StaticHandler;

/**
 A handler for serving static resources from the file system or classpath.

 @class
*/
var StaticHandler = function(j_val) {

  var j_staticHandler = j_val;
  var that = this;

  /**

   @public
   @param context {RoutingContext} 
   */
  this.handle = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_staticHandler.handle(context._jdel);
    } else utils.invalidArgs();
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
      j_staticHandler.setWebRoot(webRoot);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setFilesReadOnly(readOnly);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setMaxAgeSeconds(maxAgeSeconds);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setCachingEnabled(enabled);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setDirectoryListing(directoryListing);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setIncludeHidden(includeHidden);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setCacheEntryTimeout(timeout);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setIndexPage(indexPage);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setMaxCacheSize(maxCacheSize);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setAlwaysAsyncFS(alwaysAsyncFS);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setEnableFSTuning(enableFSTuning);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds);
      return that;
    } else utils.invalidArgs();
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
      j_staticHandler.setDirectoryTemplate(directoryTemplate);
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_staticHandler;
};

/**
 Create a handler, specifying web-root

 @memberof module:vertx-apex-js/static_handler
 @param root {string} the web-root 
 @return {StaticHandler} the handler
 */
StaticHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new StaticHandler(JStaticHandler.create());
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return new StaticHandler(JStaticHandler.create(__args[0]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = StaticHandler;