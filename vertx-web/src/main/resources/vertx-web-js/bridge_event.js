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

/** @module vertx-web-js/bridge_event */
var utils = require('vertx-js/util/utils');
var SockJSSocket = require('vertx-web-js/sock_js_socket');
var Future = require('vertx-js/future');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JBridgeEvent = io.vertx.ext.web.handler.sockjs.BridgeEvent;

/**
 Represents an event that occurs on the event bus bridge.
 <p>
 Please consult the documentation for a full explanation.

 @class
*/
var BridgeEvent = function(j_val) {

  var j_bridgeEvent = j_val;
  var that = this;
  Future.call(this, j_val);

  /**

   @public

   @return {boolean}
   */
  this.isComplete = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_bridgeEvent["isComplete()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   @return {Future}
   */
  this.setHandler = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_bridgeEvent["setHandler(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        arg0(ar.result(), null);
      } else {
        arg0(null, ar.cause());
      }
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {boolean} 
   */
  this.complete = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_bridgeEvent["complete()"]();
    }  else if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_bridgeEvent["complete(java.lang.Boolean)"](__args[0]);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   */
  this.fail = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object') {
      j_bridgeEvent["fail(java.lang.Throwable)"](utils.convParamThrowable(__args[0]));
    }  else if (__args.length === 1 && typeof __args[0] === 'string') {
      j_bridgeEvent["fail(java.lang.String)"](__args[0]);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {boolean}
   */
  this.result = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_bridgeEvent["result()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {todo}
   */
  this.cause = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnThrowable(j_bridgeEvent["cause()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {boolean}
   */
  this.succeeded = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_bridgeEvent["succeeded()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {boolean}
   */
  this.failed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_bridgeEvent["failed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   @param next {Future} 
   @return {Future}
   */
  this.compose = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return utils.convReturnVertxGen(j_bridgeEvent["compose(java.util.function.Function)"](function(jVal) {
      var jRet = __args[0](jVal);
      return jRet._jdel;
    }), Future);
    }  else if (__args.length === 2 && typeof __args[0] === 'function' && typeof __args[1] === 'object' && __args[1]._jdel) {
      return utils.convReturnVertxGen(j_bridgeEvent["compose(io.vertx.core.Handler,io.vertx.core.Future)"](function(jVal) {
      __args[0](jVal);
    }, __args[1]._jdel), Future);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param value {Object} 
   @return {Future}
   */
  this.map = function() {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      return utils.convReturnVertxGen(j_bridgeEvent["map(java.util.function.Function)"](function(jVal) {
      var jRet = __args[0](jVal);
      return utils.convParamTypeUnknown(jRet);
    }), Future);
    }  else if (__args.length === 1 && typeof __args[0] !== 'function') {
      return utils.convReturnVertxGen(j_bridgeEvent["map(java.lang.Object)"](utils.convParamTypeUnknown(__args[0])), Future);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {function}
   */
  this.completer = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedcompleter == null) {
        that.cachedcompleter = utils.convReturnHandlerAsyncResult(j_bridgeEvent["completer()"](), function(result) { return result; });
      }
      return that.cachedcompleter;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Object} the type of the event
   */
  this.type = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedtype == null) {
        that.cachedtype = utils.convReturnEnum(j_bridgeEvent["type()"]());
      }
      return that.cachedtype;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Use {@link BridgeEvent#getRawMessage} instead, will be removed in 3.3

   @public

   @return {Object}
   */
  this.rawMessage = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedrawMessage == null) {
        that.cachedrawMessage = utils.convReturnJson(j_bridgeEvent["rawMessage()"]());
      }
      return that.cachedrawMessage;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   no message involved. If the returned message is modified, {@link BridgeEvent#setRawMessage} should be called with the
   new message.

   @public

   @return {Object} the raw JSON message for the event
   */
  this.getRawMessage = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_bridgeEvent["getRawMessage()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   no message involved.

   @public
   @param message {Object} the raw message 
   @return {BridgeEvent} this reference, so it can be used fluently
   */
  this.setRawMessage = function(message) {
    var __args = arguments;
    if (__args.length === 1 && (typeof __args[0] === 'object' && __args[0] != null)) {
      j_bridgeEvent["setRawMessage(io.vertx.core.json.JsonObject)"](utils.convParamJsonObject(message));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Get the SockJSSocket instance corresponding to the event

   @public

   @return {SockJSSocket} the SockJSSocket instance
   */
  this.socket = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedsocket == null) {
        that.cachedsocket = utils.convReturnVertxGen(j_bridgeEvent["socket()"](), SockJSSocket);
      }
      return that.cachedsocket;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_bridgeEvent;
};

// We export the Constructor function
module.exports = BridgeEvent;