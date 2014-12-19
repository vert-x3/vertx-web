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

/** @module ext-apex-core-js/cookie */
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
  
   @public

   @return {string}
   */
  this.getName = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getName();
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {string}
   */
  this.getValue = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getValue();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param value {string} 
   @return {Cookie}
   */
  this.setValue = function(value) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setValue(value));
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {string}
   */
  this.getDomain = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getDomain();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param domain {string} 
   @return {Cookie}
   */
  this.setDomain = function(domain) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setDomain(domain));
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {string}
   */
  this.getPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getPath();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param path {string} 
   @return {Cookie}
   */
  this.setPath = function(path) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return new Cookie(j_cookie.setPath(path));
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {number}
   */
  this.getMaxAge = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.getMaxAge();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param maxAge {number} 
   @return {Cookie}
   */
  this.setMaxAge = function(maxAge) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      return new Cookie(j_cookie.setMaxAge(maxAge));
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {boolean}
   */
  this.isSecure = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.isSecure();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param secure {boolean} 
   @return {Cookie}
   */
  this.setSecure = function(secure) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      return new Cookie(j_cookie.setSecure(secure));
    } else utils.invalidArgs();
  };

  /**
  
   @public

   @return {boolean}
   */
  this.isHttpOnly = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie.isHttpOnly();
    } else utils.invalidArgs();
  };

  /**
  
   @public
   @param httpOnly {boolean} 
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

 @memberof module:ext-apex-core-js/cookie
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