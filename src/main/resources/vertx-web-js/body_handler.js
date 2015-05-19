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

/** @module vertx-web-js/body_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JBodyHandler = io.vertx.ext.web.handler.BodyHandler;

/**

 @class
*/
var BodyHandler = function(j_val) {

  var j_bodyHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_bodyHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set the maximum body size -1 means unlimited

   @public
   @param bodyLimit {number} the max size 
   @return {BodyHandler} reference to this for fluency
   */
  this.setBodyLimit = function(bodyLimit) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_bodyHandler["setBodyLimit(long)"](bodyLimit);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set the uploads directory to use

   @public
   @param uploadsDirectory {string} the uploads directory 
   @return {BodyHandler} reference to this for fluency
   */
  this.setUploadsDirectory = function(uploadsDirectory) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_bodyHandler["setUploadsDirectory(java.lang.String)"](uploadsDirectory);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set whether form attributes will be added to the request parameters

   @public
   @param mergeFormAttributes {boolean} true if they should be merged 
   @return {BodyHandler} reference to this for fluency
   */
  this.setMergeFormAttributes = function(mergeFormAttributes) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_bodyHandler["setMergeFormAttributes(boolean)"](mergeFormAttributes);
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_bodyHandler;
};

/**
 Create a body handler with defaults

 @memberof module:vertx-web-js/body_handler

 @return {BodyHandler} the body handler
 */
BodyHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new BodyHandler(JBodyHandler["create()"]());
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = BodyHandler;