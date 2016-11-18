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

/** @module vertx-web-js/o_auth2_auth_handler */
var utils = require('vertx-js/util/utils');
var Route = require('vertx-web-js/route');
var AuthHandler = require('vertx-web-js/auth_handler');
var RoutingContext = require('vertx-web-js/routing_context');
var OAuth2Auth = require('vertx-auth-oauth2-js/o_auth2_auth');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JOAuth2AuthHandler = io.vertx.ext.web.handler.OAuth2AuthHandler;

/**
 An auth handler that provides OAuth2 Authentication support. This handler is suitable for AuthCode flows.

 @class
*/
var OAuth2AuthHandler = function(j_val) {

  var j_oAuth2AuthHandler = j_val;
  var that = this;
  AuthHandler.call(this, j_val);

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_oAuth2AuthHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a required authority for this auth handler

   @public
   @param authority {string} the authority 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addAuthority = function(authority) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_oAuth2AuthHandler["addAuthority(java.lang.String)"](authority);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Add a set of required authorities for this auth handler

   @public
   @param authorities {Array.<string>} the set of authorities 
   @return {AuthHandler} a reference to this, so the API can be used fluently
   */
  this.addAuthorities = function(authorities) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0] instanceof Array) {
      j_oAuth2AuthHandler["addAuthorities(java.util.Set)"](utils.convParamSetBasicOther(authorities));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Build the authorization URL.

   @public
   @param redirectURL {string} where is the callback mounted. 
   @param state {string} state opaque token to avoid forged requests 
   @return {string} the redirect URL
   */
  this.authURI = function(redirectURL, state) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
      return j_oAuth2AuthHandler["authURI(java.lang.String,java.lang.String)"](redirectURL, state);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   add the callback handler to a given route.

   @public
   @param route {Route} a given route e.g.: `/callback` 
   @return {OAuth2AuthHandler} self
   */
  this.setupCallback = function(route) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_oAuth2AuthHandler["setupCallback(io.vertx.ext.web.Route)"](route._jdel);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_oAuth2AuthHandler;
};

OAuth2AuthHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.OAuth2AuthHandler");
OAuth2AuthHandler._jtype = {
  accept: function(obj) {
    return OAuth2AuthHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(OAuth2AuthHandler.prototype, {});
    OAuth2AuthHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
OAuth2AuthHandler._create = function(jdel) {
  var obj = Object.create(OAuth2AuthHandler.prototype, {});
  OAuth2AuthHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a OAuth2 auth handler

 @memberof module:vertx-web-js/o_auth2_auth_handler
 @param authProvider {OAuth2Auth} the auth provider to use 
 @param uri {string} 
 @return {OAuth2AuthHandler} the auth handler
 */
OAuth2AuthHandler.create = function(authProvider, uri) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(OAuth2AuthHandler, JOAuth2AuthHandler["create(io.vertx.ext.auth.oauth2.OAuth2Auth,java.lang.String)"](authProvider._jdel, uri));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = OAuth2AuthHandler;