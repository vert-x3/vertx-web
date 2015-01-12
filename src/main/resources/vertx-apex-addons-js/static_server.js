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

/** @module vertx-apex-addons-js/static_server */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-core-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JStaticServer = io.vertx.ext.apex.addons.StaticServer;

/**

 @class
*/
var StaticServer = function(j_val) {

  var j_staticServer = j_val;
  var that = this;

  /**

   @public
   @param context {RoutingContext} 
   */
  this.handle = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_staticServer.handle(context._jdel);
    } else utils.invalidArgs();
  };

  /**

   @public
   @param webRoot {string} 
   @return {StaticServer}
   */
  this.setWebRoot = function(webRoot) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new StaticServer(j_staticServer.setWebRoot(webRoot));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param readOnly {boolean} 
   @return {StaticServer}
   */
  this.setFilesReadOnly = function(readOnly) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setFilesReadOnly(readOnly));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param maxAgeSeconds {number} 
   @return {StaticServer}
   */
  this.setMaxAgeSeconds = function(maxAgeSeconds) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new StaticServer(j_staticServer.setMaxAgeSeconds(maxAgeSeconds));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param enabled {boolean} 
   @return {StaticServer}
   */
  this.setCachingEnabled = function(enabled) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setCachingEnabled(enabled));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param directoryListing {boolean} 
   @return {StaticServer}
   */
  this.setDirectoryListing = function(directoryListing) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setDirectoryListing(directoryListing));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param includeHidden {boolean} 
   @return {StaticServer}
   */
  this.setIncludeHidden = function(includeHidden) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setIncludeHidden(includeHidden));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param timeout {number} 
   @return {StaticServer}
   */
  this.setCacheEntryTimeout = function(timeout) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new StaticServer(j_staticServer.setCacheEntryTimeout(timeout));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param indexPage {string} 
   @return {StaticServer}
   */
  this.setIndexPage = function(indexPage) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new StaticServer(j_staticServer.setIndexPage(indexPage));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param maxCacheSize {number} 
   @return {StaticServer}
   */
  this.setMaxCacheSize = function(maxCacheSize) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new StaticServer(j_staticServer.setMaxCacheSize(maxCacheSize));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param alwaysAsyncFS {boolean} 
   @return {StaticServer}
   */
  this.setAlwaysAsyncFS = function(alwaysAsyncFS) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setAlwaysAsyncFS(alwaysAsyncFS));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param enableFSTuning {boolean} 
   @return {StaticServer}
   */
  this.setEnableFSTuning = function(enableFSTuning) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new StaticServer(j_staticServer.setEnableFSTuning(enableFSTuning));
    } else utils.invalidArgs();
  };

  /**

   @public
   @param maxAvgServeTimeNanoSeconds {number} 
   @return {StaticServer}
   */
  this.setMaxAvgServeTimeNs = function(maxAvgServeTimeNanoSeconds) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new StaticServer(j_staticServer.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds));
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_staticServer;
};

/**

 @memberof module:vertx-apex-addons-js/static_server

 @return {StaticServer}
 */
StaticServer.staticServer = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new StaticServer(JStaticServer.staticServer());
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return new StaticServer(JStaticServer.staticServer(__args[0]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = StaticServer;