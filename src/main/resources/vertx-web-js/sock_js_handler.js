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

/** @module vertx-web-js/sock_js_handler */
var utils = require('vertx-js/util/utils');
var SockJSSocket = require('vertx-web-js/sock_js_socket');
var Router = require('vertx-web-js/router');
var RoutingContext = require('vertx-web-js/routing_context');
var BridgeEvent = require('vertx-web-js/bridge_event');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSockJSHandler = io.vertx.ext.web.handler.sockjs.SockJSHandler;
var SockJSHandlerOptions = io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
var BridgeOptions = io.vertx.ext.web.handler.sockjs.BridgeOptions;
var BridgeOptions = io.vertx.ext.web.handler.sockjs.BridgeOptions;

/**

 A handler that allows you to handle SockJS connections from clients.
 <p>
 We currently support version 0.3.3 of the SockJS protocol, which can be found in
 <a href="https://github.com/sockjs/sockjs-protocol/tree/v0.3.3">this tag:</a>

 @class
*/
var SockJSHandler = function(j_val) {

  var j_sockJSHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_sockJSHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set a SockJS socket handler. This handler will be called with a SockJS socket whenever a SockJS connection
   is made from a client

   @public
   @param handler {function} the handler 
   @return {SockJSHandler} a reference to this, so the API can be used fluently
   */
  this.socketHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_sockJSHandler["socketHandler(io.vertx.core.Handler)"](function(jVal) {
      handler(new SockJSSocket(jVal));
    });
      return that;
    } else utils.invalidArgs();
  };

  /**

   @public
   @param bridgeOptions {Object} 
   @param bridgeEventHandler {function} 
   @return {SockJSHandler}
   */
  this.bridge = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object') {
      j_sockJSHandler["bridge(io.vertx.ext.web.handler.sockjs.BridgeOptions)"](__args[0] != null ? new BridgeOptions(new JsonObject(JSON.stringify(__args[0]))) : null);
      return that;
    }  else if (__args.length === 2 && typeof __args[0] === 'object' && typeof __args[1] === 'function') {
      j_sockJSHandler["bridge(io.vertx.ext.web.handler.sockjs.BridgeOptions,io.vertx.core.Handler)"](__args[0] != null ? new BridgeOptions(new JsonObject(JSON.stringify(__args[0]))) : null, function(jVal) {
      __args[1](new BridgeEvent(jVal));
    });
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sockJSHandler;
};

/**
 Create a SockJS handler

 @memberof module:vertx-web-js/sock_js_handler
 @param vertx {Vertx} the Vert.x instance 
 @param options {Object} options to configure the handler 
 @return {SockJSHandler} the handler
 */
SockJSHandler.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new SockJSHandler(JSockJSHandler["create(io.vertx.core.Vertx)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object') {
    return new SockJSHandler(JSockJSHandler["create(io.vertx.core.Vertx,io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions)"](__args[0]._jdel, __args[1] != null ? new SockJSHandlerOptions(new JsonObject(JSON.stringify(__args[1]))) : null));
  } else utils.invalidArgs();
};

/**
 Install SockJS test applications on a router - used when running the SockJS test suite

 @memberof module:vertx-web-js/sock_js_handler
 @param router {Router} the router to install on 
 @param vertx {Vertx} the Vert.x instance 
 */
SockJSHandler.installTestApplications = function(router, vertx) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object' && __args[1]._jdel) {
    JSockJSHandler["installTestApplications(io.vertx.ext.web.Router,io.vertx.core.Vertx)"](router._jdel, vertx._jdel);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = SockJSHandler;