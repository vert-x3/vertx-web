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

/** @module vertx-apex-js/session_store */
var utils = require('vertx-js/util/utils');
var Session = require('vertx-apex-js/session');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSessionStore = io.vertx.ext.apex.sstore.SessionStore;

/**
 A session store is used to store sessions for an Apex web app

 @class
*/
var SessionStore = function(j_val) {

  var j_sessionStore = j_val;
  var that = this;

  /**
   Get the session with the specified ID

   @public
   @param id {string} the unique ID of the session 
   @param resultHandler {function} will be called with a result holding the session, or a failure 
   */
  this.get = function(id, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_sessionStore["get(java.lang.String,io.vertx.core.Handler)"](id, function(ar) {
      if (ar.succeeded()) {
        resultHandler(new Session(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Delete the session with the specified ID

   @public
   @param id {string} the unique ID of the session 
   @param resultHandler {function} will be called with a result true/false, or a failure 
   */
  this.delete = function(id, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_sessionStore["delete(java.lang.String,io.vertx.core.Handler)"](id, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Add a session with the specified ID

   @public
   @param session {Session} the session 
   @param timeout {number} max time session will last without being accessed before getting expired 
   @param resultHandler {function} will be called with a result true/false, or a failure 
   */
  this.put = function(session, timeout, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] ==='number' && typeof __args[2] === 'function') {
      j_sessionStore["put(io.vertx.ext.apex.Session,long,io.vertx.core.Handler)"](session._jdel, timeout, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Remove all sessions from the store

   @public
   @param resultHandler {function} will be called with a result true/false, or a failure 
   */
  this.clear = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_sessionStore["clear(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Get the number of sessions in the store

   @public
   @param resultHandler {function} will be called with the number, or a failure 
   */
  this.size = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_sessionStore["size(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Close the store

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_sessionStore["close()"]();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sessionStore;
};

// We export the Constructor function
module.exports = SessionStore;