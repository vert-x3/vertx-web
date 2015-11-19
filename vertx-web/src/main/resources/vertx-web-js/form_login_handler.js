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

/** @module vertx-web-js/form_login_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-web-js/routing_context');
var AuthProvider = require('vertx-auth-common-js/auth_provider');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JFormLoginHandler = io.vertx.ext.web.handler.FormLoginHandler;

/**
 Handler that handles login from a form on a custom login page.
 <p>
 @class
*/
var FormLoginHandler = function(j_val) {

  var j_formLoginHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_formLoginHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the name of the form param used to submit the username

   @public
   @param usernameParam {string} the name of the param 
   @return {FormLoginHandler} a reference to this for a fluent API
   */
  this.setUsernameParam = function(usernameParam) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_formLoginHandler["setUsernameParam(java.lang.String)"](usernameParam);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the name of the form param used to submit the password

   @public
   @param passwordParam {string} the name of the param 
   @return {FormLoginHandler} a reference to this for a fluent API
   */
  this.setPasswordParam = function(passwordParam) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_formLoginHandler["setPasswordParam(java.lang.String)"](passwordParam);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the name of the session attrioute used to specify the return url

   @public
   @param returnURLParam {string} the name of the param 
   @return {FormLoginHandler} a reference to this for a fluent API
   */
  this.setReturnURLParam = function(returnURLParam) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_formLoginHandler["setReturnURLParam(java.lang.String)"](returnURLParam);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Set the url to redirect to if the user logs in directly at the url of the form login handler
   without being redirected here first

   @public
   @param directLoggedInOKURL {string} the URL to redirect to 
   @return {FormLoginHandler} a reference to this for a fluent API
   */
  this.setDirectLoggedInOKURL = function(directLoggedInOKURL) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_formLoginHandler["setDirectLoggedInOKURL(java.lang.String)"](directLoggedInOKURL);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_formLoginHandler;
};

/**
 Create a handler

 @memberof module:vertx-web-js/form_login_handler
 @param authProvider {AuthProvider} the auth service to use 
 @param usernameParam {string} the value of the form attribute which will contain the username 
 @param passwordParam {string} the value of the form attribute which will contain the password 
 @param returnURLParam {string} the value of the session attribute which will contain the return url 
 @param directLoggedInOKURL {string} a url to redirect to if the user logs in directly at the url of the form login handler without being redirected here first 
 @return {FormLoginHandler} the handler
 */
FormLoginHandler.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JFormLoginHandler["create(io.vertx.ext.auth.AuthProvider)"](__args[0]._jdel), FormLoginHandler);
  }else if (__args.length === 5 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'string' && typeof __args[4] === 'string') {
    return utils.convReturnVertxGen(JFormLoginHandler["create(io.vertx.ext.auth.AuthProvider,java.lang.String,java.lang.String,java.lang.String,java.lang.String)"](__args[0]._jdel, __args[1], __args[2], __args[3], __args[4]), FormLoginHandler);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = FormLoginHandler;