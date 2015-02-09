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

/** @module vertx-apex-core-js/session_handler */
var utils = require('vertx-js/util/utils');
var SessionStore = require('vertx-apex-core-js/session_store');
var RoutingContext = require('vertx-apex-core-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSessionHandler = io.vertx.ext.apex.core.SessionHandler;

/**

 @class
*/
var SessionHandler = function(j_val) {

  var j_sessionHandler = j_val;
  var that = this;

  /**

   @public
   @param context {RoutingContext} 
   */
  this.handle = function(context) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_sessionHandler.handle(context._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_sessionHandler;
};

/**

 @memberof module:vertx-apex-core-js/session_handler
 @param sessionCookieName {string} 
 @param sessionTimeout {number} 
 @param nagHttps {boolean} 
 @param sessionStore {SessionStore} 
 @return {SessionHandler}
 */
SessionHandler.sessionHandler = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new SessionHandler(JSessionHandler.sessionHandler(__args[0]._jdel));
  }else if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='boolean' && typeof __args[3] === 'object' && __args[3]._jdel) {
    return new SessionHandler(JSessionHandler.sessionHandler(__args[0], __args[1], __args[2], __args[3]._jdel));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = SessionHandler;