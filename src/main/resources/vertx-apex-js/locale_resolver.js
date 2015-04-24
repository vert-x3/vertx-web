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

/** @module vertx-apex-js/locale_resolver */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-js/routing_context');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JLocaleResolver = io.vertx.ext.apex.handler.LocaleResolver;

/**
 A LocaleResoler resolves the locale for the current request.
 @class
*/
var LocaleResolver = function(j_val) {

  var j_localeResolver = j_val;
  var that = this;

  /**
   Returns the locale to use for the current request 
   Note that the value returned can contain multiple locales or languages and accept any values supported by the ACCEPT_LANGUAGE header (ie: * da, en-gb;q=0.8, en;q=0.7)
   <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4">See W3c Specification</a>

   @public
   @param context {RoutingContext} - the RoutingContext 
   @param resultHandler {function} - the result handler 
   */
  this.resolve = function(context, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'function') {
      j_localeResolver["resolve(io.vertx.ext.apex.RoutingContext,io.vertx.core.Handler)"](context._jdel, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_localeResolver;
};

/**

 @memberof module:vertx-apex-js/locale_resolver

 @return {LocaleResolver}
 */
LocaleResolver.acceptLanguageHeaderResolver = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new LocaleResolver(JLocaleResolver["acceptLanguageHeaderResolver()"]());
  } else utils.invalidArgs();
};

/**

 @memberof module:vertx-apex-js/locale_resolver
 @param locale {string} 
 @return {LocaleResolver}
 */
LocaleResolver.fallbackResolver = function(locale) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'string') {
    return new LocaleResolver(JLocaleResolver["fallbackResolver(java.lang.String)"](locale));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = LocaleResolver;