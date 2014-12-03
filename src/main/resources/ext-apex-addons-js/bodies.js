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
var RoutingContext = require('ext-apex-core-js/routing_context');
var Buffer = require('vertx-js/buffer');
var FileUpload = require('ext-apex-addons-js/file_upload');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JBodies = io.vertx.ext.apex.addons.Bodies;

/**

  @class
*/
var Bodies = function(j_val) {

  var j_bodies = j_val;
  var that = this;

  this.handle = function(event) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_bodies.handle(event._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_bodies;
};

Bodies.bodies = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new Bodies(JBodies.bodies());
  }else if (__args.length === 1 && typeof __args[0] ==='number') {
    return new Bodies(JBodies.bodies(__args[0]));
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return new Bodies(JBodies.bodies(__args[0]));
  }else if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'string') {
    return new Bodies(JBodies.bodies(__args[0], __args[1]));
  } else utils.invalidArgs();
};

Bodies.getBodyAsString = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return JBodies.getBodyAsString();
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return JBodies.getBodyAsString(__args[0]);
  } else utils.invalidArgs();
};

Bodies.getBodyAsJson = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnJson(JBodies.getBodyAsJson());
  } else utils.invalidArgs();
};

Bodies.getBody = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new Buffer(JBodies.getBody());
  } else utils.invalidArgs();
};

Bodies.fileUploads = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnListSetVertxGen(JBodies.fileUploads(), FileUpload);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Bodies;