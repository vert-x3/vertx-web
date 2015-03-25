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

/** @module vertx-apex-js/cookie */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCookie = io.vertx.ext.apex.Cookie;

/**
 Represents an HTTP Cookie.
 <p>
 All cookies must have a name and a value and can optionally have other fields set such as path, domain, etc.
 <p>
 (Derived from io.netty.handler.codec.http.Cookie)

 @class
*/
var Cookie = function(j_val) {

  var j_cookie = j_val;
  var that = this;

  /**
   @return the name of this cookie

   @public

   @return {string}
   */
  this.getName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getName()"]();
    } else utils.invalidArgs();
  };

  /**
   @return the value of this cookie

   @public

   @return {string}
   */
  this.getValue = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getValue()"]();
    } else utils.invalidArgs();
  };

  /**
   Sets the value of this cookie

   @public
   @param value {string} The value to set 
   @return {Cookie} a reference to this, so the API can be used fluently
   */
  this.setValue = function(value) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setValue(java.lang.String)"](value);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Sets the domain of this cookie

   @public
   @param domain {string} The domain to use 
   @return {Cookie} a reference to this, so the API can be used fluently
   */
  this.setDomain = function(domain) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setDomain(java.lang.String)"](domain);
      return that;
    } else utils.invalidArgs();
  };

  /**
   @return  the domain for the cookie

   @public

   @return {string}
   */
  this.getDomain = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getDomain()"]();
    } else utils.invalidArgs();
  };

  /**
   Sets the path of this cookie.

   @public
   @param path {string} The path to use for this cookie 
   @return {Cookie} a reference to this, so the API can be used fluently
   */
  this.setPath = function(path) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setPath(java.lang.String)"](path);
      return that;
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string} the path for this cookie
   */
  this.getPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getPath()"]();
    } else utils.invalidArgs();
  };

  /**
   Sets the maximum age of this cookie in seconds.
   If an age of <code>0</code> is specified, this cookie will be
   automatically removed by browser because it will expire immediately.
   If  is specified, this cookie will be removed when the
   browser is closed.
   If you don't set this the cookie will be a session cookie and be removed when the browser is closed.

   @public
   @param maxAge {number} The maximum age of this cookie in seconds 
   @return {Cookie}
   */
  this.setMaxAge = function(maxAge) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new Cookie(j_cookie["setMaxAge(long)"](maxAge));
    } else utils.invalidArgs();
  };

  /**
   Sets the security getStatus of this cookie

   @public
   @param secure {boolean} True if this cookie is to be secure, otherwise false 
   @return {Cookie} a reference to this, so the API can be used fluently
   */
  this.setSecure = function(secure) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_cookie["setSecure(boolean)"](secure);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Determines if this cookie is HTTP only.
   If set to true, this cookie cannot be accessed by a client
   side script. However, this works only if the browser supports it.
   For for information, please look
   <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.

   @public
   @param httpOnly {boolean} True if the cookie is HTTP only, otherwise false. 
   @return {Cookie}
   */
  this.setHttpOnly = function(httpOnly) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new Cookie(j_cookie["setHttpOnly(boolean)"](httpOnly));
    } else utils.invalidArgs();
  };

  /**
   Set the version of the cookie

   @public
   @param version {number} 0 or 1 
   @return {Cookie} a reference to this, so the API can be used fluently
   */
  this.setVersion = function(version) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_cookie["setVersion(int)"](version);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Encode the cookie to a string. This is what is used in the Set-Cookie header

   @public

   @return {string} the encoded cookie
   */
  this.encode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["encode()"]();
    } else utils.invalidArgs();
  };

  /**
   Has the cookie been changed? Changed cookies will be saved out in the response and sent to the browser.

   @public

   @return {boolean} true if changed
   */
  this.isChanged = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["isChanged()"]();
    } else utils.invalidArgs();
  };

  /**
   Set the cookie as being changed. Changed will be true for a cookie just created, false by default if just
   read from the request

   @public
   @param changed {boolean} true if changed 
   */
  this.setChanged = function(changed) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_cookie["setChanged(boolean)"](changed);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_cookie;
};

/**
 Create a new cookie

 @memberof module:vertx-apex-js/cookie
 @param name {string} the name of the cookie 
 @param value {string} the cookie value 
 @return {Cookie} the cookie
 */
Cookie.cookie = function(name, value) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return new Cookie(JCookie["cookie(java.lang.String,java.lang.String)"](name, value));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Cookie;