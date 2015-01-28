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

/** @module vertx-apex-core-js/cookie */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCookie = io.vertx.ext.apex.core.Cookie;

/**
 Derived from io.netty.handler.codec.http.Cookie

 @class
*/
var Cookie = function(j_val) {

  var j_cookie = j_val;
  var that = this;

  /**
   Returns the name of this {@link Cookie}.

   @public

   @return {string} The name of this {@link Cookie}
   */
  this.getName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getName();
    } else utils.invalidArgs();
  };

  /**
   Returns the value of this {@link Cookie}.

   @public

   @return {string} The value of this {@link Cookie}
   */
  this.getValue = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getValue();
    } else utils.invalidArgs();
  };

  /**
   Sets the value of this {@link Cookie}.

   @public
   @param value {string} The value to set 
   @return {Cookie}
   */
  this.setValue = function(value) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setValue(value));
    } else utils.invalidArgs();
  };

  /**
   Returns the domain of this {@link Cookie}.

   @public

   @return {string} The domain of this {@link Cookie}
   */
  this.getDomain = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getDomain();
    } else utils.invalidArgs();
  };

  /**
   Sets the domain of this {@link Cookie}.

   @public
   @param domain {string} The domain to use 
   @return {Cookie}
   */
  this.setDomain = function(domain) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setDomain(domain));
    } else utils.invalidArgs();
  };

  /**
   Returns the path of this {@link Cookie}.

   @public

   @return {string} The {@link Cookie}'s path
   */
  this.getPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getPath();
    } else utils.invalidArgs();
  };

  /**
   Sets the path of this {@link Cookie}.

   @public
   @param path {string} The path to use for this {@link Cookie} 
   @return {Cookie}
   */
  this.setPath = function(path) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setPath(path));
    } else utils.invalidArgs();
  };

  /**
   Returns the maximum age of this {@link Cookie} in seconds or  if unspecified

   @public

   @return {number} The maximum age of this {@link Cookie}
   */
  this.getMaxAge = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getMaxAge();
    } else utils.invalidArgs();
  };

  /**
   Sets the maximum age of this {@link Cookie} in seconds.
   If an age of <code>0</code> is specified, this {@link Cookie} will be
   automatically removed by browser because it will expire immediately.
   If  is specified, this {@link Cookie} will be removed when the
   browser is closed.

   @public
   @param maxAge {number} The maximum age of this {@link Cookie} in seconds 
   @return {Cookie}
   */
  this.setMaxAge = function(maxAge) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new Cookie(j_cookie.setMaxAge(maxAge));
    } else utils.invalidArgs();
  };

  /**
   Checks to see if this {@link Cookie} is secure

   @public

   @return {boolean} True if this {@link Cookie} is secure, otherwise false
   */
  this.isSecure = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.isSecure();
    } else utils.invalidArgs();
  };

  /**
   Sets the security getStatus of this {@link Cookie}

   @public
   @param secure {boolean} True if this {@link Cookie} is to be secure, otherwise false 
   @return {Cookie}
   */
  this.setSecure = function(secure) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new Cookie(j_cookie.setSecure(secure));
    } else utils.invalidArgs();
  };

  /**
   Checks to see if this {@link Cookie} can only be accessed via HTTP.
   If this returns true, the {@link Cookie} cannot be accessed through
   client side script - But only if the browser supports it.
   For more information, please look <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>

   @public

   @return {boolean} True if this {@link Cookie} is HTTP-only or false if it isn't
   */
  this.isHttpOnly = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.isHttpOnly();
    } else utils.invalidArgs();
  };

  /**
   Determines if this {@link Cookie} is HTTP only.
   If set to true, this {@link Cookie} cannot be accessed by a client
   side script. However, this works only if the browser supports it.
   For for information, please look
   <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.

   @public
   @param httpOnly {boolean} True if the {@link Cookie} is HTTP only, otherwise false. 
   @return {Cookie}
   */
  this.setHttpOnly = function(httpOnly) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new Cookie(j_cookie.setHttpOnly(httpOnly));
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.encode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.encode();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_cookie;
};

/**

 @memberof module:vertx-apex-core-js/cookie
 @param name {string} 
 @param value {string} 
 @return {Cookie}
 */
Cookie.cookie = function(name, value) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return new Cookie(JCookie.cookie(name, value));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Cookie;