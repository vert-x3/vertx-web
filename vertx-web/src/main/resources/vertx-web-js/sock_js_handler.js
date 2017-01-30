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
var Vertx = require('vertx-js/vertx');
var Router = require('vertx-web-js/router');
var RoutingContext = require('vertx-web-js/routing_context');
var BridgeEvent = require('vertx-web-js/bridge_event');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSockJSHandler = Java.type('io.vertx.ext.web.handler.sockjs.SockJSHandler');
var SockJSHandlerOptions = Java.type('io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions');
var BridgeOptions = Java.type('io.vertx.ext.web.handler.sockjs.BridgeOptions');

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
    } else throw new TypeError('function invoked with invalid arguments');
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
      handler(utils.convReturnVertxGen(SockJSSocket, jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Like {@link SockJSHandler#bridge} but specifying a handler
   that will receive bridge events.

   @public
   @param bridgeOptions {Object} options to configure the bridge with 
   @param bridgeEventHandler {function} handler to receive bridge events 
   @return {SockJSHandler} a reference to this, so the API can be used fluently
   */
  this.bridge = function() {
    var __args = arguments;
    if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
      j_sockJSHandler["bridge(io.vertx.ext.web.handler.sockjs.BridgeOptions)"](__args[0] != null ? new BridgeOptions(new JsonObject(Java.asJSONCompatible(__args[0]))) : null);
      return that;
    }  else if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_sockJSHandler["bridge(io.vertx.ext.web.handler.sockjs.BridgeOptions,io.vertx.core.Handler)"](__args[0] != null ? new BridgeOptions(new JsonObject(Java.asJSONCompatible(__args[0]))) : null, function(jVal) {
      __args[1](utils.convReturnVertxGen(BridgeEvent, jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sockJSHandler;
};

SockJSHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.sockjs.SockJSHandler");
SockJSHandler._jtype = {
  accept: function(obj) {
    return SockJSHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(SockJSHandler.prototype, {});
    SockJSHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
SockJSHandler._create = function(jdel) {
  var obj = Object.create(SockJSHandler.prototype, {});
  SockJSHandler.apply(obj, arguments);
  return obj;
}
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
    return utils.convReturnVertxGen(SockJSHandler, JSockJSHandler["create(io.vertx.core.Vertx)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(SockJSHandler, JSockJSHandler["create(io.vertx.core.Vertx,io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions)"](__args[0]._jdel, __args[1] != null ? new SockJSHandlerOptions(new JsonObject(Java.asJSONCompatible(__args[1]))) : null));
  } else throw new TypeError('function invoked with invalid arguments');
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
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = SockJSHandler;