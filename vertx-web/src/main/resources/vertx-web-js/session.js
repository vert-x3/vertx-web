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

   @public

   @return {string} The unique ID of the session. This is generated using a random secure UUID.
   */
  this.id = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["id()"]();
    } else throw new TypeError('function invoked with invalid arguments');
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
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] !== 'function') {
      j_session["put(java.lang.String,java.lang.Object)"](key, utils.convParamTypeUnknown(obj));
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
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
    } else throw new TypeError('function invoked with invalid arguments');
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
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number} the time the session was last accessed
   */
  this.lastAccessed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["lastAccessed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Destroy the session

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

   @return {boolean} has the session been destroyed?
   */
  this.isDestroyed = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["isDestroyed()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {number} the amount of time in ms, after which the session will expire, if not accessed.
   */
  this.timeout = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_session["timeout()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Mark the session as being accessed.

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

Session._jclass = utils.getJavaClass("io.vertx.ext.web.Session");
Session._jtype = {
  accept: function(obj) {
    return Session._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(Session.prototype, {});
    Session.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
Session._create = function(jdel) {
  var obj = Object.create(Session.prototype, {});
  Session.apply(obj, arguments);
  return obj;
}
module.exports = Session;