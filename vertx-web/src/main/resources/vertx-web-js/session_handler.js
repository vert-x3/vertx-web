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
    } else throw new TypeError('function invoked with invalid arguments');
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
    } else throw new TypeError('function invoked with invalid arguments');
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
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Sets whether the 'secure' flag should be set for the session cookie. When set this flag instructs browsers to only
   send the cookie over HTTPS. Note that this will probably stop your sessions working if used without HTTPS (e.g. in development).

   @public
   @param secure {boolean} true to set the secure flag on the cookie 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setCookieSecureFlag = function(secure) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_sessionHandler["setCookieSecureFlag(boolean)"](secure);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Sets whether the 'HttpOnly' flag should be set for the session cookie. When set this flag instructs browsers to
   prevent Javascript access to the the cookie. Used as a line of defence against the most common XSS attacks.

   @public
   @param httpOnly {boolean} true to set the HttpOnly flag on the cookie 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setCookieHttpOnlyFlag = function(httpOnly) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_sessionHandler["setCookieHttpOnlyFlag(boolean)"](httpOnly);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
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
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set expected session id minimum length.

   @public
   @param minLength {number} the session id minimal length 
   @return {SessionHandler} a reference to this, so the API can be used fluently
   */
  this.setMinLength = function(minLength) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_sessionHandler["setMinLength(int)"](minLength);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sessionHandler;
};

SessionHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.SessionHandler");
SessionHandler._jtype = {
  accept: function(obj) {
    return SessionHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(SessionHandler.prototype, {});
    SessionHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
SessionHandler._create = function(jdel) {
  var obj = Object.create(SessionHandler.prototype, {});
  SessionHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a session handler

 @memberof module:vertx-web-js/session_handler
 @param sessionStore {SessionStore} the session store 
 @return {SessionHandler} the handler
 */
SessionHandler.create = function(sessionStore) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(SessionHandler, JSessionHandler["create(io.vertx.ext.web.sstore.SessionStore)"](sessionStore._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = SessionHandler;