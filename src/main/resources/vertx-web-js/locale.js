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

/** @module vertx-web-js/locale */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JLocale = io.vertx.ext.web.Locale;

/**
 @class
*/
var Locale = function(j_val) {

  var j_locale = j_val;
  var that = this;

  /**
   Returns the language as reported by the HTTP client.

   @public

   @return {string} language
   */
  this.language = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_locale["language()"]();
    } else utils.invalidArgs();
  };

  /**
   Returns the country as reported by the HTTP client.

   @public

   @return {string} variant
   */
  this.country = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_locale["country()"]();
    } else utils.invalidArgs();
  };

  /**
   Returns the variant as reported by the HTTP client.

   @public

   @return {string} variant
   */
  this.variant = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_locale["variant()"]();
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_locale;
};

/**

 @memberof module:vertx-web-js/locale
 @param language {string} 
 @param country {string} 
 @param variant {string} 
 @return {Locale}
 */
Locale.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnVertxGen(JLocale["create()"](), Locale);
  }else if (__args.length === 1 && typeof __args[0] === 'string') {
    return utils.convReturnVertxGen(JLocale["create(java.lang.String)"](__args[0]), Locale);
  }else if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JLocale["create(java.lang.String,java.lang.String)"](__args[0], __args[1]), Locale);
  }else if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string') {
    return utils.convReturnVertxGen(JLocale["create(java.lang.String,java.lang.String,java.lang.String)"](__args[0], __args[1], __args[2]), Locale);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Locale;