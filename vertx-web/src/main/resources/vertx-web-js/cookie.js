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

/** @module vertx-web-js/cookie */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCookie = io.vertx.ext.web.Cookie;

/**
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
      return j_cookie["getName()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string}
   */
  this.getValue = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getValue()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Cookie}
   */
  this.setValue = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setValue(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Cookie}
   */
  this.setDomain = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setDomain(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string}
   */
  this.getDomain = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getDomain()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Cookie}
   */
  this.setPath = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_cookie["setPath(java.lang.String)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string}
   */
  this.getPath = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["getPath()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {number} 
   @return {Cookie}
   */
  this.setMaxAge = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='number') {
      j_cookie["setMaxAge(long)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {boolean} 
   @return {Cookie}
   */
  this.setSecure = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_cookie["setSecure(boolean)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {boolean} 
   @return {Cookie}
   */
  this.setHttpOnly = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_cookie["setHttpOnly(boolean)"](arg0);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {string}
   */
  this.encode = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["encode()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {boolean}
   */
  this.isChanged = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_cookie["isChanged()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {boolean} 
   */
  this.setChanged = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] ==='boolean') {
      j_cookie["setChanged(boolean)"](arg0);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_cookie;
};

/**

 @memberof module:vertx-web-js/cookie
 @param name {string} 
 @param value {string} 
 @return {Cookie}
 */
Cookie.cookie = function(name, value) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JCookie["cookie(java.lang.String,java.lang.String)"](name, value), Cookie);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = Cookie;