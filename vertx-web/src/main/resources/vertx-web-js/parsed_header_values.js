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

/** @module vertx-web-js/parsed_header_values */
var utils = require('vertx-js/util/utils');
var ParsedHeaderValue = require('vertx-web-js/parsed_header_value');
var MIMEHeader = require('vertx-web-js/mime_header');
var LanguageHeader = require('vertx-web-js/language_header');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JParsedHeaderValues = io.vertx.ext.web.ParsedHeaderValues;

/**
 A container with the request's headers that are meaningful enough to be parsed
 Contains:
 <ul>
 <li>Accept -> MIME header, parameters and sortable</li>
 <li>Accept-Charset -> Parameters and sortable</li>
 <li>Accept-Encoding -> Parameters and sortable</li>
 <li>Accept-Language -> Parameters and sortable</li>
 <li>Content-Type -> MIME header and parameters</li>
 </ul>


 @class
*/
var ParsedHeaderValues = function(j_val) {

  var j_parsedHeaderValues = j_val;
  var that = this;

  /**

   @public

   @return {Array.<MIMEHeader>}
   */
  this.accept = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_parsedHeaderValues["accept()"](), MIMEHeader);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<ParsedHeaderValue>}
   */
  this.acceptCharset = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_parsedHeaderValues["acceptCharset()"](), ParsedHeaderValue);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<ParsedHeaderValue>}
   */
  this.acceptEncoding = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_parsedHeaderValues["acceptEncoding()"](), ParsedHeaderValue);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {Array.<LanguageHeader>}
   */
  this.acceptLanguage = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnListSetVertxGen(j_parsedHeaderValues["acceptLanguage()"](), LanguageHeader);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {MIMEHeader}
   */
  this.contentType = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(j_parsedHeaderValues["contentType()"](), MIMEHeader);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_parsedHeaderValues;
};

// We export the Constructor function
module.exports = ParsedHeaderValues;