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

/** @module vertx-web-js/virtual_host_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JVirtualHostHandler = Java.type('io.vertx.ext.web.handler.VirtualHostHandler');

/**
 Handler that will filter requests based on the request Host name.

 @class
*/
var VirtualHostHandler = function(j_val) {

  var j_virtualHostHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_virtualHostHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_virtualHostHandler;
};

VirtualHostHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.VirtualHostHandler");
VirtualHostHandler._jtype = {
  accept: function(obj) {
    return VirtualHostHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(VirtualHostHandler.prototype, {});
    VirtualHostHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
VirtualHostHandler._create = function(jdel) {
  var obj = Object.create(VirtualHostHandler.prototype, {});
  VirtualHostHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a handler

 @memberof module:vertx-web-js/virtual_host_handler
 @param hostname {string} 
 @param handler {function} 
 @return {VirtualHostHandler} the handler
 */
VirtualHostHandler.create = function(hostname, handler) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
    return utils.convReturnVertxGen(VirtualHostHandler, JVirtualHostHandler["create(java.lang.String,io.vertx.core.Handler)"](hostname, function(jVal) {
    handler(utils.convReturnVertxGen(RoutingContext, jVal));
  }));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = VirtualHostHandler;