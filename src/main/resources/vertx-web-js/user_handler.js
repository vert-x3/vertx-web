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

/** @module vertx-web-js/user_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');
var AuthProvider = require('vertx-auth-js/auth_provider');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JUserHandler = io.vertx.ext.web.handler.UserHandler;

/**

 @class
*/
var UserHandler = function(j_val) {

  var j_userHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_userHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_userHandler;
};

/**

 @memberof module:vertx-web-js/user_handler
 @param authProvider {AuthProvider} 
 @return {UserHandler}
 */
UserHandler.create = function(authProvider) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new UserHandler(JUserHandler["create(io.vertx.ext.auth.AuthProvider)"](authProvider._jdel));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = UserHandler;