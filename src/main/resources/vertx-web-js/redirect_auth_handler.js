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

/** @module vertx-web-js/redirect_auth_handler */
var utils = require('vertx-js/util/utils');
var AuthHandler = require('vertx-web-js/auth_handler');
var RoutingContext = require('vertx-web-js/routing_context');
var AuthProvider = require('vertx-auth-js/auth_provider');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRedirectAuthHandler = io.vertx.ext.web.handler.RedirectAuthHandler;

/**
 An auth handler that's used to handle auth by redirecting user to a custom login page.

 @class
*/
var RedirectAuthHandler = function(j_val) {

  var j_redirectAuthHandler = j_val;
  var that = this;
  AuthHandler.call(this, j_val);

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_redirectAuthHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Add a required role for this auth handler

   @public
   @param role {string} the role 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addRole = function(role) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_redirectAuthHandler["addRole(java.lang.String)"](role);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add a required permission for this auth handler

   @public
   @param permission {string} the permission 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addPermission = function(permission) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_redirectAuthHandler["addPermission(java.lang.String)"](permission);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add a set of required roles for this auth handler

   @public
   @param roles {Array.<string>} the set of roles 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addRoles = function(roles) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0] instanceof Array) {
      j_redirectAuthHandler["addRoles(java.util.Set)"](utils.convParamSetBasicOther(roles));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add a set of required permissions for this auth handler

   @public
   @param permissions {Array.<string>} the set of permissions 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addPermissions = function(permissions) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0] instanceof Array) {
      j_redirectAuthHandler["addPermissions(java.util.Set)"](utils.convParamSetBasicOther(permissions));
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_redirectAuthHandler;
};

/**
 Create a handler

 @memberof module:vertx-web-js/redirect_auth_handler
 @param authProvider {AuthProvider} the auth service to use 
 @param loginRedirectURL {string} the url to redirect the user to 
 @param returnURLParam {string} the name of param used to store return url information in session 
 @return {AuthHandler} the handler
 */
RedirectAuthHandler.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return new AuthHandler(JRedirectAuthHandler["create(io.vertx.ext.auth.AuthProvider)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return new AuthHandler(JRedirectAuthHandler["create(io.vertx.ext.auth.AuthProvider,java.lang.String)"](__args[0]._jdel, __args[1]));
  }else if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
    return new AuthHandler(JRedirectAuthHandler["create(io.vertx.ext.auth.AuthProvider,java.lang.String,java.lang.String)"](__args[0]._jdel, __args[1], __args[2]));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = RedirectAuthHandler;