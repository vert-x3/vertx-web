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

/** @module vertx-apex-js/locale_handler */
var utils = require('vertx-js/util/utils');
var RoutingContext = require('vertx-apex-js/routing_context');
var LocaleResolver = require('vertx-apex-js/locale_resolver');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JLocaleHandler = io.vertx.ext.apex.handler.LocaleHandler;

/**
 A handler that sets the locale of the RoutingContext based on the specified locale resolvers 
 and an optional list of locales supported by your application
 Note that the value of the Locale set on the context is a <a href="http://en.wikipedia.org/wiki/IETF_language_tag">IETF Language Tag</a> 
 (uses hyphen instead of underscore, ie: fr-fr or en-gb instead of fr_FR, en_GB). 
 To parse the Locale to a Java Locale use Locale.forLanguageTag(rc.getLocale());
 
 A typical application will setup several locale resolvers sorted by priority:
  - A resolver based on the current user of the application 
      (your user's locale is application specific and usually retrieved from the app db) 
  - A resolver based on an ip address
  - A resolver based on the Accept-Language header of the current request
  - A resolver based on a specified default fallback locale in case nothing matches
  
  The LocaleHandler will simply loop through each resolver and find the best matching locale supported by your app, if any

 @class
*/
var LocaleHandler = function(j_val) {

  var j_localeHandler = j_val;
  var that = this;

  /**

   @public
   @param arg0 {RoutingContext} 
   */
  this.handle = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_localeHandler["handle(io.vertx.ext.apex.RoutingContext)"](arg0._jdel);
    } else utils.invalidArgs();
  };

  /**
   Add the specified resolver to the locale Handler.
   Note that resolvers are evaluated based on insertion order

   @public
   @param resolver {LocaleResolver} 
   @return {LocaleHandler} 
   */
  this.addResolver = function(resolver) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      j_localeHandler["addResolver(io.vertx.ext.apex.handler.LocaleResolver)"](resolver._jdel);
      return that;
    } else utils.invalidArgs();
  };

  /**
   Add the specified locale as being supported by your application.
   This is used to find the best matching locale between locales supported by a user and locales supported by your application 
   For convenience, the format of the speficied locale can either be a valid Locale (ie: fr_FR) or a valid LanguageTag (ie: fr-fr)

   @public
   @param locale {string} 
   @return {LocaleHandler} 
   */
  this.addSupportedLocale = function(locale) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_localeHandler["addSupportedLocale(java.lang.String)"](locale);
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_localeHandler;
};

/**
 Create a handler

 @memberof module:vertx-apex-js/locale_handler

 @return {LocaleHandler} the handler
 */
LocaleHandler.create = function() {
  var __args = arguments;
  if (__args.length === 0) {
    return new LocaleHandler(JLocaleHandler["create()"]());
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = LocaleHandler;