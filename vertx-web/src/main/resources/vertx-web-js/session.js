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

/** @module vertx-web-js/session */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSession = io.vertx.ext.web.Session;

/**
 @class
*/
var Session = function(j_val) {

  var j_session = j_val;
  var that = this;

  /**

   @public

   @return {string}
   */
  this.id = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["id()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @param arg1 {Object} 
   @return {Session}
   */
  this.put = function(arg0, arg1) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] !== 'function') {
      j_session["put(java.lang.String,java.lang.Object)"](arg0, utils.convParamTypeUnknown(arg1));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Object}
   */
  this.get = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_session["get(java.lang.String)"](arg0));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {string} 
   @return {Object}
   */
  this.remove = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_session["remove(java.lang.String)"](arg0));
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number}
   */
  this.lastAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["lastAccessed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.destroy = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session["destroy()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {boolean}
   */
  this.isDestroyed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["isDestroyed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number}
   */
  this.timeout = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["timeout()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   */
  this.setAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session["setAccessed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_session;
};

// We export the Constructor function
module.exports = Session;