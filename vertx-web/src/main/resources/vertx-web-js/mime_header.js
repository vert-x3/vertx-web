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

/** @module vertx-web-js/mime_header */
var utils = require('vertx-js/util/utils');
var ParsedHeaderValue = require('vertx-web-js/parsed_header_value');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMIMEHeader = Java.type('io.vertx.ext.web.MIMEHeader');

/**
 @class
*/
var MIMEHeader = function(j_val) {

  var j_mIMEHeader = j_val;
  var that = this;
  ParsedHeaderValue.call(this, j_val);

  /**
   Contains the raw value that was received from the user agent 

   @public

   @return {string}
   */
  this.rawValue = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["rawValue()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Holds the unparsed value of the header.<br>
   For the most part, this is the content before the semi-colon (";")

   @public

   @return {string}
   */
  this.value = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["value()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Holds the weight specified in the "q" parameter of the header.<br>
   If the parameter is not specified, 1.0 is assumed according to 
   <a href="https://tools.ietf.org/html/rfc7231#section-5.3.1">rfc7231</a>

   @public

   @return {number} 
   */
  this.weight = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["weight()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   The value of the parameter specified by this key. Each is one of 3 things:
   <ol>
   <li>null &lt;- That key was not specified</li>
   <li>ParsedHeaderValue.EMPTY (tested using ==) &lt;- The value was not specified</li>
   <li>[Other] <- The value of the parameter</li>
   </ol>
   <b>Note:</b> The <code>q</code> parameter is never present.

   @public
   @param key {string} 
   @return {string} 
   */
  this.parameter = function(key) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      return j_mIMEHeader["parameter(java.lang.String)"](key);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   The parameters specified in this header value.
   <b>Note:</b> The <code>q</code> parameter is never present.

   @public

   @return {Array.<string>} Unmodifiable Map of parameters of this header value
   */
  this.parameters = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnMap(j_mIMEHeader["parameters()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Is this an allowed operation as specified by the corresponding header?

   @public

   @return {boolean} 
   */
  this.isPermitted = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["isPermitted()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Test if this header is matched by matchTry header 

   @public
   @param matchTry {ParsedHeaderValue} The header to be matched from 
   @return {boolean} true if this header represents a subset of matchTry, otherwise, false
   */
  this.isMatchedBy = function(matchTry) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      return j_mIMEHeader["isMatchedBy(io.vertx.ext.web.ParsedHeaderValue)"](matchTry._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   An integer that represents the absolute order position of this header

   @public

   @return {number}
   */
  this.weightedOrder = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["weightedOrder()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Gets the parsed component part of the MIME. This is the string between the beginning and the first @{code '/'}
   of the MIME

   @public

   @return {string} The component of the MIME this represents
   */
  this.component = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["component()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Gets the parsed subcomponent part of the MIME. This is the string between the first @{code '/'} and the

   @public

   @return {string} The subcomponent of the MIME this represents
   */
  this.subComponent = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_mIMEHeader["subComponent()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_mIMEHeader;
};

MIMEHeader._jclass = utils.getJavaClass("io.vertx.ext.web.MIMEHeader");
MIMEHeader._jtype = {
  accept: function(obj) {
    return MIMEHeader._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(MIMEHeader.prototype, {});
    MIMEHeader.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
MIMEHeader._create = function(jdel) {
  var obj = Object.create(MIMEHeader.prototype, {});
  MIMEHeader.apply(obj, arguments);
  return obj;
}
module.exports = MIMEHeader;