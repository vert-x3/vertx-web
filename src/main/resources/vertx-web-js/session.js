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
 Represents a browser session.
 <p>
 Sessions persist between HTTP requests for a single browser session. They are deleted when the browser is closed, or
 they time-out. Session cookies are used to maintain sessions using a secure UUID.
 <p>
 Sessions can be used to maintain data for a browser session, e.g. a shopping basket.
 <p>
 @class
*/
var Session = function(j_val) {

  var j_session = j_val;
  var that = this;

  /**
   @return The unique ID of the session. This is generated using a random secure UUID.

   @public

   @return {string}
   */
  this.id = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["id()"]();
    } else utils.invalidArgs();
  };

  /**
   Put some data in a session

   @public
   @param key {string} the key for the data 
   @param obj {Object} the data 
   @return {Session} a reference to this, so the API can be used fluently
   */
  this.put = function(key, obj) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && true) {
      j_session["put(java.lang.String,java.lang.Object)"](key, utils.convParamTypeUnknown(obj));
      return that;
    } else utils.invalidArgs();
  };

  /**
   Get some data from the session

   @public
   @param key {string} the key of the data 
   @return {Object} the data
   */
  this.get = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_session["get(java.lang.String)"](key));
    } else utils.invalidArgs();
  };

  /**
   Remove some data from the session

   @public
   @param key {string} the key of the data 
   @return {Object} the data that was there or null if none there
   */
  this.remove = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return utils.convReturnTypeUnknown(j_session["remove(java.lang.String)"](key));
    } else utils.invalidArgs();
  };

  /**
   @return the time the session was last accessed

   @public

   @return {number}
   */
  this.lastAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["lastAccessed()"]();
    } else utils.invalidArgs();
  };

  /**
   Destroy the session

   @public

   */
  this.destroy = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session["destroy()"]();
    } else utils.invalidArgs();
  };

  /**
   @return has the session been destroyed?

   @public

   @return {boolean}
   */
  this.isDestroyed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["isDestroyed()"]();
    } else utils.invalidArgs();
  };

  /**
   @return the amount of time in ms, after which the session will expire, if not accessed.

   @public

   @return {number}
   */
  this.timeout = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["timeout()"]();
    } else utils.invalidArgs();
  };

  /**
   Mark the session as being accessed.

   @public

   */
  this.setAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_session["setAccessed()"]();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_session;
};

// We export the Constructor function
module.exports = Session;