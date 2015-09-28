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
   @return  the type of the event

   @public

   @return {Object}
   */
  this.type = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedtype == null) {
        that.cachedtype = (j_bridgeEvent["type()"]()).toString();
      }
      return that.cachedtype;
    } else utils.invalidArgs();
  };

  /**
   Get the raw JSON message for the event. This will be null for SOCKET_CREATED or SOCKET_CLOSED events as there is
   no message involved.

   @public

   @return {Object} the raw JSON message for the event
   */
  this.rawMessage = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedrawMessage == null) {
        that.cachedrawMessage = utils.convReturnJson(j_bridgeEvent["rawMessage()"]());
      }
      return that.cachedrawMessage;
    } else utils.invalidArgs();
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
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_bridgeEvent;
};

// We export the Constructor function
module.exports = BridgeEvent;