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

/** @module ext-apex-core-js/session */
var utils = require('vertx-js/util/utils');
var SessionStore = require('ext-apex-core-js/session_store');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JSession = io.vertx.ext.apex.core.Session;

/**

 @class
*/
var Session = function(j_val) {

  var j_session = j_val;
  var that = this;

  /**

   @public

   @return {Object}
   */
  this.data = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_session.data());
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.id = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.id();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {number}
   */
  this.lastAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.lastAccessed();
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.setAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session.setAccessed();
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.destroy = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session.destroy();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {boolean}
   */
  this.isDestroyed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.isDestroyed();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {number}
   */
  this.timeout = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.timeout();
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {SessionStore}
   */
  this.sessionStore = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return new SessionStore(j_session.sessionStore());
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {boolean}
   */
  this.isLoggedIn = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.isLoggedIn();
    } else utils.invalidArgs();
  };

  /**

   @public

   */
  this.logout = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session.logout();
    } else utils.invalidArgs();
  };

  /**

   @public
   @param principal {string} 
   */
  this.setPrincipal = function(principal) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_session.setPrincipal(principal);
    } else utils.invalidArgs();
  };

  /**

   @public

   @return {string}
   */
  this.getPrincipal = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session.getPrincipal();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_session;
};

// We export the Constructor function
module.exports = Session;