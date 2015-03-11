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

/** @module vertx-apex-js/auth_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JAuthHandler = io.vertx.ext.apex.handler.AuthHandler;

/**
 Base interface for auth handlers.
 <p>
 An auth handler allows your application to provide authentication/authorisation support.
 <p>
 @class
*/
var AuthHandler = function(j_val) {

  var j_authHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_authHandler.handle(arg0._jdel);
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
      j_authHandler.addRole(role);
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
      j_authHandler.addPermission(permission);
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
      j_authHandler.addRoles(utils.convParamSetBasicOther(roles));
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
      j_authHandler.addPermissions(utils.convParamSetBasicOther(permissions));
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_authHandler;
};

// We export the Constructor function
module.exports = AuthHandler;