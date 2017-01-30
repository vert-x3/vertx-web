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

/** @module vertx-web-js/digest_auth_handler */
var utils = require('vertx-js/util/utils');
var HtdigestAuth = require('vertx-auth-htdigest-js/htdigest_auth');
var AuthHandler = require('vertx-web-js/auth_handler');
var RoutingContext = require('vertx-web-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JDigestAuthHandler = Java.type('io.vertx.ext.web.handler.DigestAuthHandler');

/**
 An auth handler that provides HTTP Basic Authentication support.

 @class
*/
var DigestAuthHandler = function(j_val) {

  var j_digestAuthHandler = j_val;
  var that = this;
  AuthHandler.call(this, j_val);

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_digestAuthHandler["handle(io.vertx.ext.web.RoutingContext)"](arg0._jdel);
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
      j_digestAuthHandler["addAuthority(java.lang.String)"](authority);
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
      j_digestAuthHandler["addAuthorities(java.util.Set)"](utils.convParamSetBasicOther(authorities));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_digestAuthHandler;
};

DigestAuthHandler._jclass = utils.getJavaClass("io.vertx.ext.web.handler.DigestAuthHandler");
DigestAuthHandler._jtype = {
  accept: function(obj) {
    return DigestAuthHandler._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(DigestAuthHandler.prototype, {});
    DigestAuthHandler.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
DigestAuthHandler._create = function(jdel) {
  var obj = Object.create(DigestAuthHandler.prototype, {});
  DigestAuthHandler.apply(obj, arguments);
  return obj;
}
/**
 Create a digest auth handler, specifying the expire timeout for nonces.

 @memberof module:vertx-web-js/digest_auth_handler
 @param authProvider {HtdigestAuth} the auth service to use 
 @param nonceExpireTimeout {number} the nonce expire timeout in milliseconds. 
 @return {DigestAuthHandler} the auth handler
 */
DigestAuthHandler.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(DigestAuthHandler, JDigestAuthHandler["create(io.vertx.ext.auth.htdigest.HtdigestAuth)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] ==='number') {
    return utils.convReturnVertxGen(DigestAuthHandler, JDigestAuthHandler["create(io.vertx.ext.auth.htdigest.HtdigestAuth,long)"](__args[0]._jdel, __args[1]));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = DigestAuthHandler;