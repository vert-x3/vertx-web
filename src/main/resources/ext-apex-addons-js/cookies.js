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

var utils = require('vertx-js/util/utils');
var RoutingContext = require('ext-apex-core-js/routing_context');
var Cookie = require('ext-apex-addons-js/cookie');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JCookies = io.vertx.ext.apex.addons.Cookies;

/**

  @class
*/
var Cookies = function(j_val) {

  var j_cookies = j_val;
  var that = this;

  this.handle = function(event) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_cookies.handle(event._jdel);
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_cookies;
};

Cookies.cookies = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new Cookies(JCookies.cookies());
  } else utils.invalidArgs();
};

Cookies.getCookie = function(name) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'string') {
    return new Cookie(JCookies.getCookie(name));
  } else utils.invalidArgs();
};

Cookies.addCookie = function(cookie) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    JCookies.addCookie(cookie._jdel);
  } else utils.invalidArgs();
};

Cookies.removeCookie = function(name) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'string') {
    return new Cookie(JCookies.removeCookie(name));
  } else utils.invalidArgs();
};

Cookies.cookiesNames = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return utils.convReturnSet(JCookies.cookiesNames());
  } else utils.invalidArgs();
};

Cookies.cookieCount = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return JCookies.cookieCount();
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = Cookies;