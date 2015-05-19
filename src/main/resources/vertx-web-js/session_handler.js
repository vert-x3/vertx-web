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

/** @module vertx-web-js/session_handler */
var utils = require('vertx-js/util/utils');
var SessionStore = require('vertx-web-js/session_store');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSessionHandler = io.vertx.ext.web.handler.SessionHandler;

/**

 @class
*/
var SessionHandler = function(j_val) {

  var j_sessionHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_sessionHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Set the session timeout

   @public
   @param timeout {number} the timeout, in ms. 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setSessionTimeout = function(timeout) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_sessionHandler["setSessionTimeout(long)"](timeout);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set whether a nagging log warning should be written if the session handler is accessed over HTTP, not
   HTTPS

   @public
   @param nag {boolean} true to nag 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setNagHttps = function(nag) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_sessionHandler["setNagHttps(boolean)"](nag);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Set the session cookie name

   @public
   @param sessionCookieName {string} the session cookie name 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setSessionCookieName = function(sessionCookieName) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_sessionHandler["setSessionCookieName(java.lang.String)"](sessionCookieName);
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sessionHandler;
};

/**
 Create a session handler

 @memberof module:vertx-web-js/session_handler
 @param sessionStore {SessionStore} the session store 
 @return {SessionHandler} the handler
 */
SessionHandler.create = function(sessionStore) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new SessionHandler(JSessionHandler["create(io.vertx.ext.web.sstore.SessionStore)"](sessionStore._jdel));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = SessionHandler;