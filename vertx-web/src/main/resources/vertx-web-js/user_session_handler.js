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

/** @module vertx-web-js/user_session_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');
var AuthProvider = require('vertx-auth-common-js/auth_provider');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JUserSessionHandler = Java.type('io.vertx.ext.web.handler.UserSessionHandler');

/**

 This handler should be used if you want to store the User object in the Session so it's available between
 different requests, without you having re-authenticate each time.

 It requires that the session handler is already present on previous matching routes.

 It requires an Auth provider so, if the user is deserialized from a clustered session it knows which Auth provider
 to associate the session with.

 @class
*/
var UserSessionHandler = function(j_val) {

  var j_userSessionHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_userSessionHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_userSessionHandler;
};

UserSessionHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.UserSessionHandler");
UserSessionHandler._jtype = {
  accept: function(obj) {
    return UserSessionHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(UserSessionHandler.prototype, {});
    UserSessionHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
UserSessionHandler._create = function(jdel) {
  var obj = Object.create(UserSessionHandler.prototype, {});
  UserSessionHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a new handler

 @memberof module:vertx-web-js/user_session_handler
 @param authProvider {AuthProvider} The auth provider to use 
 @return {UserSessionHandler} the handler
 */
UserSessionHandler.create = function(authProvider) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(UserSessionHandler, JUserSessionHandler["create(io.vertx.ext.auth.AuthProvider)"](authProvider._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = UserSessionHandler;